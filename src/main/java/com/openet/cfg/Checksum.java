package cfg;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Create a checksum used by ConfigTypes.
 * Provides interface to get the checksum as a raw byte array or 
 * formatted as a hex string.
 * @author kieranod
 *
 */
public class Checksum {

    private byte[] checksum = new byte[16];
    private String checksumAsHexString = null;

    
    /**
     * Creates the checksum from the fields in a ConfigType.
     * @param configType The ConfigType to create the checksum for.
     * @throws NoSuchAlgorithmException
     */
    Checksum(ConfigType configType) throws NoSuchAlgorithmException, NoSuchProviderException {
        
    	String digestString = new String();
    	for(ConfigType type=configType; type != null; type=type.superType) {
            String[] attributeNamesArray = type.getLocalAttributeNames(); 
            Arrays.sort(attributeNamesArray, new FieldNameComparator());
            int attributeIndex = 0;
            for(String attributeName: attributeNamesArray) {
                digestString += attributeName 
                    + convertToAVSTypeName(type.getAttributeType(attributeName), type.isMVAttribute(attributeName)) 
                    + Integer.toString(attributeIndex++);
            }
    	}

        // Generate the 128 bit checksum.
        java.security.MessageDigest md = null;
        try {
        	// explicitly set the provider due to OracleUCrypto being auto-selected
        	// causing hassles on the new T4* sles11 boxes
            md = java.security.MessageDigest.getInstance("MD5", "SUN");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw(e);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw(e);
        }
        checksum = md.digest(digestString.getBytes());
        checksumAsHexString = new String();
        for(byte b : checksum) {
            checksumAsHexString += String.format("%02x", b);
        }
    }

    /**
     * Creates the checksum from the fields in a ConfigType.
     * @param avsType The ConfigType to create the checksum for.
     * @throws NoSuchAlgorithmException
     */
    
    /**
     * Returns the raw checksum in a byte array.
     * @return The checksum as a byte array, size 16 - 128 bits
     */
    public byte[] getAsByteArray() {
        return checksum;
    }
    
    /**
     * Format the 128 bit checksum into a hexadecimal string.
     * @return The checksum formatted as a hex string. Should contain 32 characters.
     */
    String getAsHexString() {
        return checksumAsHexString;
    }

    /**
     * This class mimics the comparison used by val_compare() implemented
     * in $SROOT/core/dsd/dsd.c.
     * @author kieranod
     *
     */
    private class FieldNameComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            if(o1.equals(o2)) {
                return 0;
            }

            if(o1.length() != o2.length()) {
                return o1.length() - o2.length();
            }

            long o1HashStr=getHashStr(o1);
            long o2HashStr=getHashStr(o2);
            if(o1HashStr!=o2HashStr) {
                if(o1HashStr < o2HashStr) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return o1.compareTo(o2);
        }

        /**
         * Generates a hash value for the string
         * Based on the hashstr() implementation in $SROOT/core/dsd/dsdintrn.h
         * @param s String to hash.
         * @return The hash value
         */
        private long getHashStr(String s) {
            long myHashValue=0;
            for(char ch: s.toCharArray()) {
                myHashValue += ch;
            }
            return myHashValue;
        }
    }

    /**
     * The ConfigType AVS names in Java are different to those defined in C++.
     * This function converts the JAVA names to the C++ names.  
     * 
     * @param s The string to convert.
     * @param isMultiValued Is the field a sequence.
     * @return The converted string.
     */
    protected String convertToAVSTypeName(String s, boolean isMultiValued) {
        if(!isMultiValued) {
            if(s.equals(ConfigType.STRING) || s.equals(ConfigType.ENUMERATED)) {
                return "STRING";
            } else if(s.equals(ConfigType.INTEGER)) {
                return "INTEGER"; 
            } else if(s.equals(ConfigType.IPV4ADDR)) {
                return "IP ADDRESS"; 
            } else if(s.equals(ConfigType.IPADDR)) {
                return "INET ADDRESS"; 
            } else if(s.equals(ConfigType.ADDRESS)) {
                return "ADDRESS"; 
            } else if(s.equals(ConfigType.UTCTIME)) {
                return "UTCTIME";
            } else if(s.equals(ConfigType.BINARY)) {
                return "BINARY"; 
            } else if(s.equals(ConfigType.BOOLEAN)) {
                return "INTEGER"; 
            } else if(s.equals(ConfigType.REFTYPE)) {
                return "AVS"; 
            }
        } else {
            if(s.equals(ConfigType.STRING) || s.equals(ConfigType.ENUMERATED)) {
                return "STRING_SEQ";
            } else if(s.equals(ConfigType.INTEGER)) {
                return "INTEGER_SEQ"; 
            } else if(s.equals(ConfigType.IPV4ADDR)) {
                return "IPADDRESS_SEQ"; 
            } else if(s.equals(ConfigType.IPADDR)) {
                return "INETADDRESS_SEQ"; 
            } else if(s.equals(ConfigType.ADDRESS)) {
                return "ADDRESS_SEQ";
            } else if(s.equals(ConfigType.UTCTIME)) {
                return "UTCTIME_SEQ";
            } else if(s.equals(ConfigType.BINARY)) {
                return "BINARY_SEQ"; 
            } else if(s.equals(ConfigType.BOOLEAN)) {
                return "INTEGER_SEQ"; 
            } else if(s.equals(ConfigType.REFTYPE)) {
                return "AVS_SEQ"; 
            }
        }

        /*
         * For anything else just return what was passed in, 
         * maybe one of the static defined strings:
         *     ConfigType.ENVIRON
         *     ConfigType.INCOMING
         *     ConfigType.OUTGOING
         *     ConfigType.CAUSES
         *     ConfigType.USER 
         */
        return s;
    }
}

