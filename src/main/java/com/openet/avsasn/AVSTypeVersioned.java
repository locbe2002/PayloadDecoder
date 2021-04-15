package avsasn;

import java.util.Collections;
import java.util.Comparator;

import cfg.ConfigAttribute;
import cfg.ConfigType;
import cfg.ConfigType.Version;

public class AVSTypeVersioned extends AVSType {

    java.util.Vector<Version> versions;
    
    // This ctor should be calles only by AVSTypeRepository
    AVSTypeVersioned(final AVSType parentType, final String name) {
        super(parentType, name);
    }
    
    @Override
    public boolean isVersioned() {
        return true;
    }
    
    @Override
    public int getLatestVersion() {
        return versions.size();
    }
    
    void setChecksum(ConfigType configType) {
        this.versions = configType.getVersions();
        sortFields();        
    }
    
    /* (non-Javadoc)
     * @see com.openet.avsasn.AVSType#getChecksum(int)
     */
    @Override
    public int getChecksum(int version) {
        if (versions != null && version > 0 && version <= versions.size())
            return versions.get(version - 1).getChecksum().getValue();
        else // return invalid value 
            return Integer.MIN_VALUE;
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
        } else if (fieldTypeString.equals(ConfigType.ADDRESS)) {
            fieldType = AVSFieldType.ADDRESS;
        } else {
            // treat everything else as a String 
            // if(attributeType.equals(ConfigType.STRING)){
            fieldType = AVSFieldType.STRING;
        }

        fields.add(new AVSFieldInfo(field.getName(), 
                fieldType,
                field.getAvsVersion(),
                field.getIndex(),
                field.getDefaultValue() == null ? null : field.getDefaultValue()[0].getData(), 
                field.getComment(),
                field.isMultiValued(),
                field.getRefType()));
        
        for (int i = 0; i < fields.size(); i++) {
            AVSFieldInfo fi = fields.elementAt(i);
            fi.setTagId(fi.getIndex() + offset);
            nameToIndex.put(fi.getId(), fi.getIndex());
        }
        stringRepresentation = null;
    }
    
    void sortFields() {
        Collections.sort(fields,new Comparator<AVSFieldInfo>() {
            @Override
            public int compare(AVSFieldInfo o1, AVSFieldInfo o2) {
                if (o1.getIndex() == o2.getIndex()) 
                    return 0;
                else if (o1.getIndex() > o2.getIndex()) {
                    return 1;
               } else {
                   return -1;
               }
            }
        });        
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
                buffer.append(String.format("\n    field[%d]: version=%d, tag=%s, index=%d, name=%s, type=%s, reftype=%s, multiValued=%b", i, field.getVersion(), field.getTagId(), field.getIndex(), field.getId(), field.getType().getName(), field.getReftype(),
                        field.isMultiValued()));
            } else {
                buffer.append(String.format("\n    field[%d]: version=%d, tag=%s, index=%d, name=%s, type=%s, multiValued=%b", i, field.getVersion(), field.getTagId(), field.getIndex(), field.getId(), field.getType().getName(), field.isMultiValued()));
            }
        }

        buffer.append("\n}\n");
        stringRepresentation = buffer.toString();
    }
}
