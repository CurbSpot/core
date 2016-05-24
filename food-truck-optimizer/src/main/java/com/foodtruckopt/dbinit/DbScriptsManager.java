package com.foodtruckopt.dbinit;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbScriptsManager {

	private static final Log logger = LogFactory.getLog(DbScriptsManager.class);

	private static final String EXCELERP_VERSION = "EXCELERP_VERSION";
	public static void main(InputStream in, Properties prop) {

		try {
			JAXBContext jc = JAXBContext.newInstance(DbScripts.class);

			Unmarshaller u = jc.createUnmarshaller();

			DbScripts ds = (DbScripts) u.unmarshal(in);

			logger.debug("Successfully loaded DbScripts from file! ");

			applyScripts(ds, prop);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void applyScripts(DbScripts ds, Properties prop) {
		PreparedStatement ps = null;
		Statement st = null;
		Script currentScript = null;
		Connection conn = null;
		long scriptVersion = 0;

		List<Scripts> scriptsList = ds.getScripts();
		try {
			Class.forName(prop.getProperty("driverClass"));
			conn = DriverManager.getConnection(prop.getProperty("url"),
					prop.getProperty("user"), prop.getProperty("pwd"));
			conn.setAutoCommit(false);
			if (scriptsList != null) {
				for (Scripts scripts : scriptsList) {
					scriptVersion = scripts.getVersion();
					logger.debug("Processing Script Version : " + scriptVersion);
					if (scripts.getVersion() <= getVersion(prop)) {
						logger.debug("Already applied version : "
								+ scriptVersion + ". Skipping... ");
						continue;
					}
					logger.info("Applying version : " + scriptVersion);
					int totalAffectedRows = 0;
					int affectedRows = 0;
					for (Script script : scripts.getScriptList()) {
						currentScript = script;

						logger.debug("Executing script: " + script);

						if (script.getType().equals("DML")) {
							ps = conn.prepareStatement(script.getValue());
							affectedRows = ps.executeUpdate();
							logger.debug(" Affected rows : " + affectedRows);
							ps.close();
						} else {
							st = conn.createStatement();
							st.execute(script.getValue());
							logger.debug(" DDL Executed successfully!");
							st.close();
						}

						totalAffectedRows += affectedRows;
					}
					if (totalAffectedRows > 0) {
						logger.info(" Affected Rows for Script Version-"
								+ scriptVersion + " : " + totalAffectedRows);
					}
					ps = conn
							.prepareStatement("update " + EXCELERP_VERSION + " set version='"
									+ scriptVersion + "'");
					logger.info("GOSMARTER Database successfully updated to version : "
							+ scriptVersion);
					ps.executeUpdate();
					ps.close();
					conn.commit();

				}
			}
			// conn.setAutoCommit(true);
			conn.close();
		} catch (SQLException e) {
			logger.error("Unkown error while upgrading database. \n"
					+ "Script Version : " + scriptVersion + "currentScript : "
					+ currentScript, e);
			logger.error("Database is in inconsistent state!");
			try {
				conn.rollback();
				conn.close();
				logger.info("################# Changes rolled back succesfully : ");
			} catch (SQLException e1) {
				logger.error("Error while rolling back!", e1);
			}
			throw new RuntimeException(
					"Unkown error while upgrading database.", e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Long getVersion(Properties prop) throws SQLException {
		Long val = 0L;
		PreparedStatement ps = null;
		Connection conn = DriverManager.getConnection(prop.getProperty("url"),
				prop.getProperty("user"), prop.getProperty("pwd"));
		String insertVersionAndGroup = "insert into " + EXCELERP_VERSION + " values (0)";
		try {
			ps = conn.prepareStatement("select version from " + EXCELERP_VERSION);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				val = rs.getLong(1);
			} else {
				ps = conn.prepareStatement(insertVersionAndGroup);
				ps.executeUpdate();
			}
			rs.close();
		} catch (SQLException e) {

			try {
				// TODO: Table does not exist, create a table with one record
				conn.setAutoCommit(false);
				logger.debug("table does not exist so creating " +  EXCELERP_VERSION + " table");
				ps = conn.prepareStatement("create table " + EXCELERP_VERSION + "( version INTEGER NOT NULL )");
				ps.executeUpdate();
				logger.debug("created " + EXCELERP_VERSION + " table");
				ps = conn.prepareStatement(insertVersionAndGroup);
				logger.debug("inserted the record");
				ps.executeUpdate();
				conn.commit();
			} catch (Exception e1) {
				e1.printStackTrace();
				conn.rollback();
			} finally {
				conn.setAutoCommit(true);
				ps.close();
			}
		}
		return val;
	}
}
