package avsasn.ber;

import avsasn.AVS;
import avsasn.AVSType;

import java.math.BigInteger;
//import avsasn.ber.ASN1TagClass;
import java.nio.ByteBuffer;

import avsasn.codec.AVSFieldCodec;
import avsasn.codec.AVSIntegerCodec;
import avsasn.codec.AVSSequenceCodec;
import avsasn.codec.AVSStringCodec;
import avsasn.codec.AVSReferenceCodec;
import avsasn.primitivetypes.AVSInteger;

/* see disclaimer in BERDecoder class */

public class BEREncoder {

    private AVS _avs = null;
    private ByteBuffer _writeBuffer;
    private static final int DEFAULT_BUFFER_SIZE = 32*1024;
    private static final int REMAINING_LIMIT = 64;

    /*!
     * Basic ASN1/Openet primitives and their values.
     */
    public static class ASN1 {
        // Currently, only non-versioned encoding supported
        static final short NON_VERSIONED_CHECKSUM_LENGTH = 16;
        /// Maximum supported length that can be encoded in single octet
        static final short MAX_SINGLE_BYTE_LENGTH = 0x7F;
        /// High form length marker for really large data, e.g. two or more octets for length
        static final short HIGH_FORM_LENGTH = (1<<7);
        /// End of contents marker
        static final short END_OF_CONTENTS = 0x0;
        /// Begin of contents marker
        static final short BEGIN_OF_CONTENTS = 0x80;
        /*!
         * Class Tag contains numerical
         * representation of to be encoded tag.
         */
        public static class Tag {
            public static final short TAG_HIGHFORM = 0x1F;
            public static final short TAG_IA5STRING = 0x16;
            public static final short TAG_INTEGER = 0x02;
            public static final short TAG_ZERO = 0x0;
        };
        /*!
         * Class Class contains numerical
         * representation of to be encoded class.
         */
        public static class Class {
            public static final byte ID_CLASS_SHIFT = 6;
            public static final short UNIVERSAL = 0 << ID_CLASS_SHIFT;
            public static final short APPLICATION = 1 << ID_CLASS_SHIFT;
            public static final short CONTEXT = 2 << ID_CLASS_SHIFT;
            public static final short PRIVATELY = 3 << ID_CLASS_SHIFT;
        };
        /*!
         * Class Primitive is a holder of
         * supported primitives.
         */
        public static class Primitive {
            public static final short COMPOUND = (1<<5);
            public static final short PRIMITIVE = 0x0;
        }
    };

    public BEREncoder(AVS avs) {
        _avs = avs;
        _writeBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
    }

    /*!
     * Proceedes actual encoding and returns ByteBuffer
     * with encoded payload.
     */
    public ByteBuffer encode() throws ASN1Exception {
        encode(_avs);
        _writeBuffer.flip();
        return _writeBuffer;
    }
    
