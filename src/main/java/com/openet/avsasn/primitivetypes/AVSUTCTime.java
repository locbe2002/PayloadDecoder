package avsasn.primitivetypes;

import java.math.BigInteger;
import java.util.Date;

import avsasn.AVSFieldType;

final public class AVSUTCTime extends AVSValue implements Comparable<AVSUTCTime>{
    
    private Date value;

    public Date getValue() {
        return value;
    }

    public String toString() { //seconds since start of epoch
        return String.valueOf(value.getTime() / 1000);
    }

    //     public AVSUTCTime(long time){
    //         super(AVSFieldType.UTCTIME);
    //        value = new Date(time);
    //    }

    public AVSUTCTime(Date date){
        super(AVSFieldType.UTCTIME);
        if(date==null){
            throw new NullPointerException("Value can't be null");
        }
        value = date;
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
        AVSUTCTime other = (AVSUTCTime) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public int compareTo(AVSUTCTime o) {
        return getValue().compareTo(o.getValue());
    }
    
}
