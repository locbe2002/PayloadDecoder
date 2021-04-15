package avsasn;

import java.util.Collections;
import cfg.ConfigAttribute;
import cfg.ConfigType;

//XXX make this interface and hide implementation inside AVSRecordTypeRepo
//XXX Instances of this class are compared by type name this is ok as long as
//in one application we won't deal with different versions of AVS type note 
//this is very unlikely or constraint about avs type name uniqueness will change

public class AVSTypeUnversioned extends AVSType {

//    private byte[] typeChecksum;
    
    // This ctor should be calles only by AVSTypeRepository
    AVSTypeUnversioned(final AVSType parentType, final String name) {
        super(parentType, name);
    }
    
    @Override
    public boolean isVersioned() {
        return false;
    }
    
    @Override
    public int getLatestVersion() {
        return 0;
    }
    
    @Override
    void setChecksum(ConfigType configType) {
        this.checksum = configType.getChecksumAsByteArray();

    }    

    /* (non-Javadoc)
     * @see com.openet.avsasn.AVSType#getChecksum()
     */
    public byte[] getChecksum() {
        return checksum;
    }
    
    
    /* (non-Javadoc)
     * @see com.openet.avsasn.AVSType#getChecksum(int)
     * This method should never be used for unversioned avs that is why it returns invalid checksum value. 
     * @param version parameter is valid only in case of versioned AVS
     * @return invalid checksum value
     */
    public int getChecksum(int version) {
        return Integer.MIN_VALUE;
    }
    
    //this method is meant only to be called by AVSTypeRepository
    //it adds field definition and then resets tagIds accordingly
    //this implementation is very suboptimal note that all computation of tag could be done after last
    //field is added but building of AVSType is done for each type once per application lifecycle 
    //so shouldn't be a big performance problem
    //note that it is ok not to reset nameToIndex map the put method will replace old value
    void addField(String id, AVSFieldType type, boolean multiValued, String reftype) {
        fields.add(new AVSFieldInfo(id, type, multiValued, reftype));
        Collections.sort(fields);
        for (int i = 0; i < fields.size(); i++) {
            fields.elementAt(i).setTagId(i + offset);
            nameToIndex.put(fields.elementAt(i).getId(), Integer.valueOf(i));//could use autoboxing but this yield much better performance
        }
        stringRepresentation = null;
    }
    
    /* (non-Javadoc)
     * @see com.openet.avsasn.AVSType#addField(com.openet.cfg.ConfigAttribute)
     */
    @Override
    void addField(ConfigAttribute field) {
        AVSFieldType fieldType;
        String fieldTypeString = field.getType();
        if (fieldTypeString.equals(ConfigType.BOOLEAN)) {
            fieldType = AVSFieldType.BOOLEAN;
        } else if (fieldTypeString.equals(ConfigType.INTEGER)) {
            fieldType = AVSFieldType.INTEGER;
        } else if (fieldTypeString.equals(ConfigType.IPV4ADDR)) {
            fieldType = AVSFieldType.IPV4ADDR;
        } else if (fieldTypeString.equals(ConfigType.IPADDR)) {
            fieldType = AVSFieldType.IPADDR;
        } else if (fieldTypeString.equals(ConfigType.ADDRESS)) {
            fieldType = AVSFieldType.ADDRESS;
        } else if (fieldTypeString.equals(ConfigType.BINARY)) {
            fieldType = AVSFieldType.BINARY;
        } else if (fieldTypeString.equals(ConfigType.REFTYPE)) {
            fieldType = AVSFieldType.REFERENCE;
        } else if (fieldTypeString.equals(ConfigType.UTCTIME)) {
            fieldType = AVSFieldType.UTCTIME;
        } else {
            // treat everything else as a String 
            // if(attributeType.equals(ConfigType.STRING)){
            fieldType = AVSFieldType.STRING;
        }

        fields.add(new AVSFieldInfo(field.getName(), fieldType, field.isMultiValued(),field.getRefType()));
        Collections.sort(fields);
        for (int i = 0; i < fields.size(); i++) {
            fields.elementAt(i).setTagId(i + offset);
            nameToIndex.put(fields.elementAt(i).getId(), Integer.valueOf(i));//could use autoboxing but this yield much better performance
        }
        stringRepresentation = null;
    }

    protected void createString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("AVSType {");
        buffer.append("\n    name         = ");
        buffer.append(name);

        buffer.append("\n    offset       = ");
        buffer.append(offset);

        buffer.append("\n    fields count = ");
        buffer.append(getNumberOfFields());

        buffer.append("\n    parentType   = ");
        buffer.append(parentType != null ? parentType.name : "null");

        buffer.append("\n    parentOffset = ");
        buffer.append(parentType != null ? parentType.getOffset() : "null");

        for (int i = 0; i < fields.size(); i++) {
            AVSFieldInfo field = fields.elementAt(i);
            if (fields.elementAt(i).getType() == AVSFieldType.REFERENCE) {
                buffer.append(String.format("\n    field[%d]: tag=%s, name=%s, type=%s, reftype=%s, multiValued=%b", i, field.getTagId(), field.getId(), field
                        .getType().getName(), field.getReftype(), field.isMultiValued()));
            } else {
                buffer.append(String.format("\n    field[%d]: tag=%s, name=%s, type=%s, multiValued=%b", i, field.getTagId(), field.getId(), field.getType()
                        .getName(), field.isMultiValued()));
            }
        }

        buffer.append("\n}\n");
        stringRepresentation = buffer.toString();

    }
}
