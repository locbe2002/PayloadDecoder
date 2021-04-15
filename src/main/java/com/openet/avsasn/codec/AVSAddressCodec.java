package avsasn.codec;

import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSAddress;
import avsasn.primitivetypes.AVSValue;

final public class AVSAddressCodec extends AVSFieldCodec{
        
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
            
            return new avsasn.primitivetypes.AVSAddress(bytes);
        }else
             throw new ASN1Exception("Invalid Address (IPv6)");
    }

    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception {
        AVSAddress addr = (AVSAddress) value;
        // According to C++ code only 4 bytes encoded ... weird. How about V6?
        short[] data = addr.getRawValue();
        if (data.length != 4)
            if (data.length != 16) {
                throw new ASN1Exception("Wrong AVSAddress length: "+ data.length);
            }
        encoder.encodeLength(data.length);
        int idx = 0;
        for (; idx < data.length; idx++) {
            encoder.getWriteBuffer().put( (byte)data[idx]);
        }
        return encoder.getWriteBuffer();
    }

}
