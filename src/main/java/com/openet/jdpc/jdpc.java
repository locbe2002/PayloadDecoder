package com.openet.jdpc;

import java.sql.*;
import org.apache.log4j.Logger;

import com.openet.FW_payload_decoder.Payload_decoder;
/*
 * 
 */
public class jdpc {
	final static Logger logger = Logger.getLogger(jdpc.class);
	public jdpc() {
	}
/*
 * 
 */
	public static Connection GetJdpcConnection(String PmUrl) throws Exception{
        Connection RetConn = null;
        try {
          RetConn = DriverManager.getConnection(PmUrl);
            logger.info("Connection to database has been established. " + PmUrl);
        } catch (SQLException e) {
            logger.error("Can't connect to database Url: " + PmUrl + e);
            throw new Exception (e);
        }
        return RetConn;
    }
/*
 * 
 */
    public static PreparedStatement PrepareMySqlStatement(String PmSql, Connection PmConn) throws Exception{
    	PreparedStatement RetPstmt = null;
    	try {
    		RetPstmt  = PmConn.prepareStatement(PmSql);
    	} catch (SQLException e) {
    		logger.error("Error: can't create statement sql " + PmSql + e);
    		throw new Exception (e);
    	}
    	return RetPstmt;
    }
/*
 * 
 */
    public static ResultSet ExecuteSql(String SubId, PreparedStatement PmStatement) {

    	ResultSet RetResultSet = null;
        try {
        	PmStatement.setString(1,SubId);
//        	PmStatement.setObject(1, SubId);
        	RetResultSet = PmStatement.executeQuery();
        } catch (SQLException e) {
            logger.error("Fail to execute sql statement sql " + e);
        }
        return RetResultSet;
    }
/*
 * 
 */
    public static Connection createNewDatabase(String PmUrl) {
    	Connection RetConn = null;
        try {
        	RetConn = DriverManager.getConnection(PmUrl);
            if (RetConn != null) {
                DatabaseMetaData meta = RetConn.getMetaData();
                logger.info("The driver name is " + meta.getDriverName());
                logger.info("A new database has been created.");
            }
 
        } catch (SQLException e) {
            logger.error("Can't create sqlite database!: " + e);
        }
        return RetConn;
    }
}