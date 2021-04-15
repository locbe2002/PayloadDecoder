package cfg;
import java.lang.*;
import java.util.*;

public class ORStringPK implements ORPK {
    private String stringValue;

    public ORStringPK(String val) {
        stringValue = val;
    }

    public String toString() {
        return stringValue;
    }
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object other) {
        return (other != null && other instanceof ORPK && other.toString().equals(toString()));
    }
}
