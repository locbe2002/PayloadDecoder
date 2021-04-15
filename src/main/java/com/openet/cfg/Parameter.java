package cfg;
import java.lang.*;
import java.util.*;
public class Parameter {
    private String value;

    public Parameter() {
    }
    
    public Parameter(String v) {
        value = v;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(null == obj) {
            return false;
        }
        if(! (obj instanceof Parameter)) {
            return false;
        }
        
        return value.equals(((Parameter)obj).getValue());
    }

    @Override
    public int hashCode() {
        if(null == value) {
            return 0;
        }
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
    
    
}
