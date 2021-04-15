package cfg;
import java.util.*;
import java.lang.*;
import java.io.*;
import cfg.ORPK;
/**
 * A single value of an attribute in a {@link ConfigObject}
 */
public final class Value {
    
    public enum ValueType { DATA, REF, PARAM, UNKNOWN };
    
    /** The textual value */
    private String data;

    /** An object that this value references */
    private ORPK ref;
    
    /** The parameter this value represents **/
    private Parameter parameter;

    private ValueType t = ValueType.UNKNOWN;
    
    public Value() {
        // A null value has neither data nor reference nor parameter
    }

    public Value(String d) {
        setData(d);
    }

    public Value(ORPK r) {
        setRef(r);
    }
    
    public Value(Parameter p) {
        setParameter(p);
    }

    public final String getData() {
        if(getValueType().equals(ValueType.DATA)) {
            return data;
        } else if (getValueType().equals(ValueType.PARAM))
        {
            return getParameter().getValue();
        }
        return null;
    }
    
    public void setData(String data) {
        setValueType(ValueType.DATA);
        this.data = data;
    }
    
    public final ORPK getRef() {
        return ref;
    }
    
    public void setRef(ORPK ref) {
        setValueType(ValueType.REF);
        this.ref = ref;
    }
    
    public Parameter getParameter() {
        return parameter;
    }
    
    public void setParameter(Parameter parameter) {
        setValueType(ValueType.PARAM);
        this.parameter = parameter;
    }

    public ValueType getValueType() {
        return t;
    }
    
    public boolean isRef() {
    	return ValueType.REF.equals(t);
    }

    private void setValueType(ValueType t) {
        this.t = t;
    }

    /**
     * Returns true if two Value object are equal
     * 
     * Two values are equal if either the two references are equal, or if both references are null, then the two data elements are
     * equal
     */
    public final boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Value)) {
            return false;
        }
        Value val = (Value) o;

        //if both types are UNKNOWN
        if(getValueType().equals(ValueType.UNKNOWN) && val.getValueType().equals(ValueType.UNKNOWN))
            return true;
        
        //if both types are DATA
        if(getValueType().equals(ValueType.DATA) && val.getValueType().equals(ValueType.DATA))
            return ((getData() == null) ? (val.getData() == null) : getData().equals(val.getData()));

        //if both types are REF
        if(getValueType().equals(ValueType.REF) && val.getValueType().equals(ValueType.REF))
            return ((getRef() == null) ? false : getRef().equals(val.getRef()));
        
        //if both types are PARAM
        if(getValueType().equals(ValueType.PARAM) && val.getValueType().equals(ValueType.PARAM))
            return ((getParameter() == null) ? false : getParameter().equals(val.getParameter()));
        
        return false;
    }

    /**
     * Returns true if two arrays of Value objects are equal
     * 
     */
    public static final boolean equalValues(Value[] v1, Value[] v2) {
        if (v1 == null) {
            return v2 == null;
        } else if (v2 == null) {
            return v1 == null;
        } else if (v1.length != v2.length) {
            return false;
        } else if (v1.length == 0) {
            return true;
        }
        boolean allEqual = true;
        for (int i = 0; i < v1.length; ++i) {
            allEqual &= v1[i].equals(v2[i]);
        }
        return allEqual;
    }

    public final int hashCode() {
        if(getValueType().equals(ValueType.DATA)) {
            if (getData() == null) {
                return 0;
            }
            return getData().hashCode();
        }
        
        if(getValueType().equals(ValueType.REF)) {
            if(getRef() == null) {
                return 0;
            }
            return getRef().hashCode();
        }
        
        if(getValueType().equals(ValueType.PARAM)) {
            if(getParameter() == null) {
                return 0;
            }
            return getParameter().hashCode();
        }
        
        return 0;
    }

    /**
     * Returns true is the data elements of two Value objects are equal If either reference is set, then they are consider not equal
     * (i.e. this does not apply to Value object with references)
     */
    public final boolean equalData(Value val) {
        if (val == null)
            return false;
        // Neither reference must be set
        if (getRef() != null || val.getRef() != null)
            return false;
        // Even if the two data items are both null
        // we consider it not to be a match
        if (getData() == null || val.getData() == null)
            return false;
        if (getData().equals(val.getData()))
            return true;

        return false;
    }

    /**
     * A null value is one whose data and value are both unset
     */
    public boolean isNull() {
        return (getData() == null && getRef() == null && getParameter() == null);
    }

    public final String toString() {
        return "[" + (getData() != null ? "\"" + getData() + "\"" : "") + (getRef() != null ? "<" + getRef().toString() + ">" : "") + "]";
    }
}
