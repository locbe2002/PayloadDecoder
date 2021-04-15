package components;
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.io.*;
import java.net.*;

import java.util.HashMap;
import java.util.Map;

public class lookupJNI {
   private static final String indexDLL = new String("index");

   public native long createIndex(String filename);
   public native String lookup(long indexPointer, String lookupString);
   public native int greetings();
   public native int getProgress(long index);
   public native void cleanup(long index);
   public native int startLoad(long indexPointer, String filename);

   public long getIndex() { return index; }
   private long index;
   private HashMap<String, Long> indexMap = null;

   static {
      try {
          System.loadLibrary(indexDLL);
      } catch (UnsatisfiedLinkError e) {
          loadFromJar();
      }
   }

   public static void loadFromJar() {
       try {
           loadDLLs(indexDLL);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   public lookupJNI() {
       indexMap = new HashMap<String, Long>();
   }

   private static void loadDLLs(String dlls) throws Exception {
       try {
           InputStream in = lookupJNI.class.getClassLoader().getResourceAsStream("index.dll");
           File fileOut = new File(System.getProperty("java.io.tmpdir") + "/" + dlls + ".dll");
           OutputStream out = IOUtils.openOutputStream(fileOut);
           IOUtils.copy(in, out);
           in.close(); out.close();
           System.load(fileOut.toString());
       } catch (Exception e) {
           throw new Exception("Failed to load required DLL", e);
       }
   }

   public void cleanup() {
       for (Map.Entry<String, Long> entry : indexMap.entrySet()) {
         cleanup(entry.getValue().longValue());
       }
       indexMap.clear();
   }

   public void cleanup(String filename) {
       if (indexMap.containsKey(filename)) {
           cleanup(indexMap.get(filename).longValue());
           indexMap.remove(filename);
       }
   }

   public void init(String filename) {
       if (!indexMap.containsKey(filename)) {
         index = createIndex(filename);
         indexMap.put(filename, new Long(index));
       } else {
         index = indexMap.get(filename).longValue();
       }
   }

   public String lookup(String lookupString) {
      String ans = lookup(index, lookupString);
      for (Map.Entry<String, Long> entry : indexMap.entrySet()) {
          if (entry.getValue().longValue() != index) {
              ans += "\n";             
              ans += lookup(entry.getValue().longValue(), lookupString);
          }
      }
      //System.out.println("Lookup records for " + lookupString + " => " + ans); 
      return ans;
   }

   public void performWork(String filename) {
      System.out.println("filename: " + filename);
      if (indexMap.containsKey(filename)) 
          index = indexMap.get(filename).longValue();
      startLoad(index, filename);
   }
}
