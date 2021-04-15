package avsasn.primitivetypes;

import avsasn.AVSFieldType;

final public class AVSBoolean extends AVSValue implements Comparable<AVSBoolean>{
    
    private Boolean value;
    
    public Boolean getValue() {
        return value;
    }
   
    public String toString() {
        return value.toString();
    }
    
//    public AVSBoolean(boolean b) {
//        super(AVSFieldType.BOOLEAN);
//        value = new Boolean(b);
//    }
    
    public AVSBoolean(Boolean b) {
        super(AVSFieldType.BOOLEAN);
        if(b==null){
            throw new NullPointerException("Value can't be null");
        }
        value=b;
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
        AVSBoolean other = (AVSBoolean) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

   
    public int compareTo(AVSBoolean o) {
        return getValue().compareTo(o.getValue());
    }
    
}
