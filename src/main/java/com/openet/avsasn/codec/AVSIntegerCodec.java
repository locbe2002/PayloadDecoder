package avsasn.codec;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSInteger;
import avsasn.primitivetypes.AVSValue;

final public class AVSIntegerCodec extends AVSFieldCodec{
    
    public AVSIntegerCodec(){
    }

    public AVSValue decode(BERDecoder decodeBuffer) throws ASN1Exception {
        if(decodeBuffer.decodeLength()==BERDecoder.INDEFINITE_LENGTH) // note this check is done in BERDecoder
                                                                      //need better strategy where to check for this type of errors
           throw new ASN1Exception("Decoding of indefinite length primitive integer not supported");
        BigInteger value = decodeBuffer.decodeInteger();
        return new AVSInteger(value);
        
    }
    
    /*!
     * AVSIntegerCodec
     * @see com.openet.avsasn.codec.AVSFieldCodec#encode(com.openet.avsasn.primitivetypes.AVSValue, com.openet.avsasn.ber.BEREncoder)
     * 
     * Encodes BigInteger. 
     * The number of written bytes is equals to actual number of non-zero octects in value.
     * So for 
     */
    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) {
        BigInteger bigValue =((AVSInteger) value).getValue();
        // Special case
        if (bigValue == BigInteger.ZERO) {
            encoder.encodeLength(1);
            encoder.getWriteBuffer().put((byte)0x0);
        } else {
            byte [] val = bigValue.toByteArray();
            encoder.encodeLength(val.length);
            encoder.getWriteBuffer().put(val);
        }
        return encoder.getWriteBuffer();
    }

}
