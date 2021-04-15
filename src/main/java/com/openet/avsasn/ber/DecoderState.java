package avsasn.ber;

public enum DecoderState {
    START,
    END,
    TAG_DECODED,
    LENGTH_DECODED,
    VALUE_DECODED,
    ERROR;
}
