package cfg;
public interface ObjectRepository {
    public ConfigType getType(String name);
    public ConfigTypeDetails[] getDescendantTypeDetails(String name);
}
