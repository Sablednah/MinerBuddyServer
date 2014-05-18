/**
 * @author	sable <darren.douglas@gmail.com>
 * @version	3.2
 * 
 */
package me.sablednah.MinerBuddyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class MinerBuddyServer extends JavaPlugin {

	private Logger								log;

	private String								bukkitIP;
	private int									bukkitPort;

	private String								buddyIP;
	private int									buddyPort;

	private Boolean								debugMode;

	public BukkitTask							serverTaskID;
	public Boolean								stopServer	= false;

	private ServerSocket						serversocket;
	private Socket								connectionsocket;

	private Commands							processCommands;

	public static Map<String, CommandArchtype>	commandList	= new HashMap<String, CommandArchtype>();

	@Override
	public void onDisable() {

		log(Level.INFO, "Killing the HTTP server. Softly.");

		try {
			stopServer = true;
			if (connectionsocket != null) {
				connectionsocket.close();
			}
			if (serversocket != null) {
				serversocket.close();
			}
		} catch (IOException e) {
		}

		this.getServer().getScheduler().cancelTasks(this);

		log(Level.INFO, "--- END OF LINE ---");
	}

	@Override
	public void onEnable() {

		log = this.getLogger();

		processCommands = new Commands(this);
		getCommand("MinerBuddyServer").setExecutor(processCommands);

		loadConfiguration();

		if (debugMode) {
			log(Level.INFO, "DebugMode Enabled.");
		}

		this.saveResource("favicon.ico",false);
		
		/**
		 * Start REST Sever!
		 */
		stopServer = false;
		serverTaskID = this.getServer().getScheduler().runTaskAsynchronously(this, new RESTserver(this));

		log(Level.INFO, "Online.");
	}

	/**
	 * Initialise config file
	 */
	public void loadConfiguration() {
		this.getConfig().options().copyDefaults(true);

		String headertext;
		headertext = "Default Config file\r\n\r\n";
		headertext += "debugMode: [true|false] Enable extra debug info in logs.\r\n";
		headertext += "\r\n";

		this.getConfig().options().header(headertext);
		this.getConfig().options().copyHeader(true);

		this.debugMode = this.getConfig().getBoolean("debugMode");

		this.saveConfig();

		this.bukkitIP = this.getServer().getIp();
		this.bukkitPort = this.getServer().getPort();
		this.buddyIP = this.bukkitIP;
		this.buddyPort = 25569;

		if (this.bukkitIP.equals("")) {
			this.bukkitIP = "ANY";
		}

		if (this.buddyIP.equals("")) {
			this.buddyIP = this.bukkitIP;
		}

		ConfigurationSection cmdlist = this.getConfig().getConfigurationSection("commands");
		if (cmdlist != null) {
			for (String key : cmdlist.getKeys(false)) {
				ConfigurationSection commandInfo = cmdlist.getConfigurationSection(key);

				String n = key;
				int a = commandInfo.getInt("args");
				String c = commandInfo.getString("console");
				String p = commandInfo.getString("password");

				CommandArchtype cmd = new CommandArchtype(n, a, c, p);

				commandList.put(n, cmd);
				this.log.info("Command " + n + " added.");
			}
		}
	}

	public void log(Level level, String msg) {
		log.log(level, msg);
	}

	public void log(Level level, String msg, Throwable thrown) {
		log.log(level, msg, thrown);
	}

	/**
	 * @return the bukkitIP
	 */
	public String getBukkitIP() {
		return bukkitIP;
	}

	/**
	 * @param bukkitIP
	 *            the bukkitIP to set
	 */
	public void setBukkitIP(String bukkitIP) {
		this.bukkitIP = bukkitIP;
	}

	/**
	 * @return the bukkitPort
	 */
	public int getBukkitPort() {
		return bukkitPort;
	}

	/**
	 * @param bukkitPort
	 *            the bukkitPort to set
	 */
	public void setBukkitPort(int bukkitPort) {
		this.bukkitPort = bukkitPort;
	}

	/**
	 * @return the buddyIP
	 */
	public String getBuddyIP() {
		return buddyIP;
	}

	/**
	 * @param buddyIP
	 *            the buddyIP to set
	 */
	public void setBuddyIP(String buddyIP) {
		this.buddyIP = buddyIP;
	}

	/**
	 * @return the buddyPort
	 */
	public int getBuddyPort() {
		return buddyPort;
	}

	/**
	 * @param buddyPort
	 *            the buddyPort to set
	 */
	public void setBuddyPort(int buddyPort) {
		this.buddyPort = buddyPort;
	}

	/**
	 * @return the debugMode
	 */
	public Boolean getDebugMode() {
		return debugMode;
	}

	/**
	 * @param debugMode
	 *            the debugMode to set
	 */
	public void setDebugMode(Boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * @return the serversocket
	 */
	public ServerSocket getServersocket() {
		return serversocket;
	}

	/**
	 * @param serversocket
	 *            the serversocket to set
	 */
	public void setServersocket(ServerSocket serversocket) {
		this.serversocket = serversocket;
	}

	/**
	 * @return the connectionsocket
	 */
	public Socket getConnectionsocket() {
		return connectionsocket;
	}

	/**
	 * @param connectionsocket
	 *            the connectionsocket to set
	 */
	public void setConnectionsocket(Socket connectionsocket) {
		this.connectionsocket = connectionsocket;
	}

}
