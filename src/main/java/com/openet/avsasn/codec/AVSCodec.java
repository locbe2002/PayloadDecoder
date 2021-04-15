package avsasn.codec;

import java.nio.ByteBuffer;
import java.util.HashMap;

import avsasn.AVS;
import avsasn.AVSFieldType;
import avsasn.AVSTypeRepository;
import avsasn.ber.ASN1Exception;
import avsasn.ber.ASN1Tag;
import avsasn.ber.ASN1TagClass;
import avsasn.ber.ASN1TagForm;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;

//Note this class will be thread safe when AVSTypeRopesitory will be thread safe
public class AVSCodec {

    private static HashMap<AVSFieldType, AVSFieldCodec> avsFieldsCodecs = new HashMap<AVSFieldType, AVSFieldCodec>();
    private static HashMap<AVSFieldType, AVSFieldCodec> avsMultiValuedFieldsCodecs = new HashMap<AVSFieldType, AVSFieldCodec>();

    static {

        avsFieldsCodecs.put(AVSFieldType.BINARY, new AVSBinaryCodec());
        avsFieldsCodecs.put(AVSFieldType.BOOLEAN, new AVSBooleanCodec());
        avsFieldsCodecs.put(AVSFieldType.STRING, new AVSStringCodec());
        avsFieldsCodecs.put(AVSFieldType.INTEGER, new AVSIntegerCodec());
        avsFieldsCodecs.put(AVSFieldType.IPV4ADDR, new AVSIPv4AddrCodec());
        avsFieldsCodecs.put(AVSFieldType.IPADDR, new AVSIPAddrCodec());
        avsFieldsCodecs.put(AVSFieldType.ADDRESS, new AVSAddressCodec());
        avsFieldsCodecs.put(AVSFieldType.REFERENCE, new AVSReferenceCodec());
        avsFieldsCodecs.put(AVSFieldType.UTCTIME, new AVSUTCTimeCodec());

        avsMultiValuedFieldsCodecs.put(AVSFieldType.BINARY, new AVSSequenceCodec(AVSFieldType.BINARY, avsFieldsCodecs.get(AVSFieldType.BINARY)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.BOOLEAN, new AVSSequenceCodec(AVSFieldType.BOOLEAN, avsFieldsCodecs.get(AVSFieldType.BOOLEAN)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.STRING, new AVSSequenceCodec(AVSFieldType.STRING, avsFieldsCodecs.get(AVSFieldType.STRING)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.INTEGER, new AVSSequenceCodec(AVSFieldType.INTEGER, avsFieldsCodecs.get(AVSFieldType.INTEGER)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.IPV4ADDR, new AVSSequenceCodec(AVSFieldType.IPV4ADDR, avsFieldsCodecs.get(AVSFieldType.IPV4ADDR)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.IPADDR, new AVSSequenceCodec(AVSFieldType.IPADDR, avsFieldsCodecs.get(AVSFieldType.IPADDR)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.ADDRESS, new AVSSequenceCodec(AVSFieldType.ADDRESS, avsFieldsCodecs.get(AVSFieldType.ADDRESS)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.REFERENCE, new AVSSequenceCodec(AVSFieldType.REFERENCE, avsFieldsCodecs.get(AVSFieldType.REFERENCE)));
        avsMultiValuedFieldsCodecs.put(AVSFieldType.UTCTIME, new AVSSequenceCodec(AVSFieldType.UTCTIME, avsFieldsCodecs.get(AVSFieldType.UTCTIME)));

    }

    public static AVSFieldCodec getAVSFieldCodec(AVSFieldType fieldType, boolean isMultiValued) {
        if (isMultiValued)
            return AVSCodec.avsMultiValuedFieldsCodecs.get(fieldType);
        else
            return AVSCodec.avsFieldsCodecs.get(fieldType);
    }

    //note that this ctor just checks if AVSTypeRepository is correctly initialized we could skip that step
    //and then we would get ASN1Exceptions while decoding
    public AVSCodec(){
        if(!AVSTypeRepository.getInstance().isInitialized())
            throw new IllegalStateException("AVSTypeRepository is not initialized");
    }

    private static final int AVSPAYLOAD_TAG_ID = 4;

    public AVS decodeAVS(ByteBuffer buffer) throws ASN1Exception {

        BERDecoder berDecodeBuffer = new BERDecoder(buffer);
        ASN1Tag tag = berDecodeBuffer.decodeTag();

        if (tag.getTagForm() != ASN1TagForm.CONSTRUCTED)
            throw new ASN1Exception("This is not AVS: expected tag form: CONSTRUCTED, but got: PRIMITIVE");

        if (tag.getTagClass() != ASN1TagClass.CONTEXT)
            throw new ASN1Exception("This is not AVS: expected tag class: " + ASN1TagClass.CONTEXT + ", but got: " + tag.getTagClass());

        if (tag.getTagId() != AVSPAYLOAD_TAG_ID)
            throw new ASN1Exception("This is not AVS: expected tag: " + AVSPAYLOAD_TAG_ID + ", but got: " + tag.getTagId());

        AVSFieldCodec codec = AVSCodec.getAVSFieldCodec(AVSFieldType.REFERENCE, false);
        if (codec == null)
            throw new ASN1Exception("codec == null");
        avsasn.primitivetypes.AVSValue v = codec.decode(berDecodeBuffer);
        if (v ==null)
            throw new ASN1Exception("v == null");
        Object o = v.getValue();
        if (o ==null)
            throw new ASN1Exception("o == null");

//        return (AVS) AVSCodec.getAVSFieldCodec(AVSFieldType.REFERENCE, false).decode(berDecodeBuffer).getValue();
        return (AVS)o;
    }

    public ByteBuffer encodeAVS(AVS avs) throws ASN1Exception {
    	BEREncoder encoder = new BEREncoder(avs);
    	ByteBuffer payload = null;
    	try {
    		payload = encoder.encode();
    	} catch (Exception e) {
    		throw new ASN1Exception(e);
    	}
        return payload;
    }
}
