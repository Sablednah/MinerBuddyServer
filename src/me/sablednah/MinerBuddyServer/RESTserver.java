package me.sablednah.MinerBuddyServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RESTserver extends Thread {

	private MinerBuddyServer	instance;

	public RESTserver(MinerBuddyServer i) {
		this.instance = i;
	}

	public void run() {
		InetAddress myIP;
		try {
			myIP = InetAddress.getByName(Bukkit.getServer().getIp());
		} catch (UnknownHostException e) {
			instance.log(Level.SEVERE, "Fatal Error.  Is " + Bukkit.getServer().getIp() + " correct?:" + e.getMessage());
			e.printStackTrace();
			return;
		}

		instance.log(Level.INFO, "Starting HTTP REST server on " + myIP.getHostAddress() + ".");

		try { // make a ServerSocket and bind it to given port,
			instance.log(Level.INFO, "Binding port " + instance.getBuddyPort() + "...");
			instance.setServersocket(new ServerSocket(instance.getBuddyPort(), 50, myIP));
		} catch (Exception e) { // catch any errors and print errors
			instance.log(Level.SEVERE, "Fatal Error.  Is " + instance.getBuddyPort() + " free?:" + e.getMessage());
			return;
		}

		instance.log(Level.INFO, "OK!");

		while (!instance.stopServer) {
			instance.log(Level.INFO, "Ready, Waiting for next request...");
			try {
				instance.setConnectionsocket(instance.getServersocket().accept());
				InetAddress client = instance.getConnectionsocket().getInetAddress();
				instance.log(Level.INFO, client.getHostName() + " connected to server.");
				BufferedReader input = new BufferedReader(new InputStreamReader(instance.getConnectionsocket().getInputStream()));
				DataOutputStream output = new DataOutputStream(instance.getConnectionsocket().getOutputStream());
				//http_handler(input, output);
				instance.getServer().getScheduler().runTask(instance, new Http_handler(input,output));

			} catch (Exception e) { // catch any errors, and print them
				instance.log(Level.SEVERE, "Error:" + e.getMessage());
			}

		}
	}

	public class Http_handler implements Runnable {
		public BufferedReader in;
		public DataOutputStream out;
		public Http_handler(BufferedReader input, DataOutputStream output){
			in=input;
			out=output;
		}
		public void run() {
			http_handler(in,out);
		}
	}
	
	
	private void http_handler(BufferedReader input, DataOutputStream output) {
		int method = 0; // 1 get, 2 head, 0 not supported
		String path = new String();

		try {
			String tmp = input.readLine(); // read from the stream
			if (tmp == null) {
				instance.log(Level.FINE, "Null request");
				output.writeBytes(construct_http_header(httpCode.CODE_501, null));
				output.close();
				return;
			}

			String tmp2 = new String(tmp);
			instance.log(Level.INFO, "Processing REST request: " + tmp2);

			tmp.toUpperCase();
			if (tmp.startsWith("GET ")) {
				method = 1;
			} else if (tmp.startsWith("HEAD ")) {
				method = 2;
			} else if (tmp.startsWith("POST ")) {
				method = 3;
			}

			if (method == 0) { // not supported
				output.writeBytes(construct_http_header(httpCode.CODE_501, null));
				output.close();
				return;
			}

			// Extract the path!
			String[] pathBits = tmp2.split(" ");

			if (pathBits[1].startsWith("/")) {
				path = pathBits[1].substring(1);
			} else {
				path = pathBits[1];
			}

			// instance.log(Level.INFO,"Client requested:" + path); //new File(path).getAbsolutePath());

			if (path.startsWith("favicon.ico")) { // a touch of prettyness for web clients :)

				String filename = path.split("\\?")[0];
				String headerText = construct_http_header(httpCode.CODE_200, contentType.ICON);
				// instance.log(Level.INFO,"output>:" + headerText);
				output.writeBytes(headerText);
				output.flush();
				sendFile(filename, output);
				output.close();
				return;
			}

			Actions action = Actions.toAction(path.split("/")[0]);
			instance.log(Level.FINER, "path[0]>:" + path.split("/")[0]);

			String outputText = processAction(action, path);

			instance.log(Level.FINER, "output>:" + outputText);
			output.writeBytes(outputText);
			output.flush();
			output.close();
			return;

		} catch (Exception e) { // catch any exception
			instance.log(Level.SEVERE, "Error " + e.getMessage());
			if (instance.getDebugMode()) {
				e.printStackTrace();
			}
		}
		return;
	}

	private String construct_http_header(httpCode rc, contentType ct) {

		String s = "HTTP/1.1 " + rc.getCode() + "\r\n";
		s = s + "Server: MinerBuddyServer v0.1\r\n";
		s = s + "Access-Control-Allow-Origin: *\r\n";
		s = s + "Access-Control-Allow-Methods: POST, GET, HEAD *\r\n";

		if (ct != null) {
			s = s + ct.getType() + "\r\n";
		}

		s = s + "\r\n"; // this marks the end of the httpheader
		return s;
	}

	private Boolean sendFile(String filename, DataOutputStream output) throws Exception {
		instance.log(Level.INFO, "Sending File: " + instance.getDataFolder() + File.separator + filename);
		FileInputStream requestedfile = new FileInputStream(instance.getDataFolder() + File.separator + filename);
		while (true) {
			int b = requestedfile.read();
			// instance.log(Level.INFO,"b:"+b);
			if (b == -1) {
				break; // end of file
			}
			output.write(b);
			output.flush();
		}
		requestedfile.close();

		return false;
	}

	public String processAction(Actions a, String path) {
		String strOut = null;
		switch (a) {
			case PLAYERS:
				strOut = construct_http_header(httpCode.CODE_200, contentType.TEXT);
				Object[] plrs = null;
				String playersJSON;
				playersJSON = "";

				if (path.contains("/")) {
					String worldName = path.split("/")[1];
					instance.log(Level.INFO, "Player list requested for '" + worldName + "'.");
					if (instance.getServer().getWorld(worldName) != null) {
						plrs = instance.getServer().getWorld(worldName).getPlayers().toArray();
					}
				} else {
					instance.log(Level.INFO, "Player list requested.");
					plrs = instance.getServer().getOnlinePlayers();
				}
				if (plrs != null) {
					for (int i = 0; i < plrs.length; i++) {
						playersJSON = playersJSON + "\"" + ((Player) plrs[i]).getName() + "\"";
						if (i + 1 < plrs.length) {
							playersJSON = playersJSON + ",";
						}
					}
				}

				playersJSON = "{\"Players\": [" + playersJSON + "] }";

				strOut = strOut + playersJSON;
				break;

			case PLAYERCOUNT:
				strOut = construct_http_header(httpCode.CODE_200, contentType.TEXT);
				instance.log(Level.INFO, "Player count requested.");
				strOut = strOut + "{\"PlayerCount\": ";
				String worldName = null;
				if (path.contains("/")) {
					String[] strings = path.split("/");
					if (strings.length > 0) {
						worldName = path.split("/")[1];
					}
				}
				if (worldName != null) {
					if (instance.getServer().getWorld(worldName) != null) {
						strOut = strOut + instance.getServer().getWorld(worldName).getPlayers().size() + " , ";
					} else {
						strOut = strOut + "0 , ";
					}
				} else {
					strOut = strOut + instance.getServer().getOnlinePlayers().length + " , ";
				}
				strOut = strOut + "\"MaxPlayers\": ";
				strOut = strOut + instance.getServer().getMaxPlayers() + " }";
				break;

			case PLAYER:
				instance.log(Level.INFO, "Player info requested.");
				if (path.contains("/")) {
					String Playername = path.split("/")[1];
					Player p = instance.getServer().getPlayer(Playername);
					instance.log(Level.FINER, "looking for player found: " + p);
					if (p == null) {
						instance.log(Level.WARNING, "Player '" + Playername + "' not found.");
						strOut = construct_http_header(httpCode.CODE_404, contentType.TEXT);
						strOut = strOut + Playername + " does not refer to an available player resource.";
					} else {
						strOut = construct_http_header(httpCode.CODE_200, contentType.TEXT);
						strOut = strOut + "{";
						strOut = strOut + "\"DisplayName\": \"" + p.getDisplayName() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"EntityId\": \"" + p.getEntityId() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"Op\": " + p.isOp();
						strOut = strOut + ",";
						strOut = strOut + "\"GameMode\": \"" + p.getGameMode() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"Blocking\": " + p.isBlocking();
						strOut = strOut + ",";
						strOut = strOut + "\"Sneaking\": " + p.isSneaking();
						strOut = strOut + ",";
						strOut = strOut + "\"Sprinting\": " + p.isSprinting();
						strOut = strOut + ",";
						strOut = strOut + "\"Sleeping\": " + p.isSleeping();
						strOut = strOut + ",";
						strOut = strOut + "\"SleepingIgnored\": " + p.isSleepingIgnored();
						strOut = strOut + ",";
						strOut = strOut + "\"Dead\": " + p.isDead();
						strOut = strOut + ",";
						strOut = strOut + "\"Health\": \"" + p.getHealth() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"MaxHealth\": \"" + p.getMaxHealth() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"Exhaustion\": \"" + p.getExhaustion() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"Saturation\": \"" + p.getSaturation() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"FoodLevel\": \"" + p.getFoodLevel() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"RemainingAir\": \"" + p.getRemainingAir() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"MaximumAir\": \"" + p.getMaximumAir() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"Holding\": \"" + p.getItemInHand().getType().toString() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"PlayerTime\": \"" + p.getPlayerTime() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"Level\": \"" + p.getLevel() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"Exp\": \"" + p.getExp() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"TotalExperience\": \"" + p.getTotalExperience() + "\"";
						strOut = strOut + ",";
						strOut = strOut + "\"AllowFlight\": " + p.getAllowFlight();
						strOut = strOut + ",";
						strOut = strOut + "\"Flying\": " + p.isFlying();
						strOut = strOut + ",";
						strOut = strOut + "\"Location\": " + Utils.JSONLocation(p.getLocation().getChunk().getChunkSnapshot(),p.getLocation());
						strOut = strOut + ",";
						strOut = strOut + "\"Vector\": " + Utils.JSONVelocity(p.getVelocity());
						strOut = strOut + ",";
						strOut = strOut + "\"World\": " + Utils.JSONPlayerWorldInfo(p.getWorld());
						if (p.getCompassTarget() != null) {
							strOut = strOut + ",";
							strOut = strOut + "\"CompassTarget\": " + Utils.JSONLocation(p.getCompassTarget().getChunk().getChunkSnapshot(),p.getCompassTarget());
							strOut = strOut + ",";
							if (p.getLocation().getWorld() == p.getCompassTarget().getWorld()) { // same world
								strOut = strOut + "\"CompassDistance\": " + p.getLocation().distance(p.getCompassTarget());
							} else {
								strOut = strOut + "\"CompassDistance\": " + null;
							}
						}
						if (p.getBedSpawnLocation() != null) {
							strOut = strOut + ",";
							strOut = strOut + "\"BedSpawnLocation\": " + Utils.JSONLocation(p.getBedSpawnLocation().getChunk().getChunkSnapshot(),p.getBedSpawnLocation());
							strOut = strOut + ",";
							if (p.getLocation().getWorld() == p.getBedSpawnLocation().getWorld()) { // same world
								strOut = strOut + "\"BedDistance\": " + p.getLocation().distance(p.getBedSpawnLocation());
							} else {
								strOut = strOut + "\"BedDistance\": " + null;
							}
						}
						strOut = strOut + "}";

					}
				} else {
					// no player param in request - reject it!
					instance.log(Level.WARNING, "Action '" + path + "' does not contain player name.");
					strOut = construct_http_header(httpCode.CODE_404, contentType.TEXT);
					strOut = strOut + "Request does not contain a valid player name.";
				}
				break;

			case WORLDS:
				strOut = construct_http_header(httpCode.CODE_200, contentType.TEXT);
				instance.log(Level.INFO, "World list requested.");

				String WorldsJSON;
				Iterator<World> wlds = instance.getServer().getWorlds().iterator();
				WorldsJSON = "";
				while (wlds.hasNext()) {
					World w = wlds.next();
					WorldsJSON = WorldsJSON + "\"" + w.getName() + "\",";
				}
				WorldsJSON = WorldsJSON.substring(0, WorldsJSON.length() - 2);
				WorldsJSON = "{\"Worlds\": [" + WorldsJSON + "] }";

				strOut = strOut + WorldsJSON;
				break;

			case WORLDCOUNT:
				strOut = construct_http_header(httpCode.CODE_200, contentType.TEXT);
				instance.log(Level.INFO, "World count requested.");
				strOut = strOut + "{\"WorldCount\": ";
				strOut = strOut + instance.getServer().getWorlds().size() + " }";
				break;

			case COMMAND:
				instance.log(Level.INFO, "Command requested.");
				if (path.contains("/")) {
					String[] args = path.split("/");
					String cmdName = args[1];
					if (MinerBuddyServer.commandList.containsKey(cmdName)) {
						CommandArchtype requestedCommand = MinerBuddyServer.commandList.get(cmdName);
						String finalCommand = requestedCommand.getConsole();
						int reqArgs = requestedCommand.getArguments();
						instance.log(Level.FINER, "Command:" + finalCommand);
						instance.log(Level.FINER, "args.length:" + args.length);
						instance.log(Level.FINER, "reqArgs:" + reqArgs);
						if (args.length > reqArgs + 1) { // was +1 before secure check
							String secure = args[args.length - 1];
							String hash = Utils.hash(requestedCommand.getPwd() + cmdName);

							instance.log(Level.FINEST, "tohash:" + requestedCommand.getPwd() + cmdName);
							instance.log(Level.FINEST, "secure:" + secure);
							instance.log(Level.FINEST, "hash:" + hash);

							if (hash.equals(secure)) {
								for (int j = 1; j <= reqArgs; j++) {
									String repstring = "%" + j;
									instance.log(Level.FINER, "repstring:" + repstring);
									instance.log(Level.FINER, "j:" + j);
									instance.log(Level.FINER, "args[j+1]:" + args[j + 1]);
									try {
										finalCommand = finalCommand.replaceAll((repstring), URLDecoder.decode(args[j + 1], "UTF-8"));
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
									}
								}
								instance.log(Level.INFO, "Command:" + finalCommand);
								boolean bob = instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), finalCommand);
								if (bob) {
									strOut = construct_http_header(httpCode.CODE_200, contentType.TEXT);
									instance.log(Level.INFO, "Command Executed.");
									strOut = strOut + "{\"commandResult\": ";
									strOut = strOut + bob + " }";
								} else {
									instance.log(Level.WARNING, "Action '" + path + "' failed.");
									strOut = construct_http_header(httpCode.CODE_200, contentType.TEXT);
									strOut = strOut + "{\"commandResult\": ";
									strOut = strOut + bob + " }";
								}
							} else {
								// no password param in request - reject it!
								instance.log(Level.WARNING, "Action '" + path + "' does not contain valid password.");
								strOut = construct_http_header(httpCode.CODE_404, contentType.TEXT);
								strOut = strOut + "Request does not contain valid password.";
							}
						} else {
							// no player param in request - reject it!
							instance.log(Level.WARNING, "Action '" + path + "' does not contain valid arguments.");
							strOut = construct_http_header(httpCode.CODE_404, contentType.TEXT);
							strOut = strOut + "Request does not contain valid args.";
						}
					} else {
						// no player param in request - reject it!
						instance.log(Level.WARNING, "Action '" + path + "' does not contain valid command.");
						strOut = construct_http_header(httpCode.CODE_404, contentType.TEXT);
						strOut = strOut + "Request does not contain valid command.";
					}
				} else {
					// no player param in request - reject it!
					instance.log(Level.WARNING, "Action '" + path + "' does not contain arguments.");
					strOut = construct_http_header(httpCode.CODE_404, contentType.TEXT);
					strOut = strOut + "Request does not contain valid arguments.";
				}
				break;

			default: // prolly INVALID
				instance.log(Level.WARNING, "Action '" + path + "' not recognised.");
				strOut = construct_http_header(httpCode.CODE_404, contentType.TEXT);
				strOut = strOut + "Request " + path + " does not refer to an available resource.";
		}
		return strOut;
	}
}
