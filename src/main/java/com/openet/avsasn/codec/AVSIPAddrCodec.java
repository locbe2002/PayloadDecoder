package avsasn.codec;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSIPAddr;
import avsasn.primitivetypes.AVSValue;

final public class AVSIPAddrCodec extends AVSFieldCodec{
        
    public AVSValue decode(BERDecoder decodeBuffer) throws ASN1Exception {
        int length=decodeBuffer.decodeLength();
        if (length == 16 || length == 4) {
            if (decodeBuffer.bytesAvailable() < length) {
                throw new ASN1Exception("DecodeBufferUnderflow");
            }
            short bytes[] = new short[length];
            for(int i=0;i<length;++i){
                bytes[i] = decodeBuffer.getOctet();
            }
            
            return new avsasn.primitivetypes.AVSIPAddr(bytes);
        }else
             throw new ASN1Exception("Invalid IPv6Address");
    }

    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception {
        AVSIPAddr addr = (AVSIPAddr) value;
        // According to C++ code only 4 bytes encoded ... weird. How about V6?
        short[] data = addr.getRawValue();
        if (data.length != 4)
            if (data.length != 16) {
                throw new ASN1Exception("Wrong AVSIPAddr length: "+ data.length);
            }
        encoder.encodeLength(data.length);
        int idx = 0;
        for (; idx < data.length; idx++) {
            encoder.getWriteBuffer().put( (byte)data[idx]);
        }
        return encoder.getWriteBuffer();
    }

}
