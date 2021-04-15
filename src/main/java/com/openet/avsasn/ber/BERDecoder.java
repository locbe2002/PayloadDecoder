package avsasn.ber;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/* Disclaimer: This class doesn't implement ber decoder 
 * only subset of ber decoding rules are implemented 
 * note there is no guarantee that even this subset is complaint with 
 * standard. It must be compatible with C++ implementation of ber encodings
 * see files asn.h asn.cc for C++ implementation details.
 * Also keep in mind that this is NOT ASN.1 implementation it just happens to use
 * some of the ASN.1 concepts but that's it.
 * 
 *  IMPORTANT: if asn.h asn.cc is changed in incompatible way i.e.
 *  support for new ASN.1 primitive types is added this class
 *  needs to be updated accordingly. (I should probably add that comment
 *  to asn.h as well)
 *  
 *  Note this is not straight port of asn avs C implementation.
 *  
 *   BTW Java doesn't have unsigned types so one needs to be very careful when handling byte data
 *   that exceeds 0x7F value especially when doing bit operations
 */

public class BERDecoder {

    //for BER encoding the TAG format is following
    // A) 0<=tag<=30 
    //            byte 1
    // ---------------------------------
    // | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | bits 
    // ---------------------------------
    // | class |p/c|  tag number bits  |
    // ---------------------------------
    // B) tag >=31
    //            byte 1                                   byte 2                                        byte n      
    // ---------------------------------       ---------------------------------             ---------------------------------
    // | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | bits  | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | bits  ...   | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | 
    // ---------------------------------       ---------------------------------             ---------------------------------
    // | class |p/c| 1 | 1 | 1 | 1 | 1 |       | 1  |     tag number bits      |             | 0 |     tag number bits       |
    // ---------------------------------       ---------------------------------             ---------------------------------
    //
    //Note: that for 2 >= byte < n bit 7 is always 1 and byte n has bit 7 equal 0 indicating and of tag   
    //the tag value is stored in 2 >= bytes <= n on bits 6 to 0  
    //
    //where: 
    // class is:
    // -------------------------------
    // | bit 7 | bit 6 |    class    |
    // -------------------------------
    // |   0   |   0   |  UNIVERSAL  |
    // |   0   |   1   | APPLICATION |
    // |   1   |   0   |   CONTEXT   |
    // |   1   |   1   |   PRIVATE   |
    // -------------------------------
    //
    // p/c (form of value: primitive or constructed):
    // -----------------------
    // | bit 5 |    form     |
    // -----------------------
    // |   0   |  PRIMITIVE  |
    // |   1   | CONSTRUCTED |
    // -----------------------
    //
    //Note AVS encoding uses implicit tagging meaning that only the tag that appears on the 
    // left-hand side of the IMPLICIT keyword is encoded in the tag field. But since we don't use
    // ASN.1 syntax to describe AVSes this comment is really irrelevant (Just don't expect that tools
    // that can work with ASN.1 will recognise type of the field).   

    private ByteBuffer buffer;
    public static final int INDEFINITE_LENGTH = -1;
    private int valueLength;
    
    //don't know if that's really necessary
    private DecoderState state;
    public DecoderState getState() {
        return state;
    }

    //    private ASN1Tag tag;
//    public ASN1Tag getLastDecodedTag(){
//        return tag;
//    }
    
    public BERDecoder(ByteBuffer buffer) {
        //Note this is not a deep copy. The backing array is not copied
        //but changes to this buffer are not visible in passed buffer
        //as long as they don't modify underlying data (which this class doesn't do)
        this.buffer = buffer.duplicate();
        state = DecoderState.START;
    }

    //TODO For EDC we don't need that constructor
    //but for AVS protocol it could be useful
    public BERDecoder(InputStream buffer) throws ASN1Exception {
        //TODO decide which initialisation method to use      
        throw new ASN1Exception("BERDecoder(InputStream buffer) not implemented yet");
    }

    public int bytesConsumed() {
        return buffer.position();
    }

    public int bytesAvailable() {
        return buffer.limit() - buffer.position();
    }

    public void mark() {
        buffer.mark();
    }
    
    public void reset() {
        buffer.reset();
    }
    
    public short getOctet() {
        return (short) (buffer.get() & 0xFF);//watch out no unsigned type in Java !
    }

    public void moveBufferPosition(int length) {
        for (int i = 0; i < length; ++i) {
            buffer.get();
        }
    }

