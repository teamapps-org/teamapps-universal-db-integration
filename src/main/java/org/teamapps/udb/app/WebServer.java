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

import com.google.common.io.Files;
import org.teamapps.server.jetty.embedded.TeamAppsJettyEmbeddedServer;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.login.LoginWindow;
import org.teamapps.webcontroller.SimpleWebController;

import java.io.File;
import java.util.function.Supplier;

public class WebServer {

	private int port = 8080;
	private File webAppPath;
	private AccessController accessController;
	private Supplier<Component> componentSupplier;
	private final SimpleWebController webController;

	public WebServer() {
		webAppPath = Files.createTempDir();
		webController = new SimpleWebController(c -> componentSupplier.get());
		webController.setShowBackgroundImage(true);
	}

	public SimpleWebController getWebController() {
		return webController;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAccessController(AccessController accessController) {
		this.accessController = accessController;
	}

	public void setWebAppPath(File webAppPath) {
		this.webAppPath = webAppPath;
	}

	public void startServer(Supplier<Component> applicationSupplier) throws Exception {
		componentSupplier = wrapComponentSupplier(applicationSupplier);
		TeamAppsJettyEmbeddedServer server = new TeamAppsJettyEmbeddedServer(webController, webAppPath, port);
		server.start();
	}

	private Supplier<Component> wrapComponentSupplier(Supplier<Component> applicationSupplier) {
		if (accessController == null) {
			return applicationSupplier;
		} else {
			return () -> {
				VerticalLayout layout = new VerticalLayout();
				LoginWindow loginWindow = new LoginWindow();
				loginWindow.onLogin.addListener(() -> {
					if (accessController.grantAccess(loginWindow.getLogin(), loginWindow.getPassword())) {
						layout.removeAllComponents();
						layout.addComponentFillRemaining(applicationSupplier.get());
					} else {
						loginWindow.setError();
					}
				});
				layout.addComponentFillRemaining(loginWindow.getElegantPanel());
				return layout;
			};
		}
	}
}
