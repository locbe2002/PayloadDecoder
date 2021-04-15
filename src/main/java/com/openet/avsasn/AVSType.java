package avsasn;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.Vector;
import cfg.ConfigAttribute;
import cfg.ConfigType;

//XXX make this interface and hide implementation inside AVSRecordTypeRepo
//XXX Instances of this class are compared by type name this is ok as long as
//in one application we won't deal with different versions of AVS type note 
//this is very unlikely or constraint about avs type name uniqueness will change

public abstract class AVSType {

    static public AVSType Get(String name) throws Exception{
        if(!AVSTypeRepository.getInstance().isInitialized())
            throw new IllegalStateException("AVSTypeRepository is not initialized");
        
        AVSType avsType = AVSTypeRepository.getInstance().get(name);
        //avsType.dump();
        return avsType;

    } // Get the description of a named AVSRecord type.

    static public AVSType create(final AVSType superType, final String name, boolean isVersioned) {
        AVSType avsType = null;
        String superTypeName = (superType == null ? "" : superType.getName());
        if (isVersioned) {
            avsType = new AVSTypeVersioned(superType, name);
        } else {
            avsType = new AVSTypeUnversioned(superType, name);
        }
        return avsType; 
    }

    protected String name; // Human readable name.
    protected AVSType parentType; // The AVSRecordType of which we are a
    protected int offset; // Offset within an AVSRecord where this classes fields start.
    protected Vector<AVSFieldInfo> fields; // [0 .. fieldCount] Fields and their types.
    protected TreeMap<String, Integer> nameToIndex; // this is mapping field name to index in fields vector 
//    protected byte[] typeChecksum;
    protected byte[] checksum;

    //just toString cash  
    protected String stringRepresentation;

    // This ctor should be calles only by AVSTypeRepository
    AVSType(final AVSType parentType, final String name) {
        this.parentType = parentType;
        this.name = name;

        fields = new Vector<AVSFieldInfo>();
        nameToIndex = new TreeMap<String, Integer>();

        offset = parentType != null ? parentType.offset + parentType.fields.size() : 0;
        stringRepresentation = null;
    }
    
    public abstract boolean isVersioned();

    public abstract int getLatestVersion();
    
    abstract void setChecksum(ConfigType configType);
    
    /**
     * Get unversioned avs checksum.
     * @return checksum or null for versioned avs
     */
    public byte[] getChecksum() {
        return null;
    }
    
    /**
     * Get versioned avs checksum. 
     * @param version valid in case of versioned avs
     * @return checksum
     */
    abstract public int getChecksum(int version);
    
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

    abstract void addField(ConfigAttribute field);

    protected int getLocalIndex(String name) {
        Integer index = nameToIndex.get(name);
        return index == null ? -1 : index.intValue();
    }

    abstract protected void createString();

    // forbid others create AVSRecordType
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    //public API
    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public int getNumberOfFields() {
        return offset + fields.size();
    }

    public AVSType getParentType() {
        return parentType;
    }

    //TODO this method will be called often by EDCGui so should be optimised
    public boolean extendsAVSType(AVSType avsType){
        if(getName().equals(avsType.getName()))
            return true;
        
        if(parentType!=null)
            return parentType.extendsAVSType(avsType);
        
        return false;
    }
    
    
    //TODO this method will be called often by EDCGui so should be optimised
    public Vector<AVSFieldInfo> getAllAVSFieldInfo() {
        Vector<AVSFieldInfo> fieldsInfo = new Vector<AVSFieldInfo>(getNumberOfFields());
        if (parentType != null) {
            fieldsInfo.addAll(parentType.getAllAVSFieldInfo());
        }
        fieldsInfo.addAll(fields);
        return fieldsInfo;
    }
    
    public Vector<AVSFieldInfo> getLocalAVSFieldInfo() {
        return fields;
    }

    
    //TODO this method will be called often by EDCGui so should be optimised
    //this method returns fields filtered by field type
    public Vector<AVSFieldInfo> getAVSFieldInfo(AVSFieldType fieldType){
        Vector<AVSFieldInfo> fieldsInfo = new Vector<AVSFieldInfo>();
        if (parentType != null) {
            fieldsInfo.addAll(parentType.getAVSFieldInfo(fieldType));
        }
        
        for (AVSFieldInfo fieldInfo : fields) {
            if(fieldInfo.getType()==fieldType)
                fieldsInfo.add(fieldInfo);    
        }
        
        return fieldsInfo;
    }
    
    public AVSFieldInfo getAVSFieldInfo(int index) {
        return fields.get(index);
    }


    public String getFieldName(int tagId) {
        if (tagId < offset) {
            if (parentType == null)
                throw new NoSuchElementException();
            else
                return parentType.getFieldName(tagId);
        }

        if (tagId > offset + fields.size()) { //XXX fix me
            throw new NoSuchFieldError();
        }

        return fields.elementAt(tagId - offset).getId();
    }

    public int getFieldTag(String name) throws NoSuchElementException {
        int index = getLocalIndex(name);
        if (index < 0) {
            if (parentType == null)
                throw new NoSuchElementException();
            else
                return parentType.getFieldTag(name);
        }

        return index + offset;
    }

    //this probably should be splitted to getFieldType isFieldMultiValued getCodec etc
    public AVSFieldInfo getFieldInfo(String name) {
        int index = getLocalIndex(name);
        if (index < 0) {
            if (parentType == null)
                throw new NoSuchElementException();
            else
                return parentType.getFieldInfo(name);
        }

        return fields.elementAt(index);
    }

    //this probably should be splitted to getFieldType isFieldMultiValued getCodec etc
    public AVSFieldInfo getFieldInfo(int tagId) {
        if (tagId < offset) {
            if (parentType == null)
                throw new NoSuchElementException();
            else
                return parentType.getFieldInfo(tagId);
        }

        if (tagId > offset + fields.size()) { //XXX fix me
            throw new NoSuchFieldError();
        }

        return fields.elementAt(tagId - offset);
    }

    public String toString() {
        if (stringRepresentation == null)
            createString();

        return stringRepresentation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        AVSType other = (AVSType) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    // subclass.
    //    public List<Object> constraintsPostFixList; // The post fix constraint list
    //                                                // for this type
    //    public List<Object> fullConstraintsPostFixList; // The post fix constraint
    //                                                    // list for this type (and
    //                                                    // all its super types) -
    //                                                    // Optimization
    //
    //    public boolean constraintsPostProcessed; // Is the list ready for use? - Has
    //                                             // it being post processed
    //    public boolean constraintsCombined; // Have the constraints been combined
    // into one list
}