    /*!
     * Should not be used directly, but used by AVSReference.
     */
    public ByteBuffer encode(AVS avs) throws ASN1Exception {
        encodeVarLength(); // Enclose tag
        try {
        	AVSStringCodec stringCodec = new AVSStringCodec();
        	if (avs.getAVSType().isVersioned()) {
                //System.out.println("AVS Is Versioned");
        		AVSIntegerCodec intCodec = new AVSIntegerCodec();
        		AVSType type = avs.getAVSType();
        		int currentVersion = 0;
        		while (type != null) {
                    //System.out.print(type.toString() + " ");
	        		encodeTag(ASN1.Class.UNIVERSAL, ASN1.Tag.TAG_IA5STRING, ASN1.Primitive.PRIMITIVE); // Tag for string
	        		stringCodec.encode(type.getName(), this);
	        		for (int idx = 0; idx < type.getLocalAVSFieldInfo().size(); idx++) {
	        			if (type.getLocalAVSFieldInfo().elementAt(idx).getVersion() > currentVersion) {
	        				currentVersion =type.getLocalAVSFieldInfo().elementAt(idx).getVersion();									
	        				encodeTag(ASN1.Class.UNIVERSAL, ASN1.Tag.TAG_INTEGER, ASN1.Primitive.PRIMITIVE);
	        				intCodec.encode(new AVSInteger(BigInteger.valueOf(currentVersion)), this);
	        				encodeTag(ASN1.Class.UNIVERSAL, ASN1.Tag.TAG_INTEGER, ASN1.Primitive.PRIMITIVE);
	        				// Wierd. For non-versioned AVSes we are using string codec for checksum
	        				//
	        				intCodec.encode(new AVSInteger(BigInteger.valueOf(type.getChecksum(currentVersion))), this);
	        				//intCodec.encode(new AVSInteger(BigInteger.valueOf(0xaaaa0)), this);
	        			}		
	        			
	                    int tag = type.getLocalAVSFieldInfo().elementAt(idx).getTagId();
	                    if (avs.getFieldValue(tag) == null)
	                        continue;
	                    AVSFieldCodec c = type.getLocalAVSFieldInfo().elementAt(idx).getCodec();
	                    // For sequence Primitive should be Constructed
	                    if (c.getClass() == AVSSequenceCodec.class) { /* TODO: Do we need that?|| c.getClass() == AVSReferenceCodec.class) */
                            //System.out.println("AVSSequenceCodec.class");
	                        encodeTag(ASN1.Class.CONTEXT, (short)idx, ASN1.Primitive.COMPOUND);
	                    } else {
                            //System.out.println(c.getClass().toString());
	                        encodeTag(ASN1.Class.CONTEXT, (short)idx, ASN1.Primitive.PRIMITIVE);
	                    }
	                    c.encode(avs.getFieldValue(tag), this);
	                }
	                type = type.getParentType();
	                currentVersion = 0;
        		}
        		
        	} else {
                //System.out.println("AVS Is NotVersioned");
	            encodeTag(ASN1.Class.UNIVERSAL, ASN1.Tag.TAG_IA5STRING, ASN1.Primitive.PRIMITIVE); // Tag for string
	            stringCodec.encode(avs.getAVSType().getName(), this); // Put type information
	            encodeTag(ASN1.Class.APPLICATION, ASN1.Tag.TAG_ZERO, ASN1.Primitive.PRIMITIVE); // Used for checksum
	            // Checksum
	            encodeChecksum(avs.getAVSType().getChecksum());
	            AVSType type = avs.getAVSType();
	            while (type != null) {
	                for (int idx = 0; idx < type.getLocalAVSFieldInfo().size(); idx++) {
	                    int tag = type.getLocalAVSFieldInfo().elementAt(idx).getTagId();
	                    if (avs.getFieldValue(tag) == null)
	                        continue;
	                    AVSFieldCodec c = type.getLocalAVSFieldInfo().elementAt(idx).getCodec();
	                    // For sequence Primitive should be Constructed
	                    if (c.getClass() == AVSSequenceCodec.class || c.getClass() == AVSReferenceCodec.class) {
	                        encodeTag(ASN1.Class.CONTEXT, (short)tag, ASN1.Primitive.COMPOUND);
	                    } else {
	                        encodeTag(ASN1.Class.CONTEXT, (short)tag, ASN1.Primitive.PRIMITIVE);
	                    }
	                    c.encode(avs.getFieldValue(tag), this);
	                }
	                type = type.getParentType();
	            }
        	}
            encodeVarTrail(); // End of encoding
        } catch (Exception e) {
            throw new ASN1Exception(e);
        }			
        return _writeBuffer;
    }

    /*!
     * Used to encode begin-of-contents (first byte in stream)
     * or as marker of begin-of-sequence.
     */
    public void encodeVarLength() {
        _writeBuffer.put((byte) ASN1.BEGIN_OF_CONTENTS);
    }

