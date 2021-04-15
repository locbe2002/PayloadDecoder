package cfg;
import java.lang.*;
import java.util.*;

public class ObjectRepositoryException extends Exception {

    /**
     * If duplicate keys are forund in the or this string is inserted in the exception message to preceed the next key combination
     * to try.
     */
    public static final String DUPLICATE_KEYS_STRING = " Next available name is (";
    public static final String ALREADY_EXISTS = " already exists in the configuration.";

    protected Object description = null;

    private ObjectRepositoryExceptionCode errCode = ObjectRepositoryExceptionCode.ERR_NOCODE;

    public ObjectRepositoryException(Object descr, ObjectRepositoryExceptionCode code) {
        this.description = descr;
        errCode = code;
    }

    public ObjectRepositoryException(Object descr) {
        this.description = descr;
        errCode = ObjectRepositoryExceptionCode.ERR_NOCODE;
    }

    public ObjectRepositoryException() {
        // No information available
    }

    public String toString() {
        if (description != null)
            return description.toString();
        return "unknown reason";
    }

    public String getMessage() {
        return toString();
    }

    public int getErrorCode() {
        return errCode.intValue();
    }

    public ObjectRepositoryExceptionCode getExceptionCode() {
        return errCode;
    }

    /**
     * Indicates if this exception is a caused by a authorisation issue
     */
    public boolean isAuthorisationException() {
        return (errCode == ObjectRepositoryExceptionCode.ERR_VALIDATION_FAILED);
    }
}
