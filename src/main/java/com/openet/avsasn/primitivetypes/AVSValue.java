package avsasn.primitivetypes;


import avsasn.AVSFieldType;

//subclasses of this class are meant to be very thin wrappers
//around some Java types this will allow to change actual Java
//storage type for avs value without changing lot's of code
public abstract class AVSValue {

    private final AVSFieldType valueType;
    
    protected AVSValue(AVSFieldType valueType){
        this.valueType = valueType;
    }

    //this two are in AVSType/AVSFieldInfo don't know if it makes sense to have them here
    public AVSFieldType getType(){
        return valueType;
    }
    
    public String getTypeName(){
        return valueType.getName();
    }
    
    //get actual value
    public abstract Object getValue();

    //force meaningful toString implementation, I could call this method
    //getValueAsString but don't like cluttered interfaces 
    abstract public String toString();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((valueType == null) ? 0 : valueType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AVSValue other = (AVSValue) obj;
        if (valueType == null) {
            if (other.valueType != null)
                return false;
        } else if (!valueType.equals(other.valueType))
            return false;
        return true;
    }

  
   
}
