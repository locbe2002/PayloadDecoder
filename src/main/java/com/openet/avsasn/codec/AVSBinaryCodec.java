package avsasn.codec;

import java.nio.ByteBuffer;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSValue;
import avsasn.primitivetypes.AVSBinary;

final public class AVSBinaryCodec extends AVSFieldCodec{
    
    public AVSBinaryCodec() {
    }
    
    public AVSValue decode(BERDecoder decodeBuffer) throws ASN1Exception {
        int length = decodeBuffer.decodeLength();
        
        if(length == BERDecoder.INDEFINITE_LENGTH) 
            throw new ASN1Exception("Decoding of indefinite length binary data not supported");
        
        
        if (decodeBuffer.bytesAvailable() < length) 
                throw new ASN1Exception("DecodeBufferUnderflow");

        
        ByteBuffer value=ByteBuffer.allocate(length);
        
        for(int i=0;i<length;i++)//XXX fix me do array copy instead
            value.put((byte)decodeBuffer.getOctet());
        
        value.flip();
        
        return new AVSBinary(value);
    }

    /*!
     * @see com.openet.avsasn.codec.AVSFieldCodec#encode(com.openet.avsasn.primitivetypes.AVSValue, com.openet.avsasn.ber.BEREncoder)
     * Encodes binary data. Adds length tag, and contents are copied directly from AVSValue.
     */
    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) {
        AVSBinary b = (AVSBinary) value;
        byte [] data = b.getValue();
        encoder.encodeLength(data.length);
        encoder.getWriteBuffer().put(data);
        return encoder.getWriteBuffer();
    }

}
