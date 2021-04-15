package avsasn.ber;

//XXX probably move back to int constants in ASN1Tag class
//Make it enumeration ??? anyways comparing with == will work since 
//there are only four instances for application and can't be created
final public class ASN1TagClass {

    // Tag class constants note only UNIVERSAL and CONTEXT are in use
    //APPLICATION and PRIVATE shouldn't be in use since 1994 
    //    private static final short UNIVERSAL_BITS = 0x00;   // 0 << 7 | 0 << 6
    //    private static final short APPLICATION_BITS = 0x40; // 0 << 7 | 1 << 6
    //    private static final short CONTEXT_BITS = 0x80;     // 1 << 7 | 0 << 6
    //    private static final short PRIVATE_BITS = 0xC0;     // 1 << 7 | 1 << 6

    //public API
    final static public ASN1TagClass UNIVERSAL = new ASN1TagClass(0x00,"UNIVERSAL");
    final static public ASN1TagClass APPLICATION = new ASN1TagClass(0x40,"APPLICATION");
    final static public ASN1TagClass CONTEXT = new ASN1TagClass(0x80,"CONTEXT");
    final static public ASN1TagClass PRIVATE = new ASN1TagClass(0xC0,"PRIVATE");

    final public static ASN1TagClass decode(short octet) {
        octet &= TAG_CLASS_BIT_MASK;

        if (octet == CONTEXT.bits)
            return CONTEXT;

        if (octet == UNIVERSAL.bits)
            return UNIVERSAL;

        if (octet == APPLICATION.bits)
            return APPLICATION;

        if (octet == PRIVATE.bits)
            return PRIVATE;

        return null;// not possible but keeps compiler happy
    }

    //private API 
    final private static short TAG_CLASS_BIT_MASK = 0xC0; // 1 << 7 | 1 << 6 yes the same as PRIVATE_BITS
    final private short bits;

    private ASN1TagClass(int bits, String name) {
        this.bits = (short) bits;
        this.name = name;
    }
   
    private String name;
    
    public String toString(){
        return name;
    }
    
    public short bits() {
    	return this.bits;
    }
}
