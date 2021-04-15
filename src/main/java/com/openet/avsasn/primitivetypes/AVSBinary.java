package avsasn.primitivetypes;

import java.nio.ByteBuffer;

import avsasn.AVSFieldType;

final public class AVSBinary extends AVSValue {
 
    private ByteBuffer value;

    public AVSBinary(ByteBuffer contents) {
        super(AVSFieldType.BINARY);
        if(contents==null){
            throw new NullPointerException("Value can't be null");
        }
        value = contents;
    }
   
    public byte[] getValue() {
        return value.array();
    }
    
    public String toString() {
        if (value == null)
            return "";

        byte[] rawData = value.array();
        if (rawData.length == 0)
            return "";

        StringBuilder buffer = new StringBuilder(value.array().length / 4 + 1);
        buffer.append(String.format("%02X", rawData[0]));

        for (int i = 1; i < rawData.length; i++) {
            if (i % 16 == 0) {
                buffer.append(String.format("\n%02X", rawData[i]));
                continue;
            }
            if (i % 4 == 0) {
                buffer.append(String.format(" %02X", rawData[i]));
                continue;
            }
            buffer.append(String.format("%02X", rawData[i]));
        }

        return buffer.toString();
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
        AVSBinary other = (AVSBinary) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
    
}
