package components;
 
public class lookupRunnable implements Runnable {
    components.lookupJNI object;
    String filename;
    public lookupRunnable(lookupJNI object, String filename) {
        this.object = object;
        this.filename = filename;
    }
    @Override
    public void run() {
        object.performWork(filename);
    }
}