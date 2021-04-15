package avsasn.primitivetypes;

import avsasn.AVSFieldType;

final public class AVSString extends AVSValue implements Comparable<AVSString>{
    
    private String value;
   
    public String getValue() {
        return value;
    }

    public AVSString(String val){
        super(AVSFieldType.STRING);
        if(val==null){
            throw new NullPointerException("Value can't be null");
        }
        value=val;
    }

    public String toString() {
    	//scan string for non ASCII printable chars if found
    	//return replaced string if not return value
    	for(int i = 0; i < value.length(); i++){
    		char ch = value.charAt(i);
    		if(ch < 32 || ch > 126){
    			return escapeString(value,i);
    		}
    	}
    	return value;
    }

    //returns escaped string . escaping starts from escapeStartIndex 
    private String escapeString(String value, int escapeStartIndex) {
    	int valueLength = value.length();
    	StringBuilder sb = new StringBuilder(valueLength);
    	sb.append(value.substring(0,escapeStartIndex));
    	for(int i = escapeStartIndex; i < valueLength; i++){
    		char ch = value.charAt(i);
    		if(ch >= 32 && ch <= 126){
    			sb.append(ch);
    		}else{
    			sb.append(".");
    		}
    	}
    	return sb.toString();
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
        AVSString other = (AVSString) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public int compareTo(AVSString o) {
        return getValue().compareTo(o.getValue());
    }

}
