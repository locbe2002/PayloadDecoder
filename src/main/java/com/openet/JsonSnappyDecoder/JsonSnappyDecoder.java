package com.openet.JsonSnappyDecoder;
import javax.xml.bind.DatatypeConverter;
import org.xerial.snappy.Snappy;

public class JsonSnappyDecoder {
  public static void main(String[] paramArrayOfString) {
    String line = new String();
    try {
      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(paramArrayOfString[0]));
      line = reader.readLine();
      while (line != null) {
        byte[] arrayOfByte1 = DatatypeConverter.parseHexBinary(line);
        System.out.println("data.length: " + line.length() + ", compressed.length: " + arrayOfByte1.length);
        byte[] arrayOfByte2 = Snappy.uncompress(arrayOfByte1);
        String str2 = new String(arrayOfByte2, "UTF-8");
        System.out.println(str2);
        line = reader.readLine();
      }
      reader.close();
    } catch (Exception exception) {
        System.out.println("data.length: " + line.length() + "\n" + line);
        exception.printStackTrace();
    }
  }
}
