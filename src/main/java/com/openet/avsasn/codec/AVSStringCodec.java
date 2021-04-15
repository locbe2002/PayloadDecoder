package avsasn.codec;

import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSString;
import avsasn.primitivetypes.AVSValue;

final public class AVSStringCodec extends AVSFieldCodec {

    public AVSStringCodec() {
    }

    public AVSString decode(BERDecoder decodeBuffer) throws ASN1Exception {
        // System.out.println("AVSString.decode() called");

        int length = decodeBuffer.decodeLength();
        // System.out.println("AVSString length="+length);
        // System.out.println("AVSString bytes left="+decodeBuffer.bytesAvailable());

        if (length == BERDecoder.INDEFINITE_LENGTH) {
            throw new ASN1Exception("can't do indefinte length strings");
        }

        if (decodeBuffer.bytesAvailable() >= length) {
            return new AVSString(decodeBuffer.decodeIA5String());
            // System.out.println("value="+value);
        } else {
            throw new ASN1Exception("buffer underflow decoded string length: "
                    + length + ", bytes left:" + decodeBuffer.bytesAvailable());
        }

    }

    /*!
     * Convenient method. Used by other codecs.
     */
    public void encode(AVSString string, BEREncoder enc) throws ASN1Exception {
        encode(string.getValue(), enc);
    }

    /*!
     * Convenient method. Used by other codecs.
     * Encodes Java string as a bytearray.
     */
    public void encode(String what, BEREncoder enc) throws ASN1Exception {
        enc.encodeLength(what.length());
        ByteBuffer writeBuffer = enc.getWriteBuffer();
        writeBuffer.put(what.getBytes());
    }

    /*!
     * @see com.openet.avsasn.codec.AVSFieldCodec#encode(com.openet.avsasn.primitivetypes.AVSValue, com.openet.avsasn.ber.BEREncoder)
     * Encodes AVSString.
     */
    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception {
        try {
        	String str = ((AVSString) value).getValue();
        	if (encoder.getWriteBuffer().remaining() <= str.length()) {
        		encoder.resizeBuffer(str.length());
        	}
            encode(str, encoder);
        } catch (ASN1Exception e) {
            throw e;
        }
        return encoder.getWriteBuffer();
    }
}
