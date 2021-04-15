package avsasn.codec;


import java.nio.ByteBuffer;
import java.util.Date;

import avsasn.ber.ASN1Exception;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.primitivetypes.AVSValue;
import avsasn.primitivetypes.AVSUTCTime;

final public class AVSUTCTimeCodec extends AVSFieldCodec{
    
    //XXX fix me
    public AVSValue decode(BERDecoder decodeBuffer) throws ASN1Exception {
        //System.out.println("AVSUTCtime.decode() called");
        
        int length = decodeBuffer.decodeLength();
        //System.out.println("length="+length);
        //System.out.println("bytes left="+decodeBuffer.bytesAvailable());
        if (length != BERDecoder.INDEFINITE_LENGTH && decodeBuffer.bytesAvailable() >= length ) {	
    	    String ia5s = decodeBuffer.decodeIA5String();
    	    if (ia5s.isEmpty()) {
    		    ia5s = "0";
    	    }
            return new AVSUTCTime(new Date(Long.valueOf(ia5s) * 1000));
    
        } else {
            throw new ASN1Exception("can't do indefinte length uttimestamps");
        }

    }
    
    private static AVSStringCodec codec = null;

    /*!
     * @see com.openet.avsasn.codec.AVSFieldCodec#encode(com.openet.avsasn.primitivetypes.AVSValue, com.openet.avsasn.ber.BEREncoder)
     * Encodes UTCTime as string.
     */
    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception {
        if (codec == null)
            codec = new AVSStringCodec();
        try {
            codec.encode( ((AVSUTCTime)value).toString(), encoder);
        } catch (ASN1Exception e) {
            throw e;
        }
        return encoder.getWriteBuffer();
    }
    
}
