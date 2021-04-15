/*
 * ObjectRepositoryExceptionCode.java
 *
 * Created on 24 May 2004, 14:25
 */
package cfg;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A list of all exception codes for ObjectRepositoryExceptions Actually codes will automatically get assigned to new exceptions
 * code added, thus making administration of codes easier.
 * 
 * TODO: Replace with enum
 */
public class ObjectRepositoryExceptionCode implements Serializable {

	private static final long serialVersionUID = 545235786823429238L;

	private static ArrayList<ObjectRepositoryExceptionCode> codeList = new ArrayList<ObjectRepositoryExceptionCode>();

    private static int nextCode = 0;

    private int exceptionCode_;

    private String description_;

    /** Creates a new instance of ObjectRepositoryExceptionCode */
    private ObjectRepositoryExceptionCode(String descr) {
        exceptionCode_ = nextCode++;
        description_ = descr;
        codeList.add(this);
    }

    public String toString() {
        if (description_ != null)
            return description_;
        return ObjectRepositoryExceptionCode.class.toString();
    }

    public int hashCode() {
        return exceptionCode_;
    }

    public int intValue() {
        return exceptionCode_;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ObjectRepositoryExceptionCode) {
            return (exceptionCode_ == ((ObjectRepositoryExceptionCode) obj).exceptionCode_);
        }
        return false;
    }

    public boolean isValidCode() {
        return (!((this.equals(ERR_NOCODE)) || (this.equals(ERR_INVALID_CODE))));
    }

    public static ObjectRepositoryExceptionCode getExceptionCode(int intCode) {
        if (intCode >= nextCode) {
            return ERR_INVALID_CODE;
        }
        return (ObjectRepositoryExceptionCode) codeList.get(intCode);
    }

    // All ObjectRepository exception code should be added here.
    // Note that description string should be used for debug only
    public static final ObjectRepositoryExceptionCode ERR_NOCODE = new ObjectRepositoryExceptionCode("ERR_NOCODE");

    public static final ObjectRepositoryExceptionCode ERR_INVALID_CODE = new ObjectRepositoryExceptionCode("ERR_INVALID_CODE");

    public static final ObjectRepositoryExceptionCode ERR_PASSWORD_INVALID = new ObjectRepositoryExceptionCode(
            "ERR_PASSWORD_INVALID");

    public static final ObjectRepositoryExceptionCode ERR_PASSWORD_REUSE = new ObjectRepositoryExceptionCode("ERR_PASSWORD_REUSE");

    public static final ObjectRepositoryExceptionCode ERR_USER_EXPIRED = new ObjectRepositoryExceptionCode("ERR_USER_EXPIRED");

    public static final ObjectRepositoryExceptionCode ERR_USER_DISABLED = new ObjectRepositoryExceptionCode("ERR_USER_DISABLED");

    public static final ObjectRepositoryExceptionCode ERR_VALIDATION_FAILED = new ObjectRepositoryExceptionCode(
            "ERR_VALIDATION_FAILED");

    public static final ObjectRepositoryExceptionCode ERR_INVALID_FILE = new ObjectRepositoryExceptionCode("ERR_INVALID_FILE");

    public static final ObjectRepositoryExceptionCode ERR_DELETE_ADMINUSER = new ObjectRepositoryExceptionCode(
            "ERR_DELETE_ADMINUSER");

    public static final ObjectRepositoryExceptionCode ERR_DELETE_OWNUSER = new ObjectRepositoryExceptionCode("ERR_DELETE_OWNUSER");

    public static final ObjectRepositoryExceptionCode ERR_DANGLING_REFERENCES = new ObjectRepositoryExceptionCode(
            "ERR_DANGLING_REFERENCES");

    public static final ObjectRepositoryExceptionCode ERR_DUPLICATE_KEYS = new ObjectRepositoryExceptionCode("ERR_DUPLICATE_KEYS");

    public static final ObjectRepositoryExceptionCode ERR_OLD_VERSION = new ObjectRepositoryExceptionCode("ERR_OLD_VERSION");

    public static final ObjectRepositoryExceptionCode ERR_BRANDED_TYPE = new ObjectRepositoryExceptionCode("ERR_BRANDED_TYPE");

    public static final ObjectRepositoryExceptionCode ERR_ACCESS_DENIED = new ObjectRepositoryExceptionCode("ERR_ACCESS_DENIED");

    public static final ObjectRepositoryExceptionCode ERR_NO_SUCH_OBJECT = new ObjectRepositoryExceptionCode("ERR_NO_SUCH_OBJECT");

    public static final ObjectRepositoryExceptionCode ERR_UNAVAILABLE = new ObjectRepositoryExceptionCode("ERR_UNAVAILABLE");

    public static final ObjectRepositoryExceptionCode ERR_NO_SUCH_TYPE = new ObjectRepositoryExceptionCode("ERR_NO_SUCH_TYPE");
    
    public static final ObjectRepositoryExceptionCode ERR_INSTALLATION_ID_NOT_SET = new ObjectRepositoryExceptionCode("ERR_INSTALLATION_ID_NOT_SET");
    
    public static final ObjectRepositoryExceptionCode ERR_CLUSTER_ID_NOT_SET = new ObjectRepositoryExceptionCode("ERR_CLUSTER_ID_NOT_SET");
    
    public static final ObjectRepositoryExceptionCode ERR_NULL_LIST_PROVIDED = new ObjectRepositoryExceptionCode("ERR_NULL_LIST_PROVIDED");
    
    public static final ObjectRepositoryExceptionCode ERR_OPERATION_NOT_SUPPORTED = new ObjectRepositoryExceptionCode("ERR_OPERATION_NOT_SUPPORTED");
    
    public static final ObjectRepositoryExceptionCode ERR_CHILD_OBJECT_HAS_NO_PARENT = new ObjectRepositoryExceptionCode("ERR_CHILD_OBJECT_HAS_NO_PARENT");

    public static final ObjectRepositoryExceptionCode ERR_CONNECTION_POOL_MAX = new ObjectRepositoryExceptionCode("ERR_CONNECTION_POOL_MAX");

    public static final ObjectRepositoryExceptionCode ERR_NOT_IMPLEMENTED = new ObjectRepositoryExceptionCode("ERR_NOT_IMPLEMENTED");

    public static final ObjectRepositoryExceptionCode ERR_NOT_INITIALISED = new ObjectRepositoryExceptionCode("ERR_NOT_INITIALISED");

    public static final ObjectRepositoryExceptionCode ERR_NO_KEYS = new ObjectRepositoryExceptionCode("ERR_NO_KEYS");

	public static final ObjectRepositoryExceptionCode ERR_NULL_VALUE_PROVIDED = new ObjectRepositoryExceptionCode("ERR_NULL_VALUE_PROVIDED");

	public static final ObjectRepositoryExceptionCode ERR_NULL_TYPE_PROVIDED = new ObjectRepositoryExceptionCode("ERR_NULL_TYPE_PROVIDED");

    public static final ObjectRepositoryExceptionCode ERR_NO_SUCH_OBJECT_IN_CACHE = new ObjectRepositoryExceptionCode("ERR_NO_SUCH_OBJECT_IN_CACHE");

}
