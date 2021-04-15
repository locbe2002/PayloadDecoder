package avsasn.compoundtypes;

import java.util.Vector;

import avsasn.AVSFieldType;
import avsasn.primitivetypes.AVSBinary;
import avsasn.primitivetypes.AVSBoolean;
import avsasn.primitivetypes.AVSIPv4Addr;
import avsasn.primitivetypes.AVSIPAddr;
import avsasn.primitivetypes.AVSAddress;
import avsasn.primitivetypes.AVSInteger;
import avsasn.primitivetypes.AVSReference;
import avsasn.primitivetypes.AVSString;
import avsasn.primitivetypes.AVSUTCTime;
import avsasn.primitivetypes.AVSValue;

public class AVSSequence extends AVSValue {

    //    
    private Vector<AVSValue> values;

    private String stringValue = null;

    final private AVSFieldType valuesType;

    public AVSSequence(Vector<AVSValue> values, AVSFieldType valuesType) {
        super(valuesType);
        if(values==null){
            throw new NullPointerException("Value can't be null");
        }
        this.values = new Vector<AVSValue>(values.size());
        for (AVSValue element : values) {
            if(element==null){
                throw new NullPointerException("Can't have null elements in sequence"); 
            }
            this.values.add(element);
        }
        this.valuesType = valuesType;

    }

    public int size() {
        return values.size();
    }

    public AVSValue elementAt(int index) {
        return values.elementAt(index);
    }

    public void add(AVSValue element) {
        if(element==null){
            throw new NullPointerException("Can't add null elements to sequence");
        }
            
        stringValue = null;
        values.add(element);
    }

    public AVSValue remove(int index) {
        stringValue = null;
        return values.remove(index);
    }

    //note it is important to return right array type since clients may expect
    //particular array type when they get values type using getElemntsType
    //this will save some Class cast exceptions
    public AVSValue[] getValue() {

        switch (valuesType) {
        case BINARY:
            return values.toArray(new AVSBinary[values.size()]);
        case BOOLEAN:
            return values.toArray(new AVSBoolean[values.size()]);
        case INTEGER:
            return values.toArray(new AVSInteger[values.size()]);
        case IPV4ADDR:
            return values.toArray(new AVSIPv4Addr[values.size()]);
        case IPADDR:
            return values.toArray(new AVSIPAddr[values.size()]);
        case ADDRESS:
            return values.toArray(new AVSAddress[values.size()]);
        case REFERENCE:
            return values.toArray(new AVSReference[values.size()]);
        case STRING:
            return values.toArray(new AVSString[values.size()]);
        case UTCTIME:
            return values.toArray(new AVSUTCTime[values.size()]);
        }
        //won't happen but keeps compiler happy
        throw new RuntimeException("Unknown AVSFieldType: " + valuesType.getName());
    }

    private void createString() {

        switch (values.size()) {
        case 0:
            stringValue = "[]";
            break;
        case 1:
            stringValue = "[" + values.elementAt(0).toString() + "]";
            break;
        default:
            StringBuilder buffer = new StringBuilder();
            buffer.append("[" + values.elementAt(0).toString());
            for (int i = 1; i < values.size(); i++) {
                buffer.append(" ");
                buffer.append(values.elementAt(i).toString());
            }
            buffer.append("]");
            stringValue = buffer.toString();
        }
    }

    public String toString() {
        if (stringValue == null)
            createString();

        return stringValue;
    }

    public AVSFieldType getElemntsType() {
        return valuesType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        result = prime * result + ((valuesType == null) ? 0 : valuesType.hashCode());
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
        AVSSequence other = (AVSSequence) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        if (valuesType == null) {
            if (other.valuesType != null)
                return false;
        } else if (!valuesType.equals(other.valuesType))
            return false;
        return true;
    }

}
