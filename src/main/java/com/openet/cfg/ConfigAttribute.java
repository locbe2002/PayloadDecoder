package cfg;
import java.util.*;
import java.lang.*;
import java.io.*;
import org.xml.sax.Attributes;

public class ConfigAttribute {

    private static final String ID = "id";

    private static final String MULTI_VALUED = "multiValued";

    private static final String TYPE = "type";

    private static final String REF_TYPE = "reftype";

    private static final String FINAL = "final";

    private static final String MAPPER = "mapper";

    private static final String DEFAULT = "default";

    private static final String COMMENT = "comment";
    
    private static final String INDEX = "index";

    private String id;

    private boolean isFinal;

    private String type;

    private String refType;

    private boolean multiValued;
    
    private boolean isPassword;

    private String mapper;

    private Value[] defaultValue;

    /** Avs Versioning <p>
     * Store attribute version 
     * */
    private int avsVersion = -1;
    

    /** Avs Versioning <p>
     * config attribute index
     */
    private int index = -1;

    /** Avs Versioning <p>
     * config attribute comment
     */
    private String comment = "";

    /** Avs Versioning <p>
     * Get avs version for the config attribute
     * @return version number
     */
    public int getAvsVersion() {
        return avsVersion;
    }

    /** Avs Versioning <p>
     * Get comment for config attribute
     * @return comment
     */    
    public String getComment() {
        return comment;
    }
    
    /**
     * Get config attribute index
     * @return config attribute index value
     */
    public int getIndex() {
        return index;
    }

    public ConfigAttribute(Attributes atts, ObjectRepository or) throws ObjectRepositoryException {
        this(atts, or, null, "");
    }
    
    
    public ConfigAttribute(Attributes atts, ObjectRepository or, String avsVersion, String configTypeName) throws NumberFormatException, ObjectRepositoryException {
        id = atts.getValue(ID);
        multiValued = Boolean.valueOf(atts.getValue(MULTI_VALUED)).booleanValue();
        type = atts.getValue(TYPE);
        refType = atts.getValue(REF_TYPE);
        isFinal = Boolean.valueOf(atts.getValue(FINAL)).booleanValue();
        mapper = atts.getValue(MAPPER);
        String def = atts.getValue(DEFAULT);
        
        // versioned avs type
        if (avsVersion !=null && !avsVersion.isEmpty()) {
            try {
                this.avsVersion = Integer.parseInt(avsVersion);
                if (this.avsVersion < 1)
                    throw new ObjectRepositoryException(new String("Avs version value must be greater than 0 in ConfigType: " + configTypeName));
            } catch (NumberFormatException e1) {
                throw new ObjectRepositoryException(new String("Wrong value of field version"));
            }            
            String indexVal = atts.getValue(INDEX);
            if (indexVal != null && !indexVal.isEmpty()) {
                try {
                    index = Integer.parseInt(indexVal);
                    if (index < 0)
                        // Index must be greater or equal to 0
                        throw new ObjectRepositoryException(new String("Field index value must be >= 0 in ConfigType: " + configTypeName));
                } catch (NumberFormatException e) {
                    // if avsVersion is not null it means it is a versioned avs so index must be set
                    throw new NumberFormatException(new String("Field index has a wrong value in field : " + id + ", ConfigType: " + configTypeName));
                }
            } else {
                // if avsVersion is not null it means it is a versioned avs so index must be set
                throw new NumberFormatException(new String("There is no value for field index in field: " + id + ", ConfigType: " + configTypeName));
            }                
            comment = atts.getValue(COMMENT);
        }
        
        if (def != null) {
            /*if(null != type && type.equals(ConfigType.REFTYPE)) {
                try {
                    //need to resolve the reference to an actual PK here
                    ORPK defaultPK = or.getIdFromPath(def);
                    defaultValue = new Value[] { new Value(defaultPK) };
                } catch (ObjectRepositoryException ex) {
                    //skip it
                }
            
            } else {*/
                defaultValue = new Value[] { new Value(def) };
            //}
        }
        isPassword = false;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getMapper() {
        return mapper;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public String getRefType() {
        return refType;
    }

    public String getType() {
        return type;
    }

    public Value[] getDefaultValue() {
        return defaultValue;
    }

    public String getName() {
        return id;
    }
    // passwords in the OR are identified in the typedoc by the attribute  <renderhints flavor="concealed"> 
    public boolean isPassword() {
        return isPassword;
    }
    // passwords in the OR are identified in the typedoc by the attribute  <renderhints flavor="concealed"> 
    public void setPassword(boolean password) {
        this.isPassword = password;
    }

}
