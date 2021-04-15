package avsasn;

public enum AVSFieldType {
    //wow enum with String labels do not change text labels because they are used in EDCGui project as resources keys
    //if you really really want and need to change them then update resources.properties file in EDCGui project
    BINARY("binary"), BOOLEAN("boolean"), INTEGER("integer"), IPV4ADDR("ipv4Addr"), IPADDR("ipAddr"), REFERENCE("reference"), STRING("string"), UTCTIME("utctime"), ADDRESS("Address");

    private AVSFieldType(String typeName) {
        this.typeName = typeName;
    }

    final private String typeName;

    public String getName() {
        return typeName;
    }
    
}