       //C++ signature void decodeTag(OTBuffer *, Class &, unsigned int &, PrimConst &);
    public ASN1Tag decodeTag() throws ASN1Exception {

        ASN1TagClass tagClass;
        ASN1TagForm tagForm;
        int tagId;
        
        if (bytesAvailable() < 1) {
            state = DecoderState.ERROR;
            throw new ASN1Exception("DecodeBufferUnderflow"); // not enough data in buffer should probably return different exception type 
        }

        short tagByte = getOctet();
        //System.out.print("tagByte: " + tagByte);

        tagClass = ASN1TagClass.decode(tagByte);

        if ((tagByte & ASN1Tag.PC_MASK) != 0)
            tagForm = ASN1TagForm.CONSTRUCTED;
        else
            tagForm = ASN1TagForm.PRIMITIVE;
            
        //extract tag id bits from first byte
        tagId = tagByte & 0x1F;//00011111

        if (tagId == 31) {//long form 
            tagId = 0;
            while (true) {
                if (buffer.limit() == buffer.position()) {
                    state = DecoderState.ERROR;
                    throw new ASN1Exception("DecodeBufferUnderflow");
                }
                tagByte = getOctet();
                tagId = (tagId << 7) | (tagByte & 0x7F); // get contents on bits 6 to 0 

                if ((tagByte & ASN1Tag.CONTINUE_TAG_BIT) == 0)
                    break;
            }
        } //else tagId value  < 31 just one tag byte
          
        state = DecoderState.TAG_DECODED;
        //System.out.println("tagId: " + tagId);
        return new ASN1Tag(tagClass, tagForm, tagId);
    }

    // 
    //    public static final short LENGTH_FORM_BIT = 0x80;
    //    public static final short LENGTH_FORM_BIT = 0x80;

    //void decodeLength(OTBuffer *, unsigned int &);
    //ok to be 100% compliment with standard one should return BigInteger type 
    //rather than int the max int is 2^31 and actually BER encoding allows for max definite length to be 256^126
    // but IMHO maximum size of transmitted value in AVS to be 2^31 bytes is not really harsh restriction :-) 
    public int decodeLength() throws ASN1Exception {

        if (bytesAvailable() < 1) {
            throw new ASN1Exception("DecodeBufferUnderflow");
        }

        short lengthByte = getOctet();

        if ((lengthByte & 0x80) == 0x80) {
            int lengthLength = lengthByte & 0x7F;
            long length = 0;
            if (lengthLength != 0) {//long definite length form

                if (bytesAvailable() < lengthLength)
                    throw new ASN1Exception("DecodeBufferUnderflow");

                while (lengthLength-- > 0) {
                    length = length << 8 | getOctet();
                    if (length >= 0x80000000L)
                        throw new ASN1Exception("Length value too big");

                }

                valueLength = (int) length;

            } else {//indefinite length form
                valueLength = INDEFINITE_LENGTH;
            }

        } else {//short definite length
            valueLength = lengthByte & 0x7F;
        }
        state = DecoderState.LENGTH_DECODED;
        return valueLength;
    }

    //C++ long long decodeInteger(OTBuffer *b);
    public BigInteger decodeInteger() throws ASN1Exception {
        //int valueLength = decodeLength();
        if (valueLength == INDEFINITE_LENGTH) {
            throw new ASN1Exception("Can't decode indefinite length integer");
        }

        byte[] valueBytes = new byte[valueLength];

        for (int i = 0; i < valueLength; i++)
            valueBytes[i] = (byte) getOctet();
        
        //System.out.printf("%02X\n", valueBytes[0]);
        //System.out.println("BigInt value"+ new BigInteger(valueBytes));
        state = DecoderState.VALUE_DECODED;
        return new BigInteger(valueBytes); //this constructor assumes that bytes are in big endian format 
    }

    public String decodeIA5String() throws ASN1Exception {
        if (valueLength == INDEFINITE_LENGTH) {
            throw new ASN1Exception("Can't decode indefinite length string");
        }

        byte[] valueBytes = new byte[valueLength];

        for (int i = 0; i < valueLength; i++)
            valueBytes[i] = (byte) getOctet();

        state = DecoderState.VALUE_DECODED;
        return new String(valueBytes);
    }

    /**
     * Decodes 
     * @return Octet array as a byte array.
     * @throws ASN1Exception
     */
    public byte[] decodeOctetArray() throws ASN1Exception {
        if (valueLength == INDEFINITE_LENGTH) {
            throw new ASN1Exception("Can't decode indefinite length octet array");
        }
        
        byte[] valueBytes = new byte[valueLength];
        for (int i = 0; i < valueLength; i++)
            valueBytes[i] = (byte) getOctet();
        state = DecoderState.VALUE_DECODED;
        return valueBytes;
    }
}
