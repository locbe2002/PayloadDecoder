package cfg;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Create a checksum used by ConfigTypes.
 * Provides interface to get the checksum as a raw byte array or 
 * formatted as a hex string.
 * @author astulka
 *
 */
public class ChecksumVersioned extends Checksum {

    private int checksum;

    /**
     * Creates the checksum from the fields in a ConfigType.
     * @param avsType The ConfigType to create the checksum for.
     * @throws NoSuchAlgorithmException
     */
    public ChecksumVersioned(ConfigType avsType, int version) throws NoSuchAlgorithmException, NoSuchProviderException {
        super(avsType);
        
        String digestString = new String();        

        ConfigType.Version v = avsType.getVersions().get(version - 1);
        for (int index = 0; index <= v.getEndFieldIndex(); index++) {
            ConfigAttribute attr = avsType.getLocalAttribute(index);

            // it can happen if someone messed up indexes in avs type
            if (attr == null) {
                String msg = new String("Inconsistent index values for type (" + avsType.name + ")");
                throw new NoSuchAlgorithmException(msg);
            }
            digestString += attr.getName() + convertToAVSTypeName(attr.getType(), attr.isMultiValued()) + attr.getIndex();
        }

        ///
        // calculate checksum and copy 2 less significant bytes to checksum
        ///
        checksum = calculateChecksum(digestString.getBytes());
        }

    
    /**
     * Calculate the Internet Checksum of a buffer (RFC 1071 - http://www.faqs.org/rfcs/rfc1071.html)
     * Algorithm is
     * 1) apply a 16-bit 1's complement sum over all octets (adjacent 8-bit pairs [A,B], final odd length is [A,0])
     * 2) apply 1's complement to this final sum
     *
     * Notes:
     * 1's complement is bitwise NOT of positive value.
     * Ensure that any carry bits are added back to avoid off-by-one errors
     *
     * @param buf input data 
     * @return checksum
     */
    public int calculateChecksum(byte[] buf) {
      int length = buf.length;
      int i = 0;

      long sum = 0;
      long data;

      // Handle all pairs
      while (length > 1) {
        data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
        sum += data;
        i += 2;
        length -= 2;
      }

      // Handle remaining byte in odd length buffers
      if (length > 0) {
            sum += buf[i];
        }

        /* Fold 32-bit sum to 16 bits */
        while ((sum >> 16) > 0) {
            sum = (sum & 0xffff) + (sum >> 16);
      }

      // Final 1's complement value correction to 16-bits
      sum = ~sum;
      sum = sum & 0xFFFF;
        return (int)sum;
    }
    
    
    public int getValue() {
        return checksum;
    }
    }

