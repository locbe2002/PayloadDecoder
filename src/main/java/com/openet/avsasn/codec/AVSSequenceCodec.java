package avsasn.codec;

import java.nio.ByteBuffer;
import java.util.Vector;

import avsasn.AVSFieldType;
import avsasn.ber.ASN1Exception;
import avsasn.ber.ASN1Tag;
import avsasn.ber.ASN1TagClass;
import avsasn.ber.ASN1TagForm;
import avsasn.ber.BERDecoder;
import avsasn.ber.BEREncoder;
import avsasn.compoundtypes.AVSSequence;
import avsasn.primitivetypes.AVSValue;

public class AVSSequenceCodec extends AVSFieldCodec {

    final private AVSFieldType valuesType;
    final private AVSFieldCodec valuesCodec;

    AVSSequenceCodec(AVSFieldType valuesType, AVSFieldCodec valuesCodec) {
        this.valuesType = valuesType;
        this.valuesCodec = valuesCodec;
    }

    public AVSSequence decode(BERDecoder decodeBuffer) throws ASN1Exception {
        Vector<AVSValue> values = new Vector<AVSValue>();

        int length = decodeBuffer.decodeLength();
        if (length == BERDecoder.INDEFINITE_LENGTH) {
            try {
                while (true) {
                    if (decodeBuffer.bytesAvailable() < 2)// there must be at least two bytes available for end of contents TL
                        throw new ASN1Exception("DecodeBufferUnderflow");

                    ASN1Tag tag = decodeBuffer.decodeTag();

                    //check if we had 0000 TL that indicates end of contents for undefined length 
                    if (tag.getTagClass() == ASN1TagClass.UNIVERSAL && tag.getTagForm() == ASN1TagForm.PRIMITIVE && tag.getTagId() == 0) {
                        if (decodeBuffer.decodeLength() == 0) {
                            break;// end of list of values
                        } else {
                            throw new ASN1Exception("unexpected length value !=0 after end of contents tag");
                        }
                    }

                    values.add(valuesCodec.decode(decodeBuffer));

                }

            } catch (ASN1Exception e) {
                values.clear();
                throw e;
            }
        } else
            throw new ASN1Exception("definite length sequence encoding not supported"); // Sequence must be indefinite length
        return new AVSSequence(values, valuesType);
    }

    /*!
     * @see com.openet.avsasn.codec.AVSFieldCodec#encode(com.openet.avsasn.primitivetypes.AVSValue, com.openet.avsasn.ber.BEREncoder)
     * Encodes a sequence of primitives one by one.
     */
    @Override
    public ByteBuffer encode(AVSValue value, BEREncoder encoder) throws ASN1Exception {
        // Encoding variable length, since we can't predict the length of all
        // values in sequence. 
        encoder.encodeVarLength();
        AVSSequence seq  = (AVSSequence) value;
        AVSValue[] values = seq.getValue();
        for (int idx = 0; idx < values.length; idx++) {
            encoder.encodeTag(
                    BEREncoder.ASN1.Class.CONTEXT,
                    BEREncoder.ASN1.Tag.TAG_ZERO,
                    BEREncoder.ASN1.Primitive.PRIMITIVE);
            // But valuesCodec.encode() will encode length tag for each value
            valuesCodec.encode(values[idx], encoder);
        }
        // Used to mark end-of-sequence
        encoder.encodeVarTrail();
        return encoder.getWriteBuffer();
    }

}
