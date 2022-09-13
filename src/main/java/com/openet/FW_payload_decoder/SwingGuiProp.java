package com.openet.FW_payload_decoder;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.*;
import java.io.*;
import java.nio.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import org.apache.log4j.Logger;
import com.openet.FW_payload_decoder.Password.*;
import cfg.*;
import avsasn.*;
import avsasn.primitivetypes.*;
import avsasn.ber.*;
import avsasn.codec.*;
import zip.zipTool;
import com.openet.jdpc.jdpc;
import java.awt.Container;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.voltdb.VoltTable;
import org.voltdb.client.*;
import components.*;
import javax.xml.bind.DatatypeConverter;
import org.xerial.snappy.Snappy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SwingGuiProp implements ActionListener, PropertyChangeListener {
	final static Logger logger = Logger.getLogger(Payload_decoder.class);
    Boolean Encryption = false;
    private Task task;
    static Properties H2Properties = new Properties();
    static lookupJNI JNI_Handle = new lookupJNI();
    static JFrame SS_Sub_Tool_Frame;
    static final JFileChooser FcUrl = new JFileChooser();
    static final List DBNameList = new ArrayList();
    static JCheckBox FW_Gui_Encryption = null;
    static final jdpc Gl_Jdpc = new jdpc();
    static private lookupRunnable lookupRunnableObject;
    static JComboBox FW_Ver,
    				 FW_FileType;
    AVSCodec avsCodec;
    JProgressBar Prgrbr; 
	JButton H2Conn_But,
			VdbConn_But;
	String[] FW_Ver_Str = {"FW_10.x", "FW_9.x"},
			 File_Type = {"CSV", "SQLITE", "VOLTDB"};
    String Working_Dir,
    		H2PropertiesFile = "/h2SS.properties",
    		Prg_Dir,
    		FW_Version = "FW_10.x",
    		MSISDN_Id,
    		Vdb_Uid,
    		FW_FileType_Str = "CSV",
    		Vdb_Pass;
    JList 	H2StoredList;
    JTextArea PayloadHex,
    		  PayloadAvs,
    		  ResultAvs;
    static JPanel 	Manual_Decoder,
    				Password_Decoder,
    				H2Conf;
    DefaultListModel ListModel;
    private static Connection 	H2Conn = null,
    							GenDBConn = null;
    private static PreparedStatement PStatement = null;
    JTextField  VdbUid_Txt,
    			VdbPass_Txt,
    			VdbUrl_Txt,
    			H2Db_Name_Txt,
    			H2Db_Name_Store_Txt,
    			Passwd_Text,
      		  	Passwd_Enc,
    			MSISDN_ID_Txt,
    			Sqlite_Txt,
    			H2Db_Uid_Txt,
    			H2Db_Pass_Txt,
    			H2Db_Url_Txt;

    class Task extends SwingWorker<Void, Void> {
        private lookupJNI lookupJNIObject;
        public Task(lookupJNI object) {
            super();
            this.lookupJNIObject = object;
        }
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            Random random = new Random();
            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            //Sleep for at least one second to simulate "startup".
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
            while (progress < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException ignore) {}
                //Make random progress.
                progress = lookupJNIObject.getProgress(lookupJNIObject.getIndex());
                setProgress(Math.min(progress, 100));
            }
            DisplayText("Finish loading *.csv file ", JOptionPane.INFORMATION_MESSAGE, "INFO");
            Prgrbr.setIndeterminate(false);
            Prgrbr.setVisible(false);
            return null;
        }
    }
/*
 * 
 */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            Prgrbr.setIndeterminate(false);
            Prgrbr.setValue(progress);
        }
    } 
