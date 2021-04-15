package com.openet.FW_payload_decoder;
import org.apache.commons.io.FileUtils;
import org.json.*;
import java.util.*;
import java.lang.*;
import java.time.Duration;
import java.time.Instant;
import org.apache.commons.cli.*;
import org.h2.Driver;
import org.h2.tools.Server;
import java.sql.*;
import java.io.*;
import java.nio.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.math.BigInteger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import com.openet.FW_payload_decoder.Password.*;
import cfg.*;
import avsasn.*;
import avsasn.primitivetypes.*;
import avsasn.ber.*;
import avsasn.codec.*;
import zip.zipTool;
/*
 *
*/
public class Payload_decoder implements ObjectRepository {
	private static String FW_Avs = "SPM_Subscription";

	final static Logger logger = Logger.getLogger(Payload_decoder.class);

	private PreparedStatement selectOperRecs = null;
	private HashMap<String, String> map = new HashMap<>();
	private HashMap<String, ConfigType> mapType = new HashMap<>();
	
/*
 *
*/
  Payload_decoder() throws Exception {
    try {
    } catch (Exception e) {
		logger.error("Error locating h2 Driver:!", e);
		return ;
    } 
  }
/*
 *
*/
  public String getProperty(String key, String Default, Properties pm_prop) {
		String Ret = null;
    Ret = pm_prop.getProperty(key);
		if (Ret.equals(null)){
			Ret = Default;
		}
    return Ret;
  }
/*
 *
*/
  private List<String> parse(String name, String renderDocument, Connection PmConn) {
    List<String> set = new ArrayList<String> ();
    try {
      BufferedReader br = new BufferedReader(new StringReader(renderDocument));
      String thisLine = null;
      String reftype = new String("reftype=\"");
      String extend = new String("extends=\"");
      while ((thisLine = br.readLine()) != null) {
        if (thisLine.contains(extend)) {
          int startpos = thisLine.indexOf(extend) + extend.length();
          int stoppos = thisLine.indexOf("\"", startpos + 1);
          String key = thisLine.substring(startpos, stoppos);
          if (!set.contains(key))
            if (!map.containsKey(key)) {
              loadPreparedStatements(key, PmConn);
            }
        } else if (thisLine.contains(reftype)) {
          int startpos = thisLine.indexOf(reftype) + reftype.length();
          int stoppos = thisLine.indexOf("\"", startpos + 1);
          String key = thisLine.substring(startpos, stoppos);
          if (!set.contains(key))
            if (!map.containsKey(key)) set.add(thisLine.substring(startpos, stoppos));
        }
      }
    } catch (Exception e) {
			logger.error("parse error:!", e);
    }
    return set;
  }
/*
 *
*/
  public void loadPreparedStatements(String str, Connection PmConn) {
    try {
      String SELECTFROMAVS = "SELECT * FROM CONFIGTYPES WHERE NAME='" +str + "'";
      PreparedStatement selectRecs = PmConn.prepareStatement(SELECTFROMAVS);
      ResultSet rs = selectRecs.executeQuery();
      while (rs.next()) {
        String name = rs.getString(1);
        String renderDocument = rs.getString(2);
        List<String> l_set = parse(str, renderDocument, PmConn);
        if (!map.containsKey(name)) {
          map.put(name, renderDocument);
          ConfigType type = new ConfigType(renderDocument, this);
          mapType.put(name, type);
        }
      	if (l_set.size() > 0) loadPreparedStatements(l_set, PmConn);
      }
      rs.close();
      if (selectRecs != null) {
        selectRecs.close();
        selectRecs = null;
      }
    } catch (Exception e) {
    	System.out.println("you are here......");
    	e.printStackTrace();
    }
  }
/*
 *
*/
  public void loadPreparedStatements(List<String> set, Connection PmConn) {
    try {
      for (String str : set) {
        String SELECTFROMAVS = "SELECT * FROM CONFIGTYPES WHERE NAME='" +str + "'";
        PreparedStatement selectRecs = PmConn.prepareStatement(SELECTFROMAVS);
        ResultSet rs = selectRecs.executeQuery();
        while (rs.next()) {
          String name = rs.getString(1);
          String renderDocument = rs.getString(2);
          List<String> l_set = parse(name, renderDocument, PmConn);
          if (!map.containsKey(name)) {
            map.put(name, renderDocument);
            ConfigType type = new ConfigType(renderDocument, this);
            mapType.put(name, type);
          }
          if (l_set.size() > 0) loadPreparedStatements(l_set, PmConn);
        }
        rs.close();
        if (selectRecs != null) {
          selectRecs.close();
          selectRecs = null;
        }
      }
    } catch (Exception e) {
    }
  }
/*
 *
*/
  public void loadPreparedStatements(Connection PmConn) {
    String SELECTFROMAVS = "SELECT * FROM CONFIGTYPES WHERE NAME='" + FW_Avs + "'";
    System.out.println("you are here Selectformavs = " + SELECTFROMAVS);
    try {
      selectOperRecs = PmConn.prepareStatement(SELECTFROMAVS);
      ResultSet rs = selectOperRecs.executeQuery();
      List<String> set = null;
      while (rs.next()) {
        String name = rs.getString(1);
        String renderDocument = rs.getString(2);
        set = parse(name, renderDocument, PmConn);
      	if (!map.containsKey(name)) {
          map.put(name, renderDocument);
          ConfigType type = new ConfigType(renderDocument, this);
          mapType.put(name, type);
        }
      }
      rs.close();
      if (selectOperRecs != null) {
        selectOperRecs.close();
        selectOperRecs = null;
      }
      loadPreparedStatements(set, PmConn);

      for (String key : map.keySet()) {
        String value = map.get(key);
      }
    } catch (Exception e) {
		logger.error("Error in loadPreparedStatements()" + e);
		e.printStackTrace();
    } finally {
      try {
        if (selectOperRecs != null) selectOperRecs.close();
      } catch (Exception e) {
        logger.error("Error in close stmt: " + e);
        e.printStackTrace();
      }
    }
  }
/*
 *
*/
  public ConfigType getType(String name) {
    ConfigType t =  mapType.getOrDefault(name, null);
    if (t == null) {
      loadPreparedStatements(name, null);
      t =  mapType.getOrDefault(name, null);
    }
    return t;
  }
/*
 *
*/
  public ConfigTypeDetails[] getDescendantTypeDetails(String name) {
    return null;
  }
/*
 *
*/
/*
  public static void  main(String[] args) {
		FileOutputStream FAvs = null;
		FileOutputStream Fout = null;
    try {
			Parse_Cmd(args);
      Payload_decoder h = new Payload_decoder();
      h.loadPreparedStatements();
			if (!Json_Conf.equals("none")){
				h.ReadJsonConfig();
				h.DumpJsonConfig();
      	Fout = new FileOutputStream(Working_Dir + "/output/" + MainAvses.FileName);
			}
      AVSTypeRepository.getInstance().initialize(h, false);
      zipTool.getInstance().initialize();
      AVSCodec avsCodec = new AVSCodec();
      CSVParser parser = new CSVParser(new FileReader(dataFile), CSVFormat.RFC4180);
			if (DumpAvs)
      	FAvs = new FileOutputStream(Working_Dir + "/output/" + FW_Avs + ".avs");
			Instant TimeStart = Instant.now();
			String CsvHeader = "";
			for(String Field:MainAvses.AvsField){ 
				CsvHeader = CsvHeader + Field.toUpperCase() + MainAvs.Del;
			}
			for(String lv_Field:MainAvs.MSubAvs.SubAvsField){
				CsvHeader = CsvHeader + lv_Field.toUpperCase() + MainAvs.MSubAvs.Del;
			}
			int Total_CDR = 0;
      for (CSVRecord record : parser) {
        String str = record.get(record.size() - 1);
        char F_char = str.charAt(0);
        if (str != null && !str.isEmpty()) {
          str = str + "\n";
          byte[] outByte = str.getBytes();
          byte[] versionedByteArray = null;
          if (F_char == '7') {
            versionedByteArray = zipTool.getInstance().decompress(zipTool.getInstance().asHex(outByte));
          } else {
            versionedByteArray = zipTool.getInstance().appendBeginByte(zipTool.getInstance().asHex(outByte));
          }
          ByteBuffer byteBuffer = ByteBuffer.allocateDirect(versionedByteArray.length);
          byteBuffer.put(versionedByteArray);
          byteBuffer.position(0);
          try {
            AVS Avses = avsCodec.decodeAVS(byteBuffer);
            if (Avses != null) {
							if (DumpAvs && Total_CDR <= Dump_Avs_Cdr ){
								FAvs.write(Avses.toString().getBytes());
								FAvs.write("=========\n".getBytes());
							}
							if (!Json_Conf.equals("none")){
								String AvsStr = null;
								AvsStr = h.Avs2CSV(Avses) + "\n";
								Fout.write(AvsStr.getBytes());
							}
						}
          } catch (ASN1Exception e) {
            e.printStackTrace();
          }
        }
				Total_CDR++;
      }
			Instant TimeEnd = Instant.now();
			Duration timeElapsed = Duration.between(TimeStart, TimeEnd);
      logger.info("INFO: Time taken: " + timeElapsed.toMillis() + " milliseconds");
			if (!Json_Conf.equals("none")){
      	Fout.close();
			}
			if (DumpAvs)
      	FAvs.close();
      h.close();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error" + e);
    }
  }
*/
}
