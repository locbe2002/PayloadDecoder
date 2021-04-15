package avsasn.codec;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSValue;
import avsasn.primitivetypes.AVSBoolean;

final public class AVSBooleanCodec extends AVSFieldCodec {

    public AVSBooleanCodec() {
    }

    //note booleans are encoded as integers so we convert to boolean i.e. if 0 then false else true
    public AVSValue decode(BERDecoder berDecodeBuffer) throws ASN1Exception {
        if (berDecodeBuffer.decodeLength() == BERDecoder.INDEFINITE_LENGTH) // note this check is done in BERDecoder
            //need better strategy where to check for this type of errors
            throw new ASN1Exception("Decoding of indefinite length primitive boolean not supported");
        return new AVSBoolean(berDecodeBuffer.decodeInteger().equals(BigInteger.ZERO)?Boolean.FALSE:Boolean.TRUE);

    }

    /*!
     * @see com.openet.avsasn.codec.AVSFieldCodec#encode(com.openet.avsasn.primitivetypes.AVSValue, com.openet.avsasn.ber.BEREncoder)
     * Encodes AVSBoolean as single octet.
     */
    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) {
        byte boolValue = (((AVSBoolean)value).getValue())?(byte)1:(byte)0;
        encoder.encodeLength(1);
        encoder.getWriteBuffer().put(boolValue);
        return encoder.getWriteBuffer();
    }
}
