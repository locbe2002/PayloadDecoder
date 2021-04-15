package cfg;
import java.lang.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class ConfigType {
    private HashMap<String, ConfigAttribute> fields = new HashMap<String, ConfigAttribute>();
    public String name;
    public boolean isAbstract;
    private boolean isBranded;
    private boolean hasVersions = false;
    private boolean isPassword;
    public Element rootElement;
    public ConfigType superType;
    private ArrayList<AllowedChild> allowedChildren = new ArrayList<AllowedChild>();
    public static final String STRING = "string";
    public static final String BINARY = "binary";
    public static final String INTEGER = "integer";
    public static final String IPV4ADDR = "ipv4Addr";
    public static final String IPADDR = "ipAddr";
    public static final String ADDRESS = "Address";
    public static final String UTCTIME = "utctime";
    public static final String BOOLEAN = "boolean";
    public static final String REFTYPE = "reference";
    public static final String ENVIRON = "environmental";
    public static final String INCOMING = "receives";
    public static final String OUTGOING = "generates";
    public static final String CAUSES = "causes";
    public static final String USER = "user";
    public static final String ENUMERATED = "enumerated";

        public enum IMPORT_TYPE_POLICY {
            RETAIN("retain"), REPLACE("replace"), MERGE("merge"), DIE("DIE");
            private IMPORT_TYPE_POLICY(String name) {
                this.name = name;
            }

            private final String name;

            public String toString() {
                return name;
            }

            public static IMPORT_TYPE_POLICY toEnum(String importTypePolicy) {
                if(null == importTypePolicy) {
                    return null;
                } else if(0 == importTypePolicy.length()) {
                    return null;
                }
                if(importTypePolicy.equals(IMPORT_TYPE_POLICY.DIE.toString())) {
                    return IMPORT_TYPE_POLICY.DIE;
                } else if(importTypePolicy.equals(IMPORT_TYPE_POLICY.MERGE.toString())) {
                    return IMPORT_TYPE_POLICY.MERGE;
                } else if(importTypePolicy.equals(IMPORT_TYPE_POLICY.REPLACE.toString())) {
                    return IMPORT_TYPE_POLICY.REPLACE;
                } else if(importTypePolicy.equals(IMPORT_TYPE_POLICY.RETAIN.toString())) {
                    return IMPORT_TYPE_POLICY.RETAIN;
                }

                return null;
            }

        }
    protected IMPORT_TYPE_POLICY importTypePolicy;
    protected String[] keys;
    public class ConfigTypeContentHandler extends DefaultHandler {
        Vector<String> keyV = new Vector<String>();
        String lastAvsVersionParsed = "";
        String lastFieldNameParsed = "";
        ObjectRepository or;

        private Vector<String> getCSVs(String values) {
            Vector<String> v = new Vector<String>();
            if (values == null) return v;
            int idx, lastIdx;
            for (lastIdx = 0; (idx = values.indexOf(",", lastIdx)) != -1; lastIdx = idx + 1) {
                String tokenValue = values.substring(lastIdx, idx).trim();
                if (tokenValue.length() > 0)
                    v.add(tokenValue);
            }
            String lastValue = values.substring(lastIdx, values.length()).trim();
            if (lastValue.length() > 0)
                v.add(lastValue);
            return v;
        }

/*
 * <configType name="PM_GetSubscriberProfileRequest" extends="PolicyManager" importTypePolicy="replace"> 
 * <version major="1" minor="0" branded="true" /> 
 * <fields>
 * <avs_version id="1">
 * <field id="subscriberId" type="reference" reftype="PM_SubscriberIdentity" index="0" />
 * <field id="flowType" type="string" index="1" />
 * <field id="networkRequest" type="reference" reftype="AVS" index="2" />
 * <field id="SessionStoreDetails" type="reference" reftype="PM_GxPolicySession" index="3" />
 * <field id="duplicateCCRDetected" type="boolean" index="4" />
 * <field id="sprInfoCache" type="string" index="5" />
 * <field id="getNetworkInformation" type="reference" reftype="PM_GetNetworkInfoResponse" index="6" />
 * </avs_version>
 * </fields>
 * </configType>
 */
        public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException {
            try {
                if (localName.equals("configType")) {
                    for (int i = atts.getLength(); i-- > 0;) {
                        String aname = atts.getLocalName(i);
                        String avalue = atts.getValue(i);
                        if ("name".equals(aname) || "objectTypeID".equals(aname)) {
                            name = avalue;
                        }
                        else if ("key".equals(aname)) {
                            keyV = getCSVs(avalue);
                        }
                        else if ("extends".equals(aname)) {
                            superType = or.getType(avalue);
                        }
                        else if ("abstract".equals(aname)) {
                            isAbstract = "true".equals(avalue) ? true : false;
                        } else if ("importTypePolicy".equals(aname)) {
                            importTypePolicy = ConfigType.IMPORT_TYPE_POLICY.toEnum(avalue);
                        }
                    }
                } else if (localName.equals("allowedChild")) {
                    String childTypeName = atts.getValue("type");
                    boolean displayInTree = "true".equals(atts.getValue("display"));
                    boolean uniqueChildType = "true".equals(atts.getValue("uniqueChildType"));
                    String minChildren = atts.getValue("minChildren");
                    String maxChildren = atts.getValue("maxChildren");
                    int min = 0;
                    if (minChildren != null) {
                        min = Integer.parseInt(minChildren);
                    }
                    int max = Integer.MAX_VALUE;
                    if (maxChildren != null) {
                        max = Integer.parseInt(maxChildren);
                    }
                    allowedChildren.add(new AllowedChild(childTypeName, displayInTree, uniqueChildType, min, max));
                } else if (localName.equals("avs_version")) {
                    lastAvsVersionParsed = atts.getValue("id");
                    if (lastAvsVersionParsed == null)
                        throw new Exception(new String ("avs_version id not found"));
                    hasVersions = true;
                } else if (localName.equals("field")) {
                    lastFieldNameParsed = atts.getValue("id");
                    if (lastAvsVersionParsed.isEmpty()) {
                        fields.put(lastFieldNameParsed, new ConfigAttribute(atts, or));
                    } else {
                        // versioned AVS
                        fields.put(lastFieldNameParsed, new ConfigAttribute(atts, or, lastAvsVersionParsed, ConfigType.this.name));
                    }
                } else if (localName.equals("renderhints")) {
                    String f = atts.getValue("flavor");
                    if ((f != null) && f.equals("concealed")) {
                        //LOGGER.debug("startElement: "+  name + "." + lastFieldNameParsed + "': <renderhints flavor=\"" + f + "\">");
                        fields.get(lastFieldNameParsed).setPassword(true);
                    }
                } else if (localName.equals("version")) {
                    String b = atts.getValue("branded");
                    if (b != null) {
                        isBranded = b.equals("true");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new SAXException(ex);
            }
        }

        public void endElement(String namespaceURI, String localName, String qualifiedName) {
            if (localName.equals("configType")) {
                keys = (String[]) keyV.toArray(new String[keyV.size()]);
            } else if (localName.equals("avs_version")) {
                lastAvsVersionParsed = "";
            }
        }
        ConfigTypeContentHandler(ObjectRepository or) { 
            this.or = or;
        }
    }

    public class Version {
        int startFieldIndex = Integer.MAX_VALUE;
        int endFieldIndex;

        ChecksumVersioned checksum;

        public int getStartFieldIndex() {
            return startFieldIndex;
        }
        public int getEndFieldIndex() {
            return endFieldIndex;
        }

        void setChecksum(ConfigType avsType, int version) throws NoSuchAlgorithmException, NoSuchProviderException {
            try {
                checksum = new ChecksumVersioned(avsType, version);
            } catch (NoSuchAlgorithmException e) {
                throw e;
            }
        }

        public ChecksumVersioned getChecksum() {
            return checksum;
        }

        void update(int index) {
            if (index < startFieldIndex) {
                startFieldIndex = index;
            }
            if (index > endFieldIndex) {
                endFieldIndex = index;
            }
        }
    }

    private java.util.Vector<Version> versions;
    public java.util.Vector<Version> getVersions() {
        return versions;
    }

    private class AllowedChild {
        String childTypeName;

        boolean displayedInTree;

        boolean uniqueChildType;

        int minChildren;

        int maxChildren;

        AllowedChild(String childTypeName, boolean displayedInTree, boolean uniqueChildType, int minChildren, int maxChildren) {
            this.childTypeName = childTypeName;
            this.displayedInTree = displayedInTree;
            this.uniqueChildType = uniqueChildType;
            this.minChildren = minChildren;
            this.maxChildren = maxChildren;
        }
    }

    private ObjectRepository or;
    public ConfigType(ObjectRepository or) {
        this.or = or;
        this.keys = new String[0];
    }
    public String toString() {
        return name;
    }
    public ConfigType(String renderDocument, ObjectRepository or) throws Exception {
        this.or = or;
        try {
            Reader reader = new java.io.StringReader(renderDocument);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(reader), new ConfigTypeContentHandler(or));
            reader = new java.io.StringReader(renderDocument);
            SAXBuilder builder = new SAXBuilder();
            Document jdoc = builder.build(reader);
            rootElement = jdoc.getRootElement();
            if (hasVersions) setChecksum();
        } catch (ParserConfigurationException e) {
            throw new Exception("ParserConfigurationException");
        } catch (SAXException e) {
            throw new Exception("SAXException");
        } catch (IOException e) {
            throw new Exception("IOException");
        } catch (JDOMException e) {
            throw new Exception("JDOMException");
        } catch (Exception e) {
            throw e;
        }
    }
    private void setChecksum() throws ObjectRepositoryException {
        int maxVersion=0;
        Collection<ConfigAttribute > attrs = fields.values();
        for (ConfigAttribute configAttribute : attrs) {
            if (configAttribute.getAvsVersion() > maxVersion) {
                maxVersion = configAttribute.getAvsVersion();
            }
        }

        versions = new Vector<Version>(maxVersion);
        for(int i=0; i < maxVersion; ++i) {
            versions.add(new Version());
        }

        int maxIndexValue = 0;
        for (ConfigAttribute configAttribute : attrs) {
            int version = configAttribute.getAvsVersion();
            int index = configAttribute.getIndex();
            if (index > maxIndexValue)
                maxIndexValue = index;
            versions.get(version -1).update(index);
        }
        if (attrs.size() > 0 && ((maxIndexValue +1 ) != attrs.size())) {
            String msg = new String("Inconsistent index values for type (" + name + "). Max index value = "  + maxIndexValue + " while number of fields " + attrs.size());
            throw new ObjectRepositoryException(msg);
        }
        for (int i = 0; i < versions.size(); i++) {
            try {
                versions.get(i).setChecksum(this, i + 1);
            } catch (NoSuchAlgorithmException e) {
                throw new ObjectRepositoryException(e.getMessage());
            } catch (NoSuchProviderException e) {
                throw new ObjectRepositoryException(e.getMessage());
            }
        }
    }
    private Checksum checksum = null;
    public byte[] getChecksumAsByteArray() {
        if(checksum == null) {
            try {
                checksum = new Checksum(this);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
                return null;
            }
        }
        return checksum.getAsByteArray();
    }
    public String getChecksumAsHexString() {
        if(checksum==null) {
            try {
                checksum = new Checksum(this);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
                return null;
            }
        }
        return checksum.getAsHexString();
    }
    public String getChecksumAsHexString(int version) {
        if (versions != null && version > 0 && version <= versions.size()) {
            String checksum = Integer.toHexString(versions.get(version - 1).getChecksum().getValue());
            while (checksum.length() < 4) {
                checksum = "0" + checksum;
            }
            return checksum;
        }
        else
            return null;
    }
    public byte[] getChecksumAsByteArray(int version) {
        if (versions != null && version > 0 && version <= versions.size())
            return versions.get(version - 1).getChecksum().getAsByteArray();
        else
            return null;
    }
    public int getAttributeCount() {
        return fields.size();
    }
    public boolean isMVAttribute(String field) {
        ConfigAttribute att = getAttribute(field);
        if (att != null) return att.isMultiValued();
        return false;
    }
    public ConfigAttribute getLocalAttribute(int index) {
        if (index < fields.size()) {
            Collection<ConfigAttribute> attrs = fields.values();
            for (ConfigAttribute configAttribute : attrs) {
                if (configAttribute.getIndex() == index) {
                    return configAttribute;
                }
            }
        }
        return null;
    }
    public Value[] getDefaultValue(String fieldId) {
        ConfigAttribute att = getAttribute(fieldId);
        if (att != null) {
            return att.getDefaultValue();
        }
        return null;
    }
    public ConfigAttribute getAttribute(String fieldId) {
        ConfigType t = this;
        while (t != null) {
            ConfigAttribute att = (ConfigAttribute) t.fields.get(fieldId);
            if (att != null) {
                return att;
            }
            t = t.superType;
        }
        return null;
    }
    public String getAttributeType(String field) {
        ConfigAttribute att = getAttribute(field);
        if (att != null) {
            return att.getType();
        }
        return null;
    }
    public String[] getAllKeys() {
        Vector<String> allKeys = new Vector<String>();
        for (ConfigType ctype = this; ctype != null; ctype = ctype.superType)
            for (int i = 0; i < ctype.keys.length; i++)
                allKeys.add(ctype.keys[i]);
        return (String[]) allKeys.toArray(new String[allKeys.size()]);
    }
    public boolean isBranded() {
        return isBranded;
    }
    public boolean isVersioned() {
        return hasVersions;
    }
    public IMPORT_TYPE_POLICY getImportTypePolicy() {
        return importTypePolicy;
    }
    public String[] getAllAllowedChildren() {
        ArrayList<String> all = new ArrayList<String>();
        for (ConfigType t = this; t != null; t = t.superType) {
            for (Iterator i = t.allowedChildren.iterator(); i.hasNext();) {
                AllowedChild ac = (AllowedChild) i.next();
                all.add(ac.childTypeName);
            }
        }
        return (String[]) all.toArray(new String[all.size()]);
    }
    public int compareTo(Object other) {
        return name.compareTo(((ConfigType) other).name);
    }

    public boolean equals(Object other) {
        if (other != null && other instanceof ConfigType)
            return ((ConfigType) other).name.equals(name);
        return false;
    }
    public int hashCode() {
        return name.hashCode();
    }
    public String[] getLocalAttributeNames() {
        return fields.keySet().toArray(new String[fields.size()]);
    }
    public ConfigTypeDetails getConfigTypeDetails(ConfigTypeDetails details) {

        if (this.superType != null) {
            details.extendsType = this.superType.name;
        }
        else {
            details.extendsType = "";
        }

        details.isAbstract = this.isAbstract;
        details.isBranded = this.isBranded;
        details.name = this.name;
        return details;
    }
    public boolean isAttributeRefType(String field) {
        return REFTYPE.equals(getAttributeType(field));
    }
}
