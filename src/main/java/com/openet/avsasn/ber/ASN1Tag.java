package avsasn.ber;


//It is best to keep this class immutable.So don't break this adding set like methods. 
//                                                                  Thank you!
public class ASN1Tag {

    final private ASN1TagClass tagClass;
    final private ASN1TagForm tagForm;
    final private int tagId;

    //we could cache tags ???
    public ASN1Tag(final ASN1TagClass tagClass, final ASN1TagForm tagForm, final int tagId) {
        this.tagClass=tagClass;
        this.tagForm=tagForm;
        this.tagId=tagId;
    }
    
    //note that it is safe to return ASN1TagClass since this class is also immutable
    public ASN1TagClass getTagClass()throws ASN1Exception{
        return tagClass;
    }
    
    public ASN1TagForm getTagForm()throws ASN1Exception{
        return tagForm;
    }
    
    public int getTagId()throws ASN1Exception{
        return tagId; 
    }
    
    //some constants needs to be moved somewhere else
    static final short PC_MASK = 0x20; //primitive constructed 5th bit mask
    static final short CONTINUE_TAG_BIT = 0x80; //7th bit
    
    /* Universal Tags */
    public final static short TAG_BER_END_OF_CONTENTS = 0x00;    /* ber encoding end of unlimited length field */
    public final static short TAG_INTEGER             = 0x02;    /* Integer */
    public final static short TAG_BIT_STRING          = 0x03;    /* Bit String */
    public final static short TAG_OCTET_STRING        = 0x04;    /* Octet String */
    public final static short TAG_NULL                = 0x05;    /* NULL */
    public final static short TAG_OBJECT_IDENTIFIER = 0x06;    /* Object Identifier */
    public final static short TAG_SEQUENCE          = 0x10;    /* Sequence / Sequence of */
    public final static short TAG_SEQUENCE_OF       = 0x10;    /* Sequence / Sequence of */
    public final static short TAG_SET               = 0x11;    /* Set / Set of */
    public final static short TAG_SET_OF            = 0x11;    /* Set / Set of */
    public final static short TAG_PRINTABLE_STRING  = 0x13;    /* Printable String */
    public final static short TAG_PRINTABLE         = 0x12;    /* Printable String */
    public final static short TAG_IA5STRING         = 0x16;    /* IA5 String */
    public final static short TAG_UTCTIME           = 0x17;    /* UTC Time */

}
