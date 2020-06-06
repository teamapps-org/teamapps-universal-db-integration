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
