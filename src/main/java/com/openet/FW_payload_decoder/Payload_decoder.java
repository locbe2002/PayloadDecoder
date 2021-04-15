package com.openet.FW_payload_decoder;
import org.apache.commons.io.FileUtils;
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
import cfg.*;
import avsasn.*;
import avsasn.primitivetypes.*;
import avsasn.ber.*;
import avsasn.codec.*;
import zip.zipTool;
import java.math.BigInteger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import com.openet.FW_payload_decoder.Password.*;
/*
 *
*/
public class Payload_decoder implements ObjectRepository {

  private static Properties props = null;
  private static Properties Allprops = null;
  private static Connection conn = null;
  private static Connection H2Conn = null;
  private static String H2_Uid = null;
  private static String H2_Pass_Encrypted = null;
  private static String H2_Pass = null;
  private static String FW_Avs = null;
  private static String Working_Dir = null;
  private static String FW_Version = null;
  private static String dataFile;
  private static String Prg_dir;

  private static String propertiesFile = null;
  private static String H2Url = null;
  final static Logger logger = Logger.getLogger(Payload_decoder.class);

  private PreparedStatement selectOperRecs = null;
  private HashMap<String, String> map = new HashMap<>();
  private HashMap<String, ConfigType> mapType = new HashMap<>();
/*
 *
*/
  Payload_decoder(Connection PmConn) throws Exception {
	  conn = PmConn;
  }
/*
 *
*/
  public static String getProperty(String key, String Default, Properties pm_prop) {
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
  private static void Init() throws Exception {
	try {
	  props = loadProperties(propertiesFile);
	  Prg_dir = getProperty("PRG_DIR", null, props);
	  Working_Dir = getProperty("WORKING_DIR", null, props);
	  Allprops = loadProperties(Working_Dir + "/MSDB/AllServer.properties");
	  H2_Uid = getProperty("com.openet.util.JDBCConnection.username", null, Allprops);
	  H2_Pass_Encrypted = getProperty("com.openet.util.JDBCConnection.password", null, Allprops);
	  Password p = new Password(FW_Version, "decode", H2_Pass_Encrypted);
	  H2_Pass = p.Get_PlainText();
	  logger.info("Connecting to DB using username: " + H2_Uid + ", passwd: " + H2_Pass + ", URL: " + H2Url);
	  H2ConOpen(H2Url, H2_Uid, H2_Pass);
	} catch (Exception e) {
	  logger.error("Error locating h2 Driver:!", e);
	}   
  }
/*
 *
*/
  private static void H2ConOpen(String PmH2Url, String PmH2Uid, String PmH2Pass) throws Exception {        
    try {
      Class.forName("org.h2.Driver");
      H2ConClose();
      H2Conn = DriverManager.getConnection(PmH2Url, PmH2Uid, PmH2Pass);
    } catch (Exception e) {
      logger.error("Connecting to database:!", e);
      throw new Exception (e);
    }
  }
/*
 * 
*/
  private static void H2ConClose() throws Exception{
    try {
      if (H2Conn != null) {
        H2Conn.close();
      }
    } catch (Exception e) {
      logger.error("Error closing H2 database connection");
      throw new Exception (e);
    }
  }
/*
 *
*/
  private static Properties loadProperties(String Pro_File) {
    Properties Ret_Prop = new Properties();
    try {
      Ret_Prop.load(new FileInputStream(Pro_File));
    } catch (Exception e) {
      props = null;
    }
		return Ret_Prop;
  }
/*
 *
*/
  private List<String> parse(String name, String renderDocument) {
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
              loadPreparedStatements(key);
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
  private void loadPreparedStatements(String str) {
    try {
      String SELECTFROMAVS = "SELECT * FROM CONFIGTYPES WHERE NAME='" +str + "'";
      PreparedStatement selectRecs = conn.prepareStatement(SELECTFROMAVS);
      ResultSet rs = selectRecs.executeQuery();
      while (rs.next()) {
        String name = rs.getString(1);
        String renderDocument = rs.getString(2);
        List<String> l_set = parse(str, renderDocument);
        if (!map.containsKey(name)) {
          map.put(name, renderDocument);
          ConfigType type = new ConfigType(renderDocument, this);
          mapType.put(name, type);
        }
      	if (l_set.size() > 0) loadPreparedStatements(l_set);
      }
      rs.close();
      if (selectRecs != null) {
        selectRecs.close();
        selectRecs = null;
      }
    } catch (Exception e) {
    }
  }
/*
 *
*/
  private void loadPreparedStatements(List<String> set) {
    try {
      for (String str : set) {
        String SELECTFROMAVS = "SELECT * FROM CONFIGTYPES WHERE NAME='" +str + "'";
        PreparedStatement selectRecs = conn.prepareStatement(SELECTFROMAVS);
        ResultSet rs = selectRecs.executeQuery();
        while (rs.next()) {
          String name = rs.getString(1);
          String renderDocument = rs.getString(2);
          List<String> l_set = parse(name, renderDocument);
          if (!map.containsKey(name)) {
            map.put(name, renderDocument);
            ConfigType type = new ConfigType(renderDocument, this);
            mapType.put(name, type);
          }
          if (l_set.size() > 0) loadPreparedStatements(l_set);
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
  public void loadPreparedStatements() {
    String SELECTFROMAVS = "SELECT * FROM CONFIGTYPES WHERE NAME='" + FW_Avs + "'";
    logger.error("SELECTFROMAVS = " + SELECTFROMAVS);
    try {
      selectOperRecs = conn.prepareStatement(SELECTFROMAVS);
      ResultSet rs = selectOperRecs.executeQuery();
      List<String> set = null;
      while (rs.next()) {
        String name = rs.getString(1);
        String renderDocument = rs.getString(2);
        set = parse(name, renderDocument);
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
      loadPreparedStatements(set);

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
      loadPreparedStatements(name);
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
  public byte[] loadDataFileToByteArray(String filename) throws Exception {
    File dataFile = new File(filename);
    if (dataFile.length() == 0) throw new Exception("Data file " + filename + ": size should be greater than 0.");
    FileInputStream in = null;
    try {
      in = new FileInputStream(dataFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new Exception(e.getMessage());
    }
    if (dataFile.length() > Integer.MAX_VALUE) throw new Exception("Data file " + dataFile+ " too large, should be no larger than "+ Integer.toString(Integer.MAX_VALUE));
    int inByteArrayLength = (int) dataFile.length();
    byte[] bytearray = new byte[inByteArrayLength];
    try {
    	int bytesRead = in.read(bytearray);
    } catch (IOException e) {
      e.printStackTrace();
      throw new Exception("Failed to read required number of bytes ("+ dataFile.length() + ") from dataFile: " + dataFile);
    }
    return bytearray;
  }
/*
 *
*/
	public static void Parse_Cmd(String[] Args){
		Options options = new Options();
		Option ConfFile = new Option("c", "conf", true, "the configuration file");
		ConfFile.setRequired(true);
		options.addOption(ConfFile);

		Option InFile = new Option("i", "input", true, "input data file path");
		InFile.setRequired(true);
		options.addOption(InFile);

		Option FW_InAvs = new Option("a", "avs", true, "Target avs");
		FW_InAvs.setRequired(true);
		options.addOption(FW_InAvs);

		Option Url = new Option("u", "url", true, "h2 url");
		Url.setRequired(true);
		options.addOption(Url);

		Option FW_Ver = new Option("v", "fw_ver", true, "FW version");
		FW_Ver.setRequired(true);
		options.addOption(FW_Ver);

		CommandLineParser CliParser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = CliParser.parse(options, Args);
		} catch (ParseException e) {
			logger.error("Error parsing command line option! ", e);
			formatter.printHelp("utility-name", options);
		}
			dataFile = cmd.getOptionValue("input");
			FW_Avs = cmd.getOptionValue("avs");
			H2Url = cmd.getOptionValue("url");
			propertiesFile = cmd.getOptionValue("conf");
			FW_Version = cmd.getOptionValue("fw_ver");
	}
/*
 *
*/
  public static void  main(String[] args) {
	FileOutputStream FAvs = null;
	FileOutputStream Fout = null;
    try {
	    	System.out.println("Welcome to github building");
		Parse_Cmd(args);
		Init();
		Payload_decoder h = new Payload_decoder(H2Conn);
		h.loadPreparedStatements();
		AVSTypeRepository.getInstance().initialize(h, false);
		zipTool.getInstance().initialize();
		AVSCodec avsCodec = new AVSCodec();
		CSVParser parser = new CSVParser(new FileReader(dataFile), CSVFormat.RFC4180);
      	FAvs = new FileOutputStream(Working_Dir + "/output/" + FW_Avs + ".avs");
		Instant TimeStart = Instant.now();
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
						FAvs.write(Avses.toString().getBytes());
						FAvs.write("=========\n".getBytes());
					}
				} catch (ASN1Exception e) {
					e.printStackTrace();
				}
			}
		}
		Instant TimeEnd = Instant.now();
		Duration timeElapsed = Duration.between(TimeStart, TimeEnd);
		logger.info("INFO: Time taken: " + timeElapsed.toMillis() + " milliseconds");
      	FAvs.close();
      	H2ConClose();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error" + e);
    }
  }
}
