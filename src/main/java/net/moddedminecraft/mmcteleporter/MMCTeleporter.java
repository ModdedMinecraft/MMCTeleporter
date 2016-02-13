package main.java.net.moddedminecraft.mmcteleporter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

public class MMCTeleporter extends JavaPlugin implements Listener {
	public FileConfiguration config = null;
	public FileConfiguration UserD = null;
	public BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	public Player pjoin;
	public HashMap<String, String> uuids = new HashMap<String, String>();
	public double version = getConfig().getDouble("Version");

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		if (!(new File(getDataFolder(), "config.yml").exists())) {
			getLogger().info("Creating Config.yml file.");
			saveDefaultConfig();
			reloadConfig();
		} else if (version != Double.parseDouble(this.getDescription().getVersion())) {
			Double vers = getConfig().getDouble("Version");
			getConfig().set("Version", Double.parseDouble(this.getDescription().getVersion()));
			this.saveConfig();
		}

		if (!(new File(getDataFolder(), "Data").exists())) {
			getLogger().info("Creating Data Folder..");
			new File(getDataFolder(), "Data").mkdir();
		}
		reloadConfig();

		File[] folder = new File(getDataFolder(), "/Data/").listFiles();
		getLogger().info("Mapping UUID's..");
		for (File i : folder) {
			if (i.isFile()) {
				String name = i.getName().substring(0, i.getName().lastIndexOf("."));
				configurePlayer(name);
				if (UserD.getString("UUID") != null) {
					uuids.put(name, UserD.getString("UUID"));
				} else {
					getLogger().info("error: No UUID in the userfile: " + i.getName());
				}
				UserD = null;
			}
		}
		getLogger().info("Finished Mapping (" + uuids.size() + ")");
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabling MMCTeleporter");
	}

	@EventHandler
	public void joinEvent(PlayerJoinEvent event) {
		String player = event.getPlayer().getName();
		pjoin = getServer().getPlayer(event.getPlayer().getUniqueId());

		if (!(uuids.containsValue(pjoin.getUniqueId().toString()))) {
			try {
				createFile(player);
			} catch (IOException rr) {
				getLogger().info("Couldn't close the file");
			}
			configurePlayer(player);
			UserD.set("UUID", pjoin.getUniqueId().toString());
			savePlayer(player);
			uuids.put(pjoin.getName(), pjoin.getUniqueId().toString());
		} else {
			String key = getkey(pjoin.getPlayer().getUniqueId().toString());
			if (!(key.equals(pjoin.getName()))) {
				File pfile = new File(getDataFolder(), "/Data/" + key + ".yml");
				File nname = new File(pjoin.getName());
				pfile.renameTo(nname);
				uuids.remove(key);
				uuids.put(pjoin.getName(), pjoin.getUniqueId().toString());
			}
		}
		configurePlayer(player);
		if (UserD.getString("newPosition.world") != null) {
			Location loc = new Location(getServer().getWorld("world"), 8, 64, 8);
			loc.setWorld(Bukkit.getServer().getWorld(UserD.getString("newPosition.world")));
			loc.setX(UserD.getDouble("newPosition.x"));
			loc.setY(UserD.getDouble("newPosition.y"));
			loc.setZ(UserD.getDouble("newPosition.z"));
			loc.setYaw((float) UserD.getDouble("newPosition.yaw"));
			loc.setPitch((float) UserD.getDouble("newPosition.pitch"));
			pjoin.teleport(loc, TeleportCause.PLUGIN);

			scheduler.scheduleSyncDelayedTask(this,
					new Scheduler(this, 
							pjoin.getName(), 
							UserD.getString("newPosition.world"),
							UserD.getString("newPosition.setter"), 
							String.valueOf(UserD.getInt("newPosition.x")),
							String.valueOf(UserD.getInt("newPosition.y")),
							String.valueOf(UserD.getInt("newPosition.z"))),
					20 * 2);
			if (UserD.getString("newPosition.world") != null) {
				UserD.set("newPosition.world", null);
				UserD.set("newPosition.x", null);
				UserD.set("newPosition.y", null);
				UserD.set("newPosition.z", null);
				UserD.set("newPosition.yaw", null);
				UserD.set("newPosition.pitch", null);
				UserD.set("newPosition.setter", null);
				savePlayer(player);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String player = event.getPlayer().getName();
		Location loc = event.getPlayer().getLocation();
		letsSet(player, "lastPosition", loc);
		configurePlayer(player);
		if (UserD.getString("newPosition.world") != null) {
			UserD.set("newPosition.world", null);
			UserD.set("newPosition.x", null);
			UserD.set("newPosition.y", null);
			UserD.set("newPosition.z", null);
			UserD.set("newPosition.yaw", null);
			UserD.set("newPosition.pitch", null);
			UserD.set("newPosition.setter", null);
			savePlayer(player);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("mmct")) {
			if (!(sender instanceof Player)) {
				Util.sendMessage(sender, "&4You're not a player!");
			} else {
				if (args.length > 1) {
					Util.sendMessage(sender, "&4Too many Arguments!");
				} else {
					if (args.length < 1) {
						Util.sendMessage(sender, "&4Not Enough Arguments");
					} else {
						Player player = (Player) sender;
						if (!(sender.hasPermission("mmcteleporter.mmct")) && !(sender.isOp())) {
							Util.sendMessage(sender, "&4you don't have the permission!");
						} else {
							if (casein(args[0]) == null) {
								Util.sendMessage(sender, "&4Are you sure you typed the name correct? (" + args[0] + ")");
							} else {
								configurePlayer(casein(args[0]));
								Location loc = new Location(player.getWorld(), 8, 64, 8);
								loc.setWorld(Bukkit.getServer().getWorld(UserD.getString("lastPosition.world")));
								loc.setX(UserD.getDouble("lastPosition.x"));
								loc.setY(UserD.getDouble("lastPosition.y"));
								loc.setZ(UserD.getDouble("lastPosition.z"));
								loc.setYaw((float) UserD.getDouble("lastPosition.yaw"));
								loc.setPitch((float) UserD.getDouble("lastPosition.pitch"));
								UserD = null;
								player.teleport(loc, TeleportCause.COMMAND);
								Util.sendMessage(sender, "&2Successfully teleported to " + args[0] + "'s logout location!");
								return true;
							}
						}
					}
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("mmcthere")) { //TODO Add an check for teleport already set
			String caseins;
			if (!(sender instanceof Player)) {
				Util.sendMessage(sender, "&4You need to be a player to do this..");
			} else {
				if (!(sender.hasPermission("mmcteleporter.mmcthere")) && !(sender.isOp())) {
					Util.sendMessage(sender, "&4You don't have the right permission: &8mmcteleporter.mmcthere");
				} else {
					if (args.length < 1) {
						Util.sendMessage(sender, "&4I'm gonna need a player name..");
					} else {
						if (casein(args[0]) == null) {
							Util.sendMessage(sender, "&4Can't find the player file, Did " + args[0] + " ever login before?");
						} else {
							caseins = casein(args[0]);
							if ((getServer().getPlayer(caseins) != null)) {
								Util.sendMessage(sender, "&4Looks like " + caseins + " is still online, Try using the regular /tphere command.");
							} else {
								if ((sender.getName().equals(caseins))) {
									Util.sendMessage(sender, "&4You cannot set your own login position!");
								} else {
									Player player = (Player) sender;
									Location loc = player.getLocation();
									letsSet(caseins, "newPosition", loc);
									configurePlayer(caseins);
									UserD.set("newPosition.setter", player.getName());
									savePlayer(caseins);
									Util.sendMessage(sender, "&2Successfully set " + caseins + "'s login location!");
									if (!(args.length > 1)) {
										return true;
									} else {
										configurePlayer(caseins);
										savePlayer(caseins);
										return true;
									}
								}
							}
						}
					}
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("mmctback")) {
			if (!(sender instanceof Player)) {
				Util.sendMessage(sender, "&4You are not a player!");
			} else {
				if (!(sender.hasPermission("mmcteleporter.mmctback")) && !(sender.isOp())) {
					Util.sendMessage(sender, "&4Insufficient Permissions!");
				} else {
					if (args.length > 0) {
						Util.sendMessage(sender, "&4Too many arguments!");
					} else {
						configurePlayer(sender.getName());
						if (UserD.getString("newPosition.world") == null) {
							Util.sendMessage(sender, "&4You can only use this once (before logging out)");
						} else {
							Player player = (Player) sender;
							Location loc = new Location(player.getWorld(), 8, 64, 8);
							loc.setWorld(getServer().getWorld(UserD.getString("lastPosition.world")));
							loc.setX(UserD.getDouble("lastPosition.x"));
							loc.setY(UserD.getDouble("lastPosition.y"));
							loc.setZ(UserD.getDouble("lastPosition.z"));
							loc.setYaw((float) UserD.getDouble("lastPosition.yaw"));
							loc.setPitch((float) UserD.getDouble("lastPosition.pitch"));
							player.teleport(loc, TeleportCause.COMMAND);
							Util.sendMessage(sender, "&2You Successfully teleported to your previous logout location!");
							UserD.set("newPosition.world", null);
							UserD.set("newPosition.x", null);
							UserD.set("newPosition.y", null);
							UserD.set("newPosition.z", null);
							UserD.set("newPosition.yaw", null);
							UserD.set("newPosition.pitch", null);
							UserD.set("newPosition.setter", null);
							savePlayer(player.getName());
						}
					}
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("mmctspawn")) {
			String caseins;
			if (args.length < 1)
				return false;
			if (casein(args[0]) == null) {
				Util.sendMessage(sender, "&4Can't find the player file, has " + args[0] + " login before?");
			} else {
				caseins = casein(args[0]);
				if ((getServer().getPlayer(caseins) != null)) {
					Util.sendMessage(sender, "&4" + caseins + " is still online, Try using the regular /tphere command.");
				} else {
					try {
						String changeOwner = "Console";
						if (sender instanceof Player) {
							changeOwner = sender.getName();
							if (!(sender.hasPermission("mmcteleporter.mmctspawn")) && !(sender.isOp()))
								return false;
						}
						Location loc = getServer().getWorlds().get(0).getSpawnLocation();
						letsSet(caseins, "newPosition", loc);
						configurePlayer(caseins);
						UserD.set("newPosition.setter", changeOwner);
						savePlayer(caseins);
						Util.sendMessage(sender, "&2Successfully set " + caseins + "'s login location!");
					} catch (Exception e) {
						sender.sendMessage("Unable to offline teleport to spawn - exception caught");
					}
				}
			}
		}
		return true;
	}

	public String getkey(String value) {
		for (Map.Entry<String, String> entry : uuids.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey().toString();
			}
		}
		return null;
	}

	public String casein(String arg) {
		int ir = 0;
		Map.Entry<String, String> target = null;

		for (Map.Entry<String, String> entry : uuids.entrySet()) {
			if (entry.getKey().equals(arg)) {
				return entry.getKey();
			} else if (entry.getKey().equalsIgnoreCase(arg)) {
				ir++;
				target = entry;
			}
		}
		if (ir == 1 && target != null) {
			return target.getKey();
		} else if (ir < 1) {
			return null;
		}
		return null;
	}

	public void configurePlayer(String player) {
		File playerFile = new File(getDataFolder(), "/Data/" + player + ".yml");
		FileConfiguration playerc = YamlConfiguration.loadConfiguration(playerFile);
		UserD = playerc;
	}

	public void savePlayer(String player) {
		File playerFile = new File(getDataFolder(), "/Data/" + player + ".yml");
		try {
			UserD.save(playerFile);
		} catch (IOException ex) {
			getLogger().info("Player file not found!");
		}
		UserD = null;
	}

	public void letsSet(String player, String list, Location loc) {
		configurePlayer(player);
		UserD.set(list + "." + "world", loc.getWorld().getName());
		UserD.set(list + "." + "x", loc.getX());
		UserD.set(list + "." + "y", loc.getY());
		UserD.set(list + "." + "z", loc.getZ());
		UserD.set(list + "." + "yaw", loc.getYaw());
		UserD.set(list + "." + "pitch", loc.getPitch());
		savePlayer(player);
	}

	public void createFile(String name) throws IOException {
		InputStream src = this.getResource("user.yml");
		OutputStream os = null;
		int readb;
		byte[] buffer = new byte[4096];

		try {
			getLogger().info("Creating new user file for " + name);
			os = new FileOutputStream(new File(getDataFolder(), "/Data/" + name + ".yml"));
			while ((readb = src.read(buffer)) > 0) {
				os.write(buffer, 0, readb);
			}
		} catch (IOException error) {
		} finally {
			src.close();
			os.close();
		}
	}
}
