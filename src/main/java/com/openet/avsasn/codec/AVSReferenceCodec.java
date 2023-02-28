package avsasn.codec;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import avsasn.AVS;
import avsasn.AVSType;
import avsasn.ber.ASN1Exception;
import avsasn.ber.ASN1Tag;
import avsasn.ber.ASN1TagClass;
import avsasn.ber.ASN1TagForm;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSReference;
import avsasn.primitivetypes.AVSValue;
import cfg.ObjectRepositoryException;

final public class AVSReferenceCodec extends AVSFieldCodec {

    public AVSReference decode(BERDecoder berDecodeBuffer) throws ASN1Exception {

        int length = berDecodeBuffer.decodeLength(); // skip enclosing tag
                                                     // (CONTEXT,AVSPAYLOAD,CONSTRUCTED)

        if (length != BERDecoder.INDEFINITE_LENGTH && berDecodeBuffer.bytesAvailable() < length)
            throw new ASN1Exception("Available bytes less then decoded AVS length");

        // Decode and fetch the type.
        ASN1Tag tag = berDecodeBuffer.decodeTag();
        if (tag.getTagId() != ASN1Tag.TAG_IA5STRING)
            throw new ASN1Exception("invalid AVS structure");

        length = berDecodeBuffer.decodeLength();

        String avsTypeName = berDecodeBuffer.decodeIA5String();
        if (null == avsTypeName) {
        	throw new ASN1Exception("Can't decode AvsTypeName");
        }
        AVSType avsType = null;

        try {
            avsType = AVSType.Get(avsTypeName);
        } catch (IllegalStateException exc) {
            throw new ASN1Exception(exc.getMessage());
        }catch (ObjectRepositoryException exc){
            throw new ASN1Exception("ObjectRepositoryException: "+exc.getMessage());
        }catch(Exception exc){
            throw new ASN1Exception("AVSType.Get uknown exception: "+exc.getMessage());
        }

        if (avsType == null)
            throw new ASN1Exception("Invalid AVS type: " + avsTypeName);

        
        /***************************************
         * VERSION TYPE
         * *************************************/
        if (avsType.isVersioned()) {
            AVS value = new AVS(avsType);
            int currentVersion = -1;
            for (tag = berDecodeBuffer.decodeTag();; tag = berDecodeBuffer.decodeTag()) {
                //System.out.println("Tag: " + tag.getTagId());
                if (tag.getTagClass() == ASN1TagClass.CONTEXT) {
                
                    if (tag.getTagId() < avsType.getLocalAVSFieldInfo().size()) {
                        int tagId = tag.getTagId();
                        int offset = avsType.getOffset();
                        avsasn.AVSFieldInfo fi = avsType.getFieldInfo(tagId + offset);
                        AVSFieldCodec codec = fi.getCodec();
                        value.setFieldValue(tagId + offset, codec.decode(berDecodeBuffer));                    
                    } else {
                        /*
                         * At this point we have no idea of the types, all we know is that the next octet(s) is a length.
                         * We need to deal with variable length types that could include sequences and embedded AVS instances.
                         * But at the same time we need to know when decoding of this AVS ends and decoding of a parent AVS begins.
                         */
                        int numVariableLengths = 0;
                        do {
                            length = berDecodeBuffer.decodeLength();                    
                            if (length != BERDecoder.INDEFINITE_LENGTH) {
                            
                                if (berDecodeBuffer.bytesAvailable() < length) {
                                    throw new ASN1Exception("Available bytes less then decoded AVS length");
                                }
                                berDecodeBuffer.moveBufferPosition(length);

                            } else {
                                ++numVariableLengths;
                            }

                            if (numVariableLengths > 0) {
                                boolean isNullTag = true;
                                do {
                                    // decodeTag(b, cls, tag, con);
                                    tag = berDecodeBuffer.decodeTag();
                                    
                                    if (tag.getTagClass() == ASN1TagClass.UNIVERSAL && tag.getTagId() == 0) {
                                    
                                        if (berDecodeBuffer.decodeLength() != 0)
                                            throw new ASN1Exception("Invalid end of contents value");
                                    
                                        --numVariableLengths;  
                                  
                                    } else {
                                        isNullTag = false;
                                    }

                                } while (numVariableLengths > 0 && isNullTag);
                            }
                        } while (numVariableLengths > 0);
                    }
                } else if (tag.getTagClass() == ASN1TagClass.UNIVERSAL) {
                    
                    if (tag.getTagId() == 0) {

                        if (berDecodeBuffer.decodeLength() != 0) {
                            throw new ASN1Exception("Invalid end of contents value");
                        }
                        
                        // done. break for loop
                        break;
                        
                    } else if (tag.getTagId() == ASN1Tag.TAG_INTEGER) {
                        // new version
                        length = berDecodeBuffer.decodeLength();
                        currentVersion = berDecodeBuffer.decodeInteger().intValue();         
                        tag = berDecodeBuffer.decodeTag();
                        
                        if (tag.getTagClass() == ASN1TagClass.UNIVERSAL && tag.getTagId() == ASN1Tag.TAG_INTEGER) {
                            
                            length = berDecodeBuffer.decodeLength();                            
                            if (length < 1) {
                                throw new ASN1Exception("Invalid AVS type [checksum length error]: " + avsTypeName);
                            }
                            
                            BigInteger decodedChecksum = berDecodeBuffer.decodeInteger();
                            
                            if (currentVersion <= avsType.getLatestVersion()) {                            
                            
                                int typeChecksum = avsType.getChecksum(currentVersion);
                                
                                if (decodedChecksum.intValue() == 0 || decodedChecksum.intValue() != typeChecksum) {
                                    throw new ASN1Exception("AVS decode version checksum mismatch for type: " + avsTypeName);
                                }
                            }
                        } else {
                            throw new ASN1Exception("Missing version checksum");
                        }
                        /* 
                         * Decode string 
                         * */
                    } else if (tag.getTagId() == ASN1Tag.TAG_IA5STRING) {
                        
                        // a parent AVS
                        length = berDecodeBuffer.decodeLength();
                        String parentAvs = berDecodeBuffer.decodeIA5String();
                                               
//                        int tagId = tag.getTagId();
//                        int offset = avsType.getOffset();
//                        avsasn.AVSFieldInfo fi = avsType.getFieldInfo(tagId + offset);
//                        AVSFieldCodec codec = fi.getCodec();
//                        String parentAvs = codec.decode(berDecodeBuffer).getValue().toString();                        
                        try {
                            avsType = AVSType.Get(parentAvs);
                            
                            if (avsType == null) {
                                throw new ASN1Exception("unknown AVS type");
                            }                            
                            currentVersion = -1;
//                            break;
                            
                        } catch (IllegalStateException exc) {
                            throw new ASN1Exception(exc.getMessage());
                        } catch (ObjectRepositoryException exc) {
                            throw new ASN1Exception("ObjectRepositoryException: " + exc.getMessage());
                        } catch (Exception exc) {
                            throw new ASN1Exception("AVSType.Get uknown exception: " + exc.getMessage());
                        }
                    } else {
                        throw new ASN1Exception("unexpected UNIVERSAL tag in AVS");
                    }
                } else {
                    throw new ASN1Exception("unexpected tag in AVS");
                }
            }
            return new AVSReference(value);
            /***************************************
             * UNVERSIONED TYPE
             ***************************************/
        } else {
        // decode the next TLV which may be a checksum
        tag = berDecodeBuffer.decodeTag();
        if(tag.getTagClass() == ASN1TagClass.APPLICATION) {
        	// Must be checksum - decode and verify it.
        	if(tag.getTagId() == 0 && tag.getTagForm() == ASN1TagForm.PRIMITIVE) {
        		length = berDecodeBuffer.decodeLength();
        		if(length != 16) {
        			throw new ASN1Exception("Invalid AVS type [checksum length error]: " + avsTypeName);
        		}
        		byte[] checksum = berDecodeBuffer.decodeOctetArray();
        		//if(avsType.getChecksum() == null || !Arrays.equals(checksum, avsType.getChecksum())) {
        			//throw new ASN1Exception("Invalid AVS type [checksum mismatch error]: " + avsTypeName + "checksum = " + checksum + " avsc = " + avsType.getChecksum());
        		//}
        		tag = berDecodeBuffer.decodeTag();
        	} else {
        		throw new ASN1Exception("Invalid AVS type [checksum encoding error]: " + avsTypeName);
        	}
        }
        // end decode checksum

        AVS value = new AVS(avsType);

        for (;;tag = berDecodeBuffer.decodeTag()) {
            if (tag.getTagClass() == ASN1TagClass.CONTEXT) {
                if (tag.getTagId() >= avsType.getNumberOfFields())
                    throw new ASN1Exception("AVS attribute out of range");

                value.setFieldValue(tag.getTagId(), avsType.getFieldInfo(tag.getTagId()).getCodec().decode(berDecodeBuffer));

            } else if (tag.getTagClass() == ASN1TagClass.UNIVERSAL) {
                if (tag.getTagId() == 0) {
                    if (berDecodeBuffer.decodeLength() == 0)
                        break;
                    else
                        throw new ASN1Exception("Invalid end of contents value");
                } else {
                    throw new ASN1Exception("unexpected UNIVERAL tag in AVS");
                }
            } else {
                throw new ASN1Exception("unexpected tag in AVS");
            }
        }
        return new AVSReference(value);
        }

        

    }

    /*!
     * @see com.openet.avsasn.codec.AVSFieldCodec#encode(com.openet.avsasn.primitivetypes.AVSValue, com.openet.avsasn.ber.BEREncoder)
     * Encodes field of type AVS.
     */
    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception {
        AVSReference ref = (AVSReference) value;
        AVS avs = ref.getValue();
        encoder.encode(avs);
        return encoder.getWriteBuffer();
    }
}
