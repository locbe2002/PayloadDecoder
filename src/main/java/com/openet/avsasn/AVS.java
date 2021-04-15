package avsasn;

import java.util.TreeMap;

import avsasn.primitivetypes.AVSValue;

//TODO add AVS structure checks for attribute setters and getters 
//so if invalid tag/attribute name is passed it will throw InvalidAttributeIdException
//keep in mind that field of reference type can hold specified AVS type or some type that
//extend it so maybe it is not good idea after all ????
public class AVS {

    private AVSType type;

    //this tag, value map
    private TreeMap<Integer, AVSValue> fieldValues;

    public AVS(final AVSType type) {
        this.type = type;
        fieldValues = new TreeMap<Integer, AVSValue>();
    }

    public void setFieldValue(int index, AVSValue value) {
        fieldValues.put(Integer.valueOf(index), value);
    }

    public void setFieldValue(String attributeName, AVSValue value) {
        fieldValues.put(Integer.valueOf(type.getFieldTag(attributeName)), value);
    }

    public AVSValue getFieldValue(int tag) {
        return fieldValues.get(Integer.valueOf(tag));
    }

    public AVSValue getFieldValue(String attributeName) {
        return getFieldValue(type.getFieldTag(attributeName));
    }

    public AVSType getAVSType() {
        return type;
    }

    public String toString() {
        return avsToString(this, false, 0);
    }

    static private final int INDENT_SIZE = 4;

    private static String getPadding(int level) {
        int length = level * INDENT_SIZE;
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            buffer.append(' ');
        return buffer.toString();
    }

    //I know it's messy but just for now
    //printNullValues flag controls if result should contain empty fields i.e. fields that 
    //were not set this information is taken from AVSTypeDoc
    private String avsToString(final AVS avs, boolean printNullValues, int level) {

        StringBuilder buffer = new StringBuilder();

        buffer.append(String.format("%sAVSRecord (type=%s) {\n", AVS.getPadding(level), avs.getAVSType().getName()));

        for (AVSFieldInfo fieldInfo : avs.getAVSType().getAllAVSFieldInfo()) {

            AVSValue value = avs.getFieldValue(fieldInfo.getId());

            if (value != null || printNullValues) {
                if (fieldInfo.getType() == AVSFieldType.REFERENCE) {
                    if (fieldInfo.isMultiValued()) {
                        if (value != null) {
                            AVSValue[] avses = (AVSValue[]) value.getValue();
                            buffer.append(String.format("%s%s(%s_seq) -> {\n", getPadding(level + 1), fieldInfo.getId(), fieldInfo.getReftype()));
                            for (int i = 0; i < avses.length; i++)
                                buffer.append(avsToString((AVS) avses[i].getValue(), printNullValues, level + 2));

                            buffer.append(String.format("%s}\n", getPadding(level + 1)));

                        } else {
                            buffer.append(String.format("%s%s(%s_seq) -> --NOT SET--\n", getPadding(level + 1), fieldInfo.getId(), fieldInfo.getReftype()));
                        }
                    } else {
                        if (value != null) {
                            buffer.append(String.format("%s%s(%s) -> {\n", getPadding(level + 1), fieldInfo.getId(), fieldInfo.getReftype()));
                            buffer.append(avsToString((AVS) value.getValue(), printNullValues, level + 2));
                            buffer.append(String.format("%s}\n", getPadding(level + 1)));
                        } else {
                            buffer.append(String.format("%s%s(%s) -> --NOT SET--\n", getPadding(level + 1), fieldInfo.getId(), fieldInfo.getReftype()));
                        }
                    }
                } else {
                    buffer.append(String.format("%s%s(%s%s) -> %s\n", getPadding(level + 1), fieldInfo.getId(), fieldInfo.getType().getName(), fieldInfo
                            .isMultiValued() ? "_seq" : "", value != null ? value.toString() : "--NOT SET--"));
                }
            }

        }

        buffer.append(String.format("%s}\n", getPadding(level), avs.getAVSType().getName()));
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldValues == null) ? 0 : fieldValues.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        AVS other = (AVS) obj;
        if (fieldValues == null) {
            if (other.fieldValues != null)
                return false;
        } else if (!fieldValues.equals(other.fieldValues))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