    /*!
     * Used to encode tag by class, tag value and pc type.
     * Note, it supports HIGH and LOW encoding as C++ version.
     */
    public void encodeTag(short cls, short tag, short primConst) {
        if (tag >= ASN1.Tag.TAG_HIGHFORM) {
            //System.out.print("ASN1.Tag.TAG_HIGHFORM cls: " + cls + ", tag: " + tag + ", primConst: " + primConst);
            BigInteger value = BigInteger.valueOf(cls | primConst | ASN1.Tag.TAG_HIGHFORM);
            ByteArrayToString(value.toByteArray());
            byte[] b = value.toByteArray();
            int i = 0;
            for (; i < b.length; i++) {
                if (b[i] == 0x0) continue;
                else break;
            }
            for (; i < b.length; i++)
                _writeBuffer.put(b[i]);
            _writeBuffer.put((byte) (tag & 0xff));
        } else {
            //System.out.print("cls: " + cls + ", tag: " + tag + ", primConst: " + primConst);
            byte value = (byte)(cls | primConst | tag);
            ByteToString(value);
            _writeBuffer.put(value);
        }
        //System.out.println(" ");
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private void ByteArrayToString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        //System.out.println(new String(hexChars));
    }
    private void ByteToString(byte bytes) {
        char[] hexChars = new char[2];
        int v = bytes & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        //System.out.println(new String(hexChars));
    }

    /*!
     * If length require more the single octet,
     * the method automatically uses HIGH form.
     */
    public void encodeLength(int length) {
        // Check if length can be encoded by single byte
        if (length > ASN1.MAX_SINGLE_BYTE_LENGTH) {
            int size = sizeOfInt(length);
            // Encoding length tag with 7th bit set to 1
            // Note the sign
            _writeBuffer.put((byte) ( ((size | ASN1.HIGH_FORM_LENGTH) & 0xff)));
            
            for (int i =  (size - 1) * 8; i >= 0; i -= 8) {
                byte c = (byte) (length >> i & 0xff);
                // Don't write leading zeroes.
                if (c !=0){
                    _writeBuffer.put(c);
                }
            }
        } else {
            _writeBuffer.put((byte) length);
        }
    }
    
    public void resizeBuffer(int bytes) {
    	int pos = _writeBuffer.position();
		int bufferSize = _writeBuffer.capacity();

		// Hopefully GC will deallocate mem. for old one...
		if (bufferSize+bytes <= bufferSize) {
			// Do nothing
			return;
		}
		byte [] data = new byte[pos];
		_writeBuffer.position(0);
		_writeBuffer.get(data);
		_writeBuffer = ByteBuffer.allocateDirect(bufferSize + bytes);
		_writeBuffer.put(data);
    }

    /*!
     * Returns writeBuffer.
     * Note, no flip required if encode() finished successfully.
     */
    public ByteBuffer getWriteBuffer() {
    	if (_writeBuffer.remaining() < REMAINING_LIMIT) {
    		resizeBuffer(REMAINING_LIMIT);
    	}
        return _writeBuffer;
    }

    /*!
     * An fast method for 32bit integer which
     * returns a number eq to minimum bytes required to store a value. 
     */
    private int sizeOfInt(int value) {
        int realSize = ((value & 0xff000000) != 0) ? 1 : 0
                + (((value & 0xffff0000) != 0) ? 1 : 0)
                + (((value & 0xffffff00) != 0) ? 1 : 0)
                + (((value & 0xffffffff) != 0) ? 1 : 0);
        return realSize;
    }

    /*!
     * Returns the number of octets required to store
     * value.
     * Convenience method for BigInt class.
     * Could be used to 64 bit integers. 
     */
    private int sizeOfInt(BigInteger value) {
        return value.toByteArray().length;
    }

    /*!
     * Puts end-of-contents marker to byte buffer.
     * Also used to mark end-of-sequence.
     */
    public void encodeVarTrail() {
        _writeBuffer.put((byte) ASN1.END_OF_CONTENTS);
        _writeBuffer.put((byte) ASN1.END_OF_CONTENTS);
    }

    /*!
     * Encodes checksum length and checksum value.
     * Note, currently only non-versioned checksum supported.  
     */
    public void encodeChecksum(byte[] checksum) throws ASN1Exception {
        if (null != checksum) {
            if (checksum.length > ASN1.NON_VERSIONED_CHECKSUM_LENGTH)
                throw new ASN1Exception("Checksum length is more then " + ASN1.NON_VERSIONED_CHECKSUM_LENGTH);
            encodeLength(ASN1.NON_VERSIONED_CHECKSUM_LENGTH);
            _writeBuffer.put(checksum);
        } else {
            byte[] fakeChecksum = new byte[ASN1.NON_VERSIONED_CHECKSUM_LENGTH];
            _writeBuffer.put(fakeChecksum);
        }
    }
}
