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
package org.teamapps.udb.explorer;

import org.teamapps.udb.app.WebServer;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.schema.SchemaInfoProvider;

import java.io.File;

public class DatabaseExplorer {

	private final File path;

	public DatabaseExplorer(File path, SchemaInfoProvider schema) throws Exception {
		this.path = path;
		UniversalDB database = UniversalDB.createStandalone(new File(path, "database"), schema);
		WebServer webServer = new WebServer();
		webServer.startServer(() -> new DatabaseExplorerApp(database).getApplication().getUi());
	}
}
