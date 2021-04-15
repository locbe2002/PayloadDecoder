package avsasn.codec;

import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSValue;

public abstract class AVSFieldCodec {

    public abstract AVSValue decode(BERDecoder decodeBuffer) throws ASN1Exception;
   
    //not implemented for now
    public abstract ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception;

}
