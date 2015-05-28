package org.fiware.apps.marketplace.it;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2015 CoNWeT Lab, Universidad Politécnica de Madrid
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.catalina.startup.Tomcat;
import org.junit.rules.TemporaryFolder;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;

/**
 * Loads Tomcat and MariaDB in an embedded way to run Integration Tests.
 * A new war is created modifying the database properties to point to a test database.
 * This war is automatically deployed in the embedded Tomcat.
 * @author aitor
 *
 */
public class Environment {
	
	private static final Environment INSTANCE = new Environment();
	
	private TemporaryFolder baseDir;	
	private Tomcat tomcat;
	private DB embeddedDB;
	private AtomicBoolean started = new AtomicBoolean(false);
	
	private final static String MODIFIED_WAR_NAME = "WMarket-Integration.war";
	private final static String DATABASE = "marketplace-test";
	
	/**
	 * Make constructor private in order to control the number of instances (there should be only one).
	 * If the environ cannot be set up, a non-checked exception will be thrown.
	 */
	private Environment() {
		try {
			// Initialize Tomcat and Temporary folder
			tomcat = new Tomcat();
			baseDir = new TemporaryFolder();
			
			// Initialize DB
			int port = getFreePort();
			embeddedDB = DB.newEmbeddedDB(port);

			// Initialize baseDir and the webapps directory
			baseDir.create();
			File webApps = baseDir.newFolder("webapps");
			
			// Set up Tomcat
			tomcat.setPort(0);											// Automatic port
			tomcat.setBaseDir(baseDir.getRoot().getAbsolutePath());		// Base Dir
			tomcat.addContext("/", webApps.getAbsolutePath());			// Context
			
			// Create properties
			Properties properties = new Properties();
			properties.setProperty("jdbc.driverClassName", "com.mysql.jdbc.Driver");
			properties.setProperty("jdbc.url", String.format("jdbc:mysql://localhost:%d/%s", port, DATABASE));
			properties.setProperty("jdbc.username", "root");
			properties.setProperty("jdbc.password", "");
			
			File propertiesFile = baseDir.newFile("properties.properties");
			propertiesFile.createNewFile();
			properties.store(new FileOutputStream(propertiesFile), "");
			
			// Copy the WAR (the original one cannot be modified)
			String projectDirectory = Paths.get(".").toAbsolutePath().toString();
			String modifiedWarPath = projectDirectory + "/target/" + MODIFIED_WAR_NAME;
			
		    Path originalWar = Paths.get(projectDirectory + "/target/WMarket.war");
		    Path modifiedWar = Paths.get(modifiedWarPath);
		    Files.copy(originalWar, modifiedWar, StandardCopyOption.REPLACE_EXISTING);
		    
		    // Modify properties using the file created previously
		    FileSystem fs = FileSystems.newFileSystem(modifiedWar, null);
	        Path fileInsideZipPath = fs.getPath("WEB-INF/classes/properties/database.properties");
	        // Copy properties into the WAR
	        Files.copy(propertiesFile.toPath(), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
	        fs.close();
	        
	        // Add modified WAR       
			tomcat.addWebapp("WMarket", modifiedWarPath);
		} catch (Exception ex) {
			// This will prevent test from starting...
			throw new RuntimeException(ex);
		}

	}
	
	/**
	 * Get the Environment instance (there is only one instance)
	 * @return Environment instance.
	 */
	public static Environment getInstance() {
		return INSTANCE;
	}
	
	private static int getFreePort() throws IOException {
		
		ServerSocket socket = new ServerSocket(0);
		int port = socket.getLocalPort();
		socket.close();
		
		return port;
	}
	
	/**
	 * Method to start Tomcat and MariaDB. If this method has been previously called and
	 * stop() has not been called, Tomcat and MariaDB won't be started again,
	 * @return The port where Tomcat is running
	 * @throws Exception When Tomcat or MariaDB cannot be started
	 */
	public int start() throws Exception {
		if (!started.getAndSet(true)) {
			// Start up
			embeddedDB.start();
			embeddedDB.createDB(DATABASE);
			tomcat.start();
		}
		
		// At this point, Tomcat is supposed to be initialized
		return tomcat.getConnector().getLocalPort();
	}
	
	/**
	 * Method to stop Tomcat and MariaDB. This method is prepared to stop these services only when they
	 * are running
	 * @throws Exception When the services cannot be stopped
	 */
	public void stop() throws Exception {
		if (started.getAndSet(false)) {
			// Stop tomcat
			// It cannot be destroyed. Otherwise, we won't be able to restart it again
			tomcat.stop();
			
			// Stop database
			embeddedDB.stop();
		}
	}
	
	/**
	 * Deletes all the entries existing in the database
	 * @throws ManagedProcessException If an error arises when the database was being cleaned
	 */
	public void cleanDB() throws ManagedProcessException {
		embeddedDB.run("DELETE FROM price_components;", "root", null, DATABASE);
		embeddedDB.run("DELETE FROM price_plans;", "root", null, DATABASE);
		embeddedDB.run("DELETE FROM bookmarks;", "root", null, DATABASE);
		embeddedDB.run("DELETE FROM offerings;", "root", null, DATABASE);
		embeddedDB.run("DELETE FROM descriptions;", "root", null, DATABASE);
		embeddedDB.run("DELETE FROM stores;", "root", null, DATABASE);
		embeddedDB.run("DELETE FROM users;", "root", null, DATABASE);
	}

}
