/*-
 * ========================LICENSE_START=================================
 * TeamApps.org UniversalDB Integration
 * ---
 * Copyright (C) 2020 TeamApps.org
 * ---
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
 * =========================LICENSE_END==================================
 */
package org.teamapps.udb.app;

import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.schema.SchemaInfoProvider;
import org.teamapps.ux.component.Component;
import org.teamapps.webcontroller.WebController;

import java.io.File;
import java.util.function.Supplier;

public class Server {

	private final File dbDir;
	private final File path;
	private final SchemaInfoProvider schema;
	private final WebServer webServer;
	private UniversalDB database;

	public Server(File path, SchemaInfoProvider schema) {
		dbDir = new File(path, "database");
		this.path = path;
		this.schema = schema;
		dbDir.mkdir();
		webServer = new WebServer();
	}

	public WebController getWebController() {
		return webServer.getWebController();
	}

	public void startServer(Supplier<Component> applicationSupplier) throws Exception {
		startServer(8080, applicationSupplier);
	}

	public void startServer(int port, Supplier<Component> applicationSupplier) throws Exception {
		database = UniversalDB.createStandalone(new File(path, "database"), schema);
		webServer.setPort(port);
		webServer.startServer(applicationSupplier);
	}

	public UniversalDB getDatabase() {
		return database;
	}
}
