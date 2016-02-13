package main.java.net.moddedminecraft.mmcteleporter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Scheduler implements Runnable {
	String world;
	String setter;
	Player pjoin;
	String play;
	String x;
	String y;
	String z;
	MMCTeleporter plugin;

	public Scheduler(MMCTeleporter pl, String player, String worldd, String setterr, String xx, String yy,
			String zz) {
		plugin = pl;
		pjoin = plugin.getServer().getPlayer(player);
		world = worldd;
		setter = setterr;
		x = xx;
		y = yy;
		z = zz;
		play = player;
	}

	@Override
	public void run() {
		pjoin.sendMessage(ChatColor.GRAY + "You were teleported!");
		pjoin.sendMessage(ChatColor.DARK_PURPLE + "-----------------------------------------------------");
		pjoin.sendMessage(ChatColor.DARK_GRAY + "Teleporter: " + setter);
		pjoin.sendMessage(ChatColor.DARK_GRAY + "Prev Pos: " + world + ", " + x + ", " + y + ", " + z);
		if ((pjoin.hasPermission("mmcteleporter.mmctback")) || (pjoin.isOp())) {
			pjoin.sendMessage(ChatColor.DARK_GRAY + "You can use /mmctback to go back to where you were!");
		}
	}
}