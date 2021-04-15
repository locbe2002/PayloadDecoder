package avsasn.primitivetypes;

import avsasn.AVSFieldType;

final public class AVSIPAddr extends AVSValue implements Comparable<AVSIPAddr> {
    
    //private Inet6Address value;
    private String value;
    private short[] ip;

    public String getValue() {
        return value;
    }
    
    public short[] getRawValue() {
        return ip;
    }

    public AVSIPAddr(short bytes[]) {
        super(AVSFieldType.IPADDR);
        if(bytes.length == 16){
            value = String.format("%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
        }else{
            value = String.format("%d.%d.%d.%d", bytes[0], bytes[1], bytes[2], bytes[3]);
        }
        ip = bytes.clone();
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
        AVSIPAddr other = (AVSIPAddr) obj;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (ip.length != other.ip.length){
            return false;
        }else{
            for(int i=0;i<ip.length;++i){
                if (ip[i]!=other.ip[i])
                    return false;
            }
        }
        return true;
    }

    public int compareTo(AVSIPAddr o) {
        int result = 0;
        if (ip.length != o.ip.length){
            return ip.length < o.ip.length?-1:1;
        }
        
        for (int i = 0; i < ip.length; ++i) {
            result = ip[i] < o.ip[i] ? -1 : (ip[i] == o.ip[i] ? 0 : 1);
            if (result != 0) {
                return result;
            }
        }

        return result;
    }

}
