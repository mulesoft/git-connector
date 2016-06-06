package org.mule.modules.git.config;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.mule.api.ConnectionException;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.TestConnectivity;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.components.ConnectionManagement;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Optional;

@ConnectionManagement(friendlyName = "Git Configuration")
public class ConnectorConfig {

	private String username;
	private String password;

	/**
	 * Default repository base directory
	 */
	@Configurable
	@Optional
	private String directory;

	/**
	 * Connect
	 *
	 * @param username
	 *            A username
	 * @param password
	 *            A password
	 * @throws ConnectionException
	 */
	@Connect
	@TestConnectivity
	public void connect(@ConnectionKey String username, @Password String password) throws ConnectionException {
		this.username = username;
		this.password = password;
		
	}

	/**
	 * Disconnect
	 */
	@Disconnect
	public void disconnect() {
		this.username = null;
		this.password = null;
	}

	/**
	 * Are we connected
	 */
	@ValidateConnection
	public boolean isConnected() {
		// TODO: Change it to reflect that we are connected.
		return username != null  && password != null;
	}

	/**
	 * Are we connected
	 */
	@ConnectionIdentifier
	public String connectionId() {
		return "001";
	}

	public CredentialsProvider getCredentialsProvider() {
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
		return credentialsProvider;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}