package avsasn.primitivetypes;

import java.math.BigInteger;

import avsasn.AVSFieldType;

final public class AVSInteger extends AVSValue implements Comparable<AVSInteger> {

    private BigInteger value;

    public AVSInteger(BigInteger val) {
        super(AVSFieldType.INTEGER);
        if(val==null){
            throw new NullPointerException("Value can't be null");
        }
        value = val;
    }

    //    
    //    public AVSInteger(long val){
    //        value = BigInteger.valueOf(val);
    //    }

    public BigInteger getValue() {
        return value;
    }

    public String toString() {
        return value.toString(10);
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
        AVSInteger other = (AVSInteger) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public int compareTo(AVSInteger o) {
        return getValue().compareTo(o.getValue());
    }

}
