package avsasn;

import avsasn.codec.AVSCodec;
import avsasn.codec.AVSFieldCodec;

//TODO this class probably should be hidden and data accessed only via AVSType
public class AVSFieldInfo implements Comparable<AVSFieldInfo> {

    //name of avs attribute field element id attribute in avs xml typedoc 
    private String id;

    private AVSFieldType type;

    private boolean multiValued;

    private String reftype;

    //  //field asn tag 
    private int tagId;

    private AVSFieldCodec codec;

    //used for AVSField comparison
    private long hash;

    private boolean hasDefaultValue = false;
    
    private int index = -1;
    
    private int avs_version = -1;
    
    private String comment;
    
    private String defaultValue;
    
    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return the hasDefaultValue
     */
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the avs_version
     */
    public int getVersion() {
        return avs_version;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }


    //XXX remove this ctor it is only used to lookup AVSType in vector of fields in AVSType private method
    //getLocalIndex this is just a hack to get it work but fix it asap(this comment will probably stay for 10 years)
    //HA and you were wrong !!!! 
    //    AVSFieldInfo(String id){
    //        this.id=id;
    //        hash = 0;
    //        for (int i = 0; i < id.length(); i++)
    //            hash += id.charAt(i);
    //    }

    AVSFieldInfo(String id, AVSFieldType type, boolean multiValued, String reftype) {
        this.id = id;
        this.type = type;
        this.multiValued = multiValued;
        this.reftype = reftype;

        this.codec = AVSCodec.getAVSFieldCodec(type, multiValued);

        tagId = -1;
        
        hash = 0;
        for (int i = 0; i < id.length(); i++)
            hash += id.charAt(i);
        
        createStringValue();
    }

    AVSFieldInfo(String id, AVSFieldType type, int avs_version, int index, String defaultValue, String comment, boolean multiValued, String reftype) {
        this.id = id;
        this.type = type;
        this.multiValued = multiValued;
        this.reftype = reftype;
        this.avs_version = avs_version;
        this.index = index;
        this.defaultValue = defaultValue;
        this.comment = comment;

        this.codec = AVSCodec.getAVSFieldCodec(type, multiValued);

        tagId = -1;

        hash = 0;
        for (int i = 0; i < id.length(); i++)
            hash += id.charAt(i);

        createStringValue();
    }

    
    public String getId() {
        return id;
    }

    public AVSFieldType getType() {
        return type;
    }

    public String getReftype() {
        return reftype;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public int getTagId() {
        return tagId;
    }

    //should be used only by AVSType class to populate tagId
    //the tagId depends on all fields ordering so can be computed 
    //only when all fields are added
    void setTagId(int tagId) {
        this.tagId = tagId;
        createStringValue();
    }

    //this is avs codec that is used to decode value for this field
    public AVSFieldCodec getCodec() {
        return codec;
    }

    //This strange ordering function is taken from C implementation of AVS encoding
    //since correct tag id generation relies on order of fields in AVS the sort order
    //must be exactly the same.
    public int compareTo(AVSFieldInfo o) {
        //1) compare addresses
        if (this == o)
            return 0;

        //2) compare field name length
        if (id.length() != o.id.length())
            return id.length() - o.id.length();

        //3) compare hash
        if (hash != o.hash) {
            long result = hash - o.hash; //we need to return int 
            if (result == 0)
                return 0;
            if (result > 0)
                return 1;
            return -1;
        }

        //4)finally if all previous ways to compare failed do string compare
        return id.compareTo(o.id);
    }

    private String stringValue;

    private void createStringValue() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[ id: ");
        buffer.append(id);
        buffer.append(", tag: ");
        buffer.append(tagId);
        buffer.append(", type: ");
        buffer.append(type.getName());
        buffer.append(", multiValued: ");
        buffer.append(multiValued ? "true" : "false");
        buffer.append(", reftype: ");
        buffer.append(reftype);
        buffer.append(" ]");
        stringValue = buffer.toString();
    }

    public String toString(){
        return  stringValue;
    }
}
