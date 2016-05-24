/*
 * Copyright 2006-2007 The Scriptella Project Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foodtruckopt;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.foodtruckopt.dbinit.DbScriptsManager;

/**
 * Database initializer.
 * <p>
 * Initializes DB on web application startup.
 */
public class WebDbInitializer {

	private String WEBINIT_DBXML_PATH = "/dbScripts.xml";

	public void contextInitialized() throws Exception {

		Properties prop = getDbProperties();
		InputStream in1 = getClass().getResourceAsStream("/dbScripts.xml");

		DbScriptsManager.main(in1, prop);
	}

	private Properties getDbProperties() throws IOException, URISyntaxException {
		Properties prop = new Properties();
		InputStream in = getClass().getResourceAsStream("/application.properties");

		prop.load(in);

		if (isHeroku()) {
			URI dbUrl = new URI(System.getProperty("database.url"));
			prop.setProperty("driverClass", "org.postgresql.Driver");
			System.out.println("***dbHost=" + dbUrl.getHost() + "***" + dbUrl.getUserInfo().split(":")[0] + "****" +  dbUrl.getUserInfo().split(":")[1]);
			prop.setProperty("url", "jdbc:postgresql://" + dbUrl.getHost() + ":" + dbUrl.getPort() + dbUrl.getPath());
			prop.setProperty("user", dbUrl.getUserInfo().split(":")[0]);
			prop.setProperty("pwd", dbUrl.getUserInfo().split(":")[1]);

		} else {
			prop.setProperty("driverClass", "org.postgresql.Driver");
			prop.setProperty("url", prop.getProperty("spring.datasource.url"));
			prop.setProperty("user", prop.getProperty("spring.datasource.username"));
			prop.setProperty("pwd", prop.getProperty("spring.datasource.password"));
			
			System.out.println("url=" + prop.getProperty("spring.datasource.url"));
		}
		return prop;
	}

	private boolean isHeroku() {
		String dbUrlStr = System.getenv("DATABASE_URL");
		System.out.println("dbUrlStr=" + dbUrlStr);
		if (dbUrlStr == null || dbUrlStr.isEmpty()) {
			return false;
		}
		return true;
	}

}
