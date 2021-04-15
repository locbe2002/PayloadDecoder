package avsasn.primitivetypes;

import avsasn.AVS;
import avsasn.AVSFieldType;
import avsasn.AVSType;

final public class AVSReference extends AVSValue {
    
    private AVS value;
    
    public AVS getValue() {
        return value;
    }

    public String toString() {
        return value.toString();
    }

    public String getAVSTypeName(){
        return value.getAVSType().getName();
    }
   
    public AVSType getAVSType(){
        return value.getAVSType();
    }
    
    public AVSReference(AVS avs) {//should we make copy here ???
        super(AVSFieldType.REFERENCE);
        if(avs==null){
            throw new NullPointerException("Value can't be null");
        }

        value = avs;
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
        AVSReference other = (AVSReference) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }


}