/*
 * 
 */
    private void H2ConOpen(String PmH2Url, String PmH2Uid, String PmH2Pass) throws Exception {        
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
    private void H2ConClose() throws Exception{
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
    public JProgressBar PlaceJProgressBar(int X, int Y, int PmSize, int Width, JPanel PmPanel) {
    	JProgressBar RetJProgressBar = new JProgressBar();
    	RetJProgressBar.setValue(0); 
    	RetJProgressBar.setStringPainted(true);
    	RetJProgressBar.setBounds(X, Y, PmSize, Width);
    	PmPanel.add(RetJProgressBar);
    	
        return RetJProgressBar;
    }
/*
* 
*/
    public JComboBox PlaceJComboBox(int X, int Y, int PmSize, int Width, JPanel PmPanel, String[] PmItems ) {
    	JComboBox RetJComboBox = new JComboBox(PmItems);
    	RetJComboBox.setBounds(X, Y, PmSize, Width);
    	RetJComboBox.setSelectedItem(FW_Version);
    	RetJComboBox.addActionListener(this);
    	PmPanel.add(RetJComboBox);
    	return RetJComboBox;
    }
/*
 * 
 */
    public JButton PlaceJButton(int X, int Y, int PmSize, int Width, JPanel PmPanel, String PmTxt) {
    	JButton RetTextField = new JButton(PmTxt);
    	RetTextField.setBounds(X, Y, PmSize, Width);
    	RetTextField.addActionListener(this);
        PmPanel.add(RetTextField);
        
        return RetTextField;
    }
 /*
  *	 
 */
    public void PrintException(Exception Ex)
    {
    	StringWriter writer = new StringWriter();
    	PrintWriter printWriter = new PrintWriter( writer );
    	Ex.printStackTrace( printWriter );
    	printWriter.flush();

    	logger.error(writer.toString());
    }
/*
 * 
*/
    public JTextField PlaceTextBox (int X, int Y, int PmSize, int PmHeight, JPanel PmPanel, String PmTxt, boolean PmEnab) {
		
    	JTextField RetTextField = new JTextField(20);
    	RetTextField.setBounds(X, Y, PmSize, PmHeight);
        RetTextField.setEnabled(PmEnab);
        RetTextField.setText(PmTxt);
        PmPanel.add(RetTextField);
    	
        return RetTextField;
    }
/*
 * 
*/
    public JCheckBox PlaceCheckBox(int X, int Y, int PmSize, JPanel PmPanel, String PmTxt) {
    	JCheckBox RetCheckBox = new JCheckBox(PmTxt);
        RetCheckBox.setBounds(X, Y, PmSize, 20);
        PmPanel.add(RetCheckBox);
        RetCheckBox.addActionListener(this);
        return RetCheckBox;
    }
/*
 * 
 */
    public void RefreshFrame(){
    	SS_Sub_Tool_Frame.setVisible(false);
    	SS_Sub_Tool_Frame.setVisible(true);
    }
/*
* 
*/
    public JTextArea PlaceScollableTextField(int X, int Y, int Width, int High, JPanel PmPanel) {
        
        JTextArea LocalTextArea = new JTextArea(High, Width);

		JScrollPane AreaTextScroller = new JScrollPane(LocalTextArea);
		AreaTextScroller.setPreferredSize(new Dimension(250, 80));
		AreaTextScroller.setBounds(X, Y, Width, High);
		LocalTextArea.setLineWrap(true);
		PmPanel.add(AreaTextScroller);
		return LocalTextArea;
    }
/*
 * 
 */
    private void RetrieveNameDetails(String PmName) {
    	H2Db_Url_Txt.setText(H2Properties.getProperty(PmName + ".Url"));
    	H2Db_Pass_Txt.setText(H2Properties.getProperty(PmName + ".Pass"));
    	H2Db_Uid_Txt.setText(H2Properties.getProperty(PmName + ".Uid"));
    	Encryption = (H2Properties.getProperty(PmName + ".Enc").equals("false")?false:true);
    	FW_Gui_Encryption.setSelected(Encryption);
    	FW_Version = H2Properties.getProperty(PmName + ".FWVer");
    	FW_Ver.setSelectedItem(FW_Version);
    }
/*
 * 
 */
    public void PlaceDefaultList(int X, int Y, JPanel PmPanel) {
    	
		LoadPropertiesFile(System.getProperty("WORKING_DIR") + H2PropertiesFile);
    	ListModel = new DefaultListModel();
    	
        Set<String> Keys = H2Properties.stringPropertyNames();
        for (String Key : Keys) {
        	if (!DBNameList.contains(Key.split("\\.")[0])) {
        		DBNameList.add(Key.split("\\.")[0]);
        		ListModel.addElement(Key.split("\\.")[0]);
        	}
        }
        H2StoredList = new JList(ListModel);
        H2StoredList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        H2StoredList.setLayoutOrientation(JList.VERTICAL);
        H2StoredList.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(H2StoredList);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setBounds(X, Y, 140, 110);
		PmPanel.add(listScroller);
    }
/*
 * 
 */
    private void RemoveAndStoreProperties(String PmName) {
    	H2Properties.remove(PmName + ".Uid", H2Db_Uid_Txt.getText());
    	H2Properties.remove(PmName + ".Pass", H2Db_Pass_Txt.getText());
    	H2Properties.remove(PmName + ".Url", H2Db_Url_Txt.getText());
    	H2Properties.remove(PmName + ".FWVer", FW_Version);
    	H2Properties.remove(PmName + ".Enc", (Encryption)?"true":"false");
    	StorePropertiesFile(System.getProperty("WORKING_DIR") + H2PropertiesFile);
    }
 /*
 * 
 */
    private void GetAndStoreProperties(String SaveName) {
    	H2Properties.setProperty(SaveName + ".Uid", H2Db_Uid_Txt.getText());
    	H2Properties.setProperty(SaveName + ".Pass", H2Db_Pass_Txt.getText());
    	H2Properties.setProperty(SaveName + ".Url", H2Db_Url_Txt.getText());
    	H2Properties.setProperty(SaveName + ".FWVer", FW_Version);
    	H2Properties.setProperty(SaveName + ".Enc", (Encryption)?"true":"false");
    	StorePropertiesFile(System.getProperty("WORKING_DIR") + H2PropertiesFile);
    }
/*
 * 
*/
    public JLabel PlaceLabel(int X, int Y, JPanel PmPanel, String PmTxt) {
		JLabel Ret_Lbl = new JLabel(PmTxt);
		Ret_Lbl.setBounds(X, Y, 140, 25);
		PmPanel.add(Ret_Lbl);
		return Ret_Lbl;
    }
/*
 * 
 */
    public void DisplayText(String PmText, int Message, String Level) {
    	JOptionPane.showMessageDialog(H2Conf, PmText, Level, Message);
    }
/*
 * 
 */
	public static void JdpcConnectionClose() {
		try {
			if (PStatement != null)
				PStatement.close();
			if (GenDBConn != null)
				GenDBConn.close();
		}catch (Exception e) {
			logger.error("Failed to close db connection" + e);
		}
	}
/*
 * 
 */
	private static String hexToASCII(String hexValue){
      		StringBuilder output = new StringBuilder("");
      		for (int i = 0; i < hexValue.length(); i += 2){
			System.out.println("i = " + i + " len = " + hexValue.length());
         		String str = hexValue.substring(i, i + 2);
         		output.append((char) Integer.parseInt(str, 16));
      		}
      		return output.toString();
   	}
/*
 * 
 */
    private String Hex2AVS(String PmPayloadHex) {
    	AVS Avses = null;
    	try {
//    		PmPayloadHex = PmPayloadHex + "\n";
    		byte[] outByte = (PmPayloadHex + "\n").getBytes();
    		byte[] versionedByteArray = null;
		String F_2_char = PmPayloadHex.substring(0,2);
		System.out.println("you are here" + PmPayloadHex);
    		if (F_2_char.equals("70") || F_2_char.equals("78")) {
    			versionedByteArray = zipTool.getInstance().decompress(zipTool.getInstance().asHex(outByte));
		}else if (F_2_char.equals("80")) {
			versionedByteArray = zipTool.getInstance().appendBeginByte(zipTool.getInstance().asHex(outByte));
		} else {
        		String OriJson = new String (Snappy.uncompress(DatatypeConverter.parseHexBinary(PmPayloadHex)), "UTF-8");
        		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        		JsonElement jsonElement = JsonParser.parseString(OriJson);
        		return gson.toJson(jsonElement);			
    		}
    		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(versionedByteArray.length);
    		byteBuffer.put(versionedByteArray);
    		byteBuffer.position(0);
    		logger.info("byteBuffer: " + zipTool.getInstance().asString(versionedByteArray));
		for(int i=0; i< versionedByteArray.length ; i++) {
         		System.out.print(versionedByteArray[i]);
      		}
		System.out.println("\n");
    		logger.info("versionedByteArray: " + versionedByteArray);
		Avses = avsCodec.decodeAVS(byteBuffer);
    	}catch(Exception e) {
    		PrintException(e);
    		logger.error("Error: Can't decode avs:" + e);
        	DisplayText("Failled to decode AVS! ", JOptionPane.ERROR_MESSAGE, "ERROR");
    	}
    	return Avses.toString();
    }
 /*
  * 
  */
    private String GetVoltdbTable() {
    	String RetStr = null;
    	try {
            CallableStatement Proc = GenDBConn.prepareCall("{call @SystemCatalog(?)}");
            Proc.setString(1, "TABLES");
            ResultSet results = Proc.executeQuery();
    		while (results.next()) {
    			if (results.getString(3).endsWith("DOCUMENT")) {
    				RetStr = results.getString(3);
    				break;
    			}
    		}
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	System.out.println("table = " + RetStr);
		return RetStr;
    }
 /*
  * 
  */
    private void IninitialezeVdb() throws Exception {
    	String VdbUrl = "";
    	try {
        	Class.forName("org.voltdb.jdbc.Driver");
    		String Port = VdbUrl_Txt.getText().split("\\:")[1];
    		for (String Host : (String[])VdbUrl_Txt.getText().split("\\:")[0].split(",")) {
    			if (VdbUrl.isEmpty()) {
    				VdbUrl = VdbUrl + Host + ":" + Port;
    			}else {
    				VdbUrl = VdbUrl + "," + Host + ":" + Port;
    			}
    		}
    		JdpcConnectionClose();
    		GenDBConn = Gl_Jdpc.GetJdpcConnection("jdbc:voltdb://" + VdbUrl + "?autoreconnect=true");
    		String MySql = " select payload from " + GetVoltdbTable() + " where DOC_ID=?";
    		PStatement = Gl_Jdpc.PrepareMySqlStatement(MySql, GenDBConn);
    	}catch(Exception e) {
    		GenDBConn = null;
    		logger.error("Failled to initialize Voltdb: " + e);
    		throw new Exception(e);
    	}
    }
/*
 * 
 */
    private void InitializeSqlite(String PmDbFile) throws Exception {
    	try {
    		JdpcConnectionClose();
    		GenDBConn = Gl_Jdpc.GetJdpcConnection("jdbc:sqlite:" + PmDbFile);
    		String mysql = "select payload from Subscriber where SubId = ?";
        	PStatement = Gl_Jdpc.PrepareMySqlStatement(mysql, GenDBConn);
        	DisplayText("Successfully connect to database ", JOptionPane.INFORMATION_MESSAGE, "INFO");
    	}catch(Exception e) {
    		logger.error("Error: Can't sqlite file File:" + Sqlite_Txt.getText() + e);
        	DisplayText("Can't connect to database! " + Sqlite_Txt.getText(), JOptionPane.ERROR_MESSAGE, "ERROR");
    		throw new Exception(e);
    	}
    }
/*
 * 
 */
    private void IninitializeH2() throws Exception {
    	
    	try {
    		String Pass = H2Db_Pass_Txt.getText();
    		logger.info("FW_Version = " + FW_Version + "Pass = " + Pass);
    		if (Encryption) {
    			Password p = new Password(FW_Version, "decode", Pass);
    			Pass = p.Get_PlainText();
    		}
    		logger.info("Pass = " + Pass);
    		String Url = "jdbc:h2:" + H2Db_Url_Txt.getText().split("\\.")[0] + ";JMX=TRUE;LOCK_TIMEOUT=20000;MVCC=TRUE;";
    		logger.info("url = " + Url);
    		H2ConOpen(Url, H2Db_Uid_Txt.getText(), Pass);
    		Payload_decoder h = new Payload_decoder(H2Conn);
    		h.loadPreparedStatements();
    		AVSTypeRepository.getInstance().initialize(h, false);
    		zipTool.getInstance().initialize();
    		avsCodec = new AVSCodec();
    	}catch(Exception e) {
    		logger.error("Failled to initialize H2 Database" + e);
    		PrintException(e);
    		throw new Exception (e);
    	}
    }
/*
* 
*/
    public void actionPerformed(ActionEvent e) {
    	String Action = e.getActionCommand();
        if (Action.equals("Select")) {
        	RetrieveNameDetails((String)H2StoredList.getSelectedValue());
        }else if (Action.equals("Del")) {
        	RemoveAndStoreProperties((String)H2StoredList.getSelectedValue());
        	ListModel.remove(H2StoredList.getSelectedIndex());
        }else if (Action.contains("Encryption")) {
        	Encryption = !Encryption;
        }else if (Action.equals("Save")) {
        	String SaveName = H2Db_Name_Store_Txt.getText();
        	if (!SaveName.isEmpty()) {
        		GetAndStoreProperties(SaveName);
        		ListModel.addElement(SaveName);
        		SetH2EditableText(false);
        	}else {
        		DisplayText("Save Name field is empty! ", JOptionPane.ERROR_MESSAGE, "ERROR"); 
        	}
        }else if (Action.contentEquals("Update")) {
        	SetH2EditableText(true);
        }else if (Action.equals("Connect")) {
        	try {
        		JButton Conn_But = (JButton)e.getSource();
        		if (Conn_But == H2Conn_But) {
        			IninitializeH2();
        		}else{
        			IninitialezeVdb();
        		}
        		DisplayText("Successful!", JOptionPane.INFORMATION_MESSAGE, "INFO");
        	}catch(Exception ex) {
        		logger.error("Error: Can't connect to H2 db File:" + H2Db_Uid_Txt.getText() + ex);
            	DisplayText("Can't connect to database! " + H2Db_Url_Txt.getText(), JOptionPane.ERROR_MESSAGE, "ERROR");
        	}	
        }else if (Action.equals("Decode")){
    		String LcPayloadHex = PayloadHex.getText();
        	try {
                if (!LcPayloadHex.isEmpty()) {
                	logger.info (LcPayloadHex);
                	PayloadAvs.setText(Hex2AVS(LcPayloadHex));
                }
        	}catch(Exception ex) {
        		logger.error(ex);
        	}
        }else if (Action.equals("Decrypt")){
    		String LcPasswdEnc = Passwd_Enc.getText();
        	try {
                if (!LcPasswdEnc.isEmpty()) {
                	logger.info ("encrypted pass: " + LcPasswdEnc + " - FW_Version = " + FW_Version);
        			Password p = new Password(FW_Version, "decode", LcPasswdEnc);
        			Passwd_Text.setText(p.Get_PlainText());
                }
        	}catch(Exception ex) {
        		logger.error(ex);
        	}
        }else if (Action.equals("...")) {
            int returnVal = FcUrl.showOpenDialog(H2Conf);   
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	File FcDbFile = FcUrl.getSelectedFile();
            	H2Db_Url_Txt.setText(FcDbFile.getAbsolutePath() + FcDbFile.getName());
            }
        }else if (Action.equals("Open")) {
        	try {
        		int returnVal = FcUrl.showOpenDialog(H2Conf);
        		if (returnVal == JFileChooser.APPROVE_OPTION) {
        			Class.forName("org.sqlite.JDBC");
        			File FcDbFile = FcUrl.getSelectedFile();
        			String DbFile = (String)FcDbFile.getAbsolutePath();
        			Sqlite_Txt.setText(DbFile);
        			if (FW_FileType_Str.equals("SQLITE")) {
        				InitializeSqlite(DbFile);
        			}else {     				
                        		task = new Task(JNI_Handle);
                        		task.addPropertyChangeListener(this);
                        		task.execute();
        				JNI_Handle.init(DbFile);
                        		lookupRunnableObject = new components.lookupRunnable(JNI_Handle, DbFile);
                        		Thread t = new Thread(lookupRunnableObject);
                        		t.start();
        				int progress = 0;
        	            		Prgrbr.setValue(0);
        	            		Prgrbr.setIndeterminate(true);
        	            		Prgrbr.setVisible(true);
        			}
        		}
        	}catch (Exception ex){
        		System.out.println("you are here" + ex);
        	}
        }else if (Action.equals("Search")){
        	int LcCdr = 0;
    		try {
				String MSISDN_Id_Txt = MSISDN_ID_Txt.getText();
    			if (FW_FileType_Str.equals("SQLITE") || FW_FileType_Str.equals("VOLTDB")) {
    				for (String Id : MSISDN_Id_Txt.split(",")) {
    					ResultSet myresultset = Gl_Jdpc.ExecuteSql (Id, PStatement);
    					ResultAvs.append("Voltdb MSISDN: " + Id + "\n");
    					while (myresultset.next()) {
    						ResultAvs.append ("Record #" + LcCdr + ":\n");
    						ResultAvs.append (Hex2AVS(myresultset.getString("payload")));
    					}
    					LcCdr++;
    				}
//    			}else if ( FW_FileType_Str.equals("VOLTDB")) {
    			}else {
    				for (String Id : MSISDN_Id_Txt.split(",")) {
    					ResultAvs.append("MSISDN: " + Id + "\n");
    					CSVParser CsvParser = new CSVParser(new StringReader(JNI_Handle.lookup(Id)), CSVFormat.RFC4180);
    					for (CSVRecord record : CsvParser) {	
    						String Payload = record.get(record.size() - 1);
    						ResultAvs.append ("Record #" + LcCdr + ":\n");
    						ResultAvs.append (Hex2AVS(Payload));
    						LcCdr++;
    					}
    				}
    			}
    		}catch(Exception Se) {
    			logger.error("Failled to execute statement");
    		}

        }else if (Action.equals("comboBoxChanged")){
        	if (e.getSource().equals(FW_Ver)) {
        		FW_Version =  (String) FW_Ver.getSelectedItem();
        		logger.info("New FW version is selected " + FW_Version);
        	}else {
        		FW_FileType_Str = (String) FW_FileType.getSelectedItem();
        	}
        }
    }
/*
 * 
 */
    public static void main(String[] args) {
	System.out.println("this is a test");
    	JTabbedPane DecodeTabPane = new JTabbedPane();
    	SS_Sub_Tool_Frame = new JFrame("Subscriber and Session Store Properties");
    	SS_Sub_Tool_Frame.setSize(800, 400);
    	SS_Sub_Tool_Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingGuiProp GuiProp = new SwingGuiProp();
        JPanel Decode = new JPanel();
        H2Conf = new JPanel();
        Manual_Decoder = new JPanel();
        Password_Decoder = new JPanel();
		
		DecodeTabPane.addTab("Decoder", Decode);
		DecodeTabPane.setMnemonicAt(0, KeyEvent.VK_2);
        DecodeTabPane.addTab("H2 Configuration", H2Conf);
        DecodeTabPane.addTab("Manual Decoder", Manual_Decoder);
        DecodeTabPane.addTab("Password Decoder", Password_Decoder);
        
        SS_Sub_Tool_Frame.add(DecodeTabPane);

        GuiProp.placeComponents(Decode);
        GuiProp.placeComponents4H2(H2Conf);
        GuiProp.placeComponentsManual(Manual_Decoder);
        GuiProp.placeComponentsPassword(Password_Decoder);

        SS_Sub_Tool_Frame.setVisible(true);
    }
/*
* 
*/
	private void StorePropertiesFile(String PmFile) {
		try {
			Writer inputStream = new FileWriter(PmFile);
			H2Properties.store(inputStream, "h2 properties");
			inputStream.close();
		} catch (FileNotFoundException e) {
			logger.error("File not found exception!" + e);
		} catch (IOException e) {
			logger.error("IO exception!" + e);
		}		
	}
/*
 * 
 */
	private void LoadPropertiesFile(String PmFile){
		try {
			File FilePtr = new File(PmFile);
			if(!FilePtr.exists()){
				FilePtr.createNewFile();
			}
			FileInputStream FilePropertiesIn = new FileInputStream(FilePtr);
			H2Properties.load(FilePropertiesIn);
			FilePropertiesIn.close();
		} catch (FileNotFoundException e) {
			logger.error("LoadPropertiesFile: File not found!", e);
		} catch (IOException e) {
			logger.error("LoadPropertiesFile: IOException!", e);
		}
	}
/*
 * 
 */
	private void SetH2EditableText(boolean PmEdit) {
		H2Db_Uid_Txt.setEditable(PmEdit);
		H2Db_Uid_Txt.setEnabled(PmEdit);
		H2Db_Pass_Txt.setEnabled(PmEdit);
		H2Db_Url_Txt.setEnabled(PmEdit);
		FW_Ver.setEnabled(PmEdit);
		FW_Gui_Encryption.setEnabled(PmEdit);
	}
/*
 * 
 */
	private void placeComponentsPassword(JPanel PmPanel){
		PmPanel.setBorder(BorderFactory.createTitledBorder("FW password decoder"));
		PmPanel.setLayout(null);
		PlaceLabel (10, 20, PmPanel, "Encrypted Password:");
		Passwd_Enc = PlaceTextBox(10, 40, 360, 25, PmPanel, null, true);
		PlaceLabel (380, 20, PmPanel, "Password in plain text:");
		Passwd_Text = PlaceTextBox(380, 40, 360, 25, PmPanel, null, true);
		PlaceJButton(10, 65, 80, 25, PmPanel, "Decrypt");
        FW_Ver = PlaceJComboBox(100, 65, 110, 25, PmPanel, FW_Ver_Str);
        FW_Ver.setEditable(false);
		}
/*
 * 
 */
	private void placeComponentsManual(JPanel PmPanel){
		PmPanel.setBorder(BorderFactory.createTitledBorder("Single CDR decoder"));
		PmPanel.setLayout(null);
		PlaceLabel (10, 20, PmPanel, "Payload in Hex format:");
		PayloadHex = PlaceScollableTextField(10, 40, 360, 260, PmPanel);
		PlaceLabel (380, 20, PmPanel, "Payload in AVS format:");
		PayloadAvs = PlaceScollableTextField(380, 40, 360, 260, PmPanel);
		PlaceJButton(10, 300, 80, 25, PmPanel, "Decode");
	}
/*
 * 
*/
	private void placeComponents4H2(JPanel PmPanel) {
		
		PmPanel.setBorder(BorderFactory.createTitledBorder("H2 Database Active Session"));
		PmPanel.setLayout(null);
		PlaceLabel (10, 20, PmPanel, "User name:");
		H2Db_Uid_Txt = PlaceTextBox(10, 40, 190, 25, PmPanel, null, false);
		PlaceLabel (210, 20, PmPanel, "Password:");
		H2Db_Pass_Txt = PlaceTextBox(210, 40, 190, 25, PmPanel, null, false);
        PlaceLabel (10, 100, PmPanel, "H2 Database Url:");
        H2Db_Url_Txt = PlaceTextBox(10, 125, 370, 25, PmPanel, null, false);
		PlaceJButton(380, 125, 20, 25, PmPanel, "...");
        FW_Gui_Encryption = PlaceCheckBox(10, 75, 200, PmPanel, "FW Gui Password Encryption");
        PlaceLabel (210, 75, PmPanel, "FW_Version");
        FW_Ver = PlaceJComboBox(290, 80, 110, 20, PmPanel, FW_Ver_Str);
        FW_Ver.setEditable(false);
        PlaceLabel (450, 20, PmPanel, "Database Connections");
        PlaceDefaultList(450,40, PmPanel);
		PlaceJButton(450, 150, 70, 25, PmPanel, "Select");
		PlaceJButton(520, 150, 70, 25, PmPanel, "Del");
		PlaceJButton(10, 150, 70, 25, PmPanel, "Save");
		H2Db_Name_Store_Txt = PlaceTextBox(80, 150, 150, 25, PmPanel, null, true);
		PlaceJButton(230, 150, 80, 25, PmPanel, "Update");
		H2Conn_But = PlaceJButton(310, 150, 90, 25, PmPanel, "Connect");
	}
/*
* 
*/
	private void placeComponents(JPanel PmPanel) {
		PmPanel.setBorder(BorderFactory.createTitledBorder("Database"));
		PmPanel.setLayout(null);
        PlaceLabel (10, 20, PmPanel, "File type");
        FW_FileType = PlaceJComboBox(100, 20, 130, 20, PmPanel, File_Type);
        FW_FileType.setEditable(false);

        Sqlite_Txt = PlaceTextBox(10, 40, 220, 20, PmPanel, null, false);
        PlaceJButton(240, 40, 80, 20, PmPanel, "Open");
        PlaceLabel (10, 60, PmPanel, "MSISDN Id");
        MSISDN_ID_Txt = PlaceTextBox(10, 80, 220, 20, PmPanel, null, true);
        PlaceJButton(240, 80, 80, 20, PmPanel, "Search");
		PlaceLabel (10, 100, PmPanel, "Result in AVS:");
		ResultAvs = PlaceScollableTextField(10, 120, 670, 200, PmPanel);

        PlaceLabel (340, 20, PmPanel, "VoltDb UserName");
        VdbUid_Txt = PlaceTextBox(340, 40, 160, 20, PmPanel, null, true);
        PlaceLabel (520, 20, PmPanel, "VoltDb Password");
        VdbPass_Txt = PlaceTextBox(520, 40, 160, 20, PmPanel, null, true);
        PlaceLabel (340, 60, PmPanel, "VoltDb Hosts:Port");
        VdbUrl_Txt = PlaceTextBox(340, 80, 220, 20, PmPanel, null, true);
        VdbConn_But = PlaceJButton(580, 80, 100, 20, PmPanel, "Connect");
        Prgrbr = PlaceJProgressBar (240, 20, 80, 20, PmPanel);
    }
    public void windowOpened(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}

}
