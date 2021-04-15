package avsasn.primitivetypes;

import avsasn.AVSFieldType;

final public class AVSAddress extends AVSValue implements Comparable<AVSAddress> {
    
    private String value;
    private short[] addr;

    public String getValue() {
        return value;
    }
    
    public short[] getRawValue() {
        return addr;
    }

    public AVSAddress(short bytes[]) {
        super(AVSFieldType.ADDRESS);
        if(bytes.length == 16){
            value = String.format("%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
        }else{
            value = String.format("%d.%d.%d.%d", bytes[0], bytes[1], bytes[2], bytes[3]);
        }
        addr = bytes.clone();
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
        AVSAddress other = (AVSAddress) obj;
        if (addr == null) {
            if (other.addr != null)
                return false;
        } else if (addr.length != other.addr.length){
            return false;
        }else{
            for(int i=0;i<addr.length;++i){
                if (addr[i]!=other.addr[i])
                    return false;
            }
        }
        return true;
    }

    public int compareTo(AVSAddress o) {
        int result = 0;
        if (addr.length != o.addr.length){
            return addr.length < o.addr.length?-1:1;
        }
        
        for (int i = 0; i < addr.length; ++i) {
            result = addr[i] < o.addr[i] ? -1 : (addr[i] == o.addr[i] ? 0 : 1);
            if (result != 0) {
                return result;
            }
        }

        return result;
    }

}
