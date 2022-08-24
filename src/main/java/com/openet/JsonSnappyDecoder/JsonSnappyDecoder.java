package com.openet.JsonSnappyDecoder;
import javax.xml.bind.DatatypeConverter;
import org.xerial.snappy.Snappy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonSnappyDecoder {
  public static void main(String[] paramArrayOfString) {
    String line = new String();
    try {
      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(paramArrayOfString[0]));
      line = reader.readLine();
      while (line != null) {
//        byte[] arrayOfByte1 = DatatypeConverter.parseHexBinary(line);
//        System.out.println("data.length: " + line.length() + ", compressed.length: " + arrayOfByte1.length);
//        byte[] arrayOfByte2 = Snappy.uncompress(arrayOfByte1);
//        String OriJson = new String(arrayOfByte2, "UTF-8");
        String OriJson = new String (Snappy.uncompress(DatatypeConverter.parseHexBinary(line)), "UTF-8");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(OriJson);
        System.out.println(gson.toJson(jsonElement));
        line = reader.readLine();
      }
      reader.close();
    } catch (Exception exception) {
        System.out.println("data.length: " + line.length() + "\n" + line);
        exception.printStackTrace();
    }
  }
}
