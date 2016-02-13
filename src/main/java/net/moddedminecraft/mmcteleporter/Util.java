package main.java.net.moddedminecraft.mmcteleporter;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Util {

	@SuppressWarnings("unused")
	private static MMCTeleporter plugin;

	public Util(MMCTeleporter instance) {
		plugin = instance;
	}

	public static void broadcastMessage(String message) {
		Bukkit.getServer().broadcastMessage(processColours(message));
	}

	public static void sendMessage(CommandSender sender, String message) {
		if (sender instanceof Player) {
			sender.sendMessage(processColours(message));
		} else {
			sender.sendMessage(stripColours(message));
		}
	}

	public static int roundUP(double d) {
		double dAbs = Math.abs(d);
		int i = (int) dAbs;
		double result = dAbs - (double) i;
		if (result == 0.0) {
			return (int) d;
		} else {
			return (int) d < 0 ? -(i + 1) : i + 1;
		}
	}

	public static String processColours(String str) {
		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
	}

	public static String stripColours(String str) {
		return str.replaceAll("(&([a-f0-9]))", "");
	}

	public static String reason(String[] s, int start, int end) {
		String[] args = (String[]) Arrays.copyOfRange(s, start, end);
		return StringUtils.join(args, " ");
	}

}
