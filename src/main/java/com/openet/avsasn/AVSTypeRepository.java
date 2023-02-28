package avsasn;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import cfg.ConfigType;
import cfg.ConfigTypeDetails;
import cfg.ObjectRepository;
import cfg.ObjectRepositoryException;

//TODO add proper locking if this class will be thread safe all other classes will be thread safe
public class AVSTypeRepository {

    // Initialization on Demand Holder (IODH) idiom
    private static class SingletonHolder {
        static AVSTypeRepository instance = new AVSTypeRepository();
    }

    public static AVSTypeRepository getInstance() {
        return SingletonHolder.instance;
    }

    // forbid others create
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private final ReentrantLock lock = new ReentrantLock();

    private Hashtable<String, AVSType> avsTypesCache;
    private ObjectRepository objectRepository;

    public AVSType get(String avsTypeName) throws ObjectRepositoryException {// should throw something else

        if (!initialized) {
            return null;
        }
        AVSType result = null;
        if ((result = avsTypesCache.get(avsTypeName)) != null)
            return result;

        lock.lock();
        try {
            return find0(avsTypeName);
        } finally {
            lock.unlock();
        }

    }

    private AVSType find0(String name) throws ObjectRepositoryException {
        if (!initialized)
            return null;

        AVSType result;
System.out.println("you are in find0 Name = " + name);
        if ((result = avsTypesCache.get(name)) != null)
            return result;

        ConfigType configType;

        configType = objectRepository.getType(name);// this method can throw

        AVSType superType = null;
        if (configType != null) {
            // "AVS" is the top of the tree for AVSes
            // and "Object" is the top of the tree for EAVSes
            if (!configType.name.equals("AVS") && !configType.name.equals("Object")) {
                superType = find0(configType.superType.name);
            }
        }

        //create type instance either versioned or unversioned
        AVSType avsType = AVSType.create(superType, name, configType.isVersioned());

        String[] configTypeFields = configType.getLocalAttributeNames();
        for (int i = 0; i < configTypeFields.length; i++) {
            avsType.addField(configType.getAttribute(configTypeFields[i]));
            }

        avsType.setChecksum(configType); // in C++ API it is called sortFields()
        avsTypesCache.put(name, avsType);
        return avsType;
    }

    private boolean initialized;

    private AVSTypeRepository() {
        initialized = false;
        avsTypesCache = new Hashtable<String, AVSType>();
    }

    public boolean isInitialized() {
        return initialized;
    }

    //note that it is better to preload avs types on the other hand one would conserve memory if types are loaded lazily
    //so it's up to you what to do  
    public void initialize(ObjectRepository objectRepository, boolean preloadAVSTypes) throws Exception {
        if (objectRepository == null)
            throw new NullPointerException();
        
        avsTypesCache.clear();
        //objectRepository.flushTypeCache();
        this.objectRepository = objectRepository;
        initialized = true;

        if (preloadAVSTypes) {
            try {
                ConfigTypeDetails[] details = objectRepository.getDescendantTypeDetails("AVS");
                if (details != null && details.length > 0) {
                    for (int i = 0; i < details.length; i++) {
                        get(details[i].name);
                    }
                }
            } catch (Exception e) {
                avsTypesCache.clear();
                this.objectRepository = null;
                initialized = false;
                throw e;
            }
        }

    }

}
