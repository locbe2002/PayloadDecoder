package avsasn.primitivetypes;

//import java.net.Inet4Address;
import avsasn.AVSFieldType;

//XXX no depending how we will use this class
// the value will change right now stored it as string in decimal dot notation
final public class AVSIPv4Addr extends AVSValue implements Comparable<AVSIPv4Addr> {

    //private Inet4Address value;
    private String value;
    private short[] ipv4;

    public String getValue() {
        return value;
    }
    
    public short[] getRawValue() {
        return ipv4;
    }

    public AVSIPv4Addr(short b1, short b2, short b3, short b4) {
        super(AVSFieldType.IPV4ADDR);

        value = String.format("%d.%d.%d.%d", b1, b2, b3, b4);
        ipv4= new short[4];
        ipv4[0] = b1;
        ipv4[1] = b2;
        ipv4[2] = b3;
        ipv4[3] = b4;

    }

    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AVSIPv4Addr other = (AVSIPv4Addr) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public int compareTo(AVSIPv4Addr o) {
        int result =0;
        for (int i = 0; i < 4; i++) {
            result = ipv4[i] < o.ipv4[i] ? -1 : (ipv4[i] == o.ipv4[i] ? 0 : 1);
            if (result != 0) {
                return result;
            }
        }

        return result;
    }

}
