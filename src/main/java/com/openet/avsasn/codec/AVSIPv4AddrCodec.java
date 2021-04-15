package avsasn.codec;

//import java.net.Inet4Address;

import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSIPv4Addr;
import avsasn.primitivetypes.AVSValue;

//XXX no depending how we will use this class
// the value will change right now stored it as string in decimal dot notation
final public class AVSIPv4AddrCodec extends AVSFieldCodec{
        
    public AVSValue decode(BERDecoder decodeBuffer) throws ASN1Exception {
        int length=decodeBuffer.decodeLength();
        //System.out.println("decode ipv4 length="+ length);
        if (length == 4) {
            if (decodeBuffer.bytesAvailable() < length) {
                throw new ASN1Exception("DecodeBufferUnderflow");
            }
            short b1 = decodeBuffer.getOctet();
            short b2=decodeBuffer.getOctet();
            short b3=decodeBuffer.getOctet();
            short b4=decodeBuffer.getOctet();
            return new avsasn.primitivetypes.AVSIPv4Addr(b1,b2,b3,b4);
         }else
         throw new ASN1Exception("Invalid IPv4Address");
    }

    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception {
        AVSIPv4Addr addr = (AVSIPv4Addr) value;
        short[] data = addr.getRawValue();
        try {
            encoder.encodeLength(4);
            int idx = 0;
            for(; idx < data.length; idx++)
                encoder.getWriteBuffer().put((byte)data[idx]);
        } catch (Exception e) {
            throw new ASN1Exception("Can't encode AVSIPv4Addr.");
        }
        return encoder.getWriteBuffer();
    }

}
