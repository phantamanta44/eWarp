package io.github.phantamanta44.ewarp;

import io.github.phantamanta44.ewarp.PermUtil.WarpResult;
import io.github.phantamanta44.ewarp.WarpDB.Warp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class eWarp extends JavaPlugin {
	
	public static final String msgPrefix = ChatColor.AQUA + "EclipsePvP " + ChatColor.GRAY + "» " + ChatColor.RESET;
	public WarpDB db;
	
	@Override
	public void onEnable() {
		db = new WarpDB(this);
		WarpUtil.passPlugin(this);
	}
	
	@Override
	public void onDisable() {
		db.writeOut();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argArray) {
		
		if (cmd.getName().equals("warp")) {
			List<String> args = sanitizeArgs(argArray);
			Map<String, String> parsedArgs = parseArgs(Arrays.asList(argArray));
			if (args.size() == 0) {
				if (parsedArgs.containsKey("--help")) {
					sendPrefixedMessage(sender, "Usage: /warp <warpname>");
					sendPrefixedMessage(sender, "warpname: Name of the warp to travel to");
					return true;
				}
				sendPrefixedMessage(sender, "Usage: /warp [--help] [set|list|del|clear]");
			}
			else {
				if (args.get(0).equals("set")) {
					if (parsedArgs.containsKey("--help") || args.size() == 1) {
						sendPrefixedMessage(sender, "Usage: /warp set [-p] [-o <owner>] <warpname>");
						sendPrefixedMessage(sender, "Creates a warp.");
						sendPrefixedMessage(sender, "-p flag: Set warp as private");
						sendPrefixedMessage(sender, "-o flag: Designate warp owner");
						sendPrefixedMessage(sender, "warpname: Name of the warp");
						return true;
					}
					if (!db.dataSet.containsKey(args.get(1))) {
						UUID id = ((Player)sender).getUniqueId();
						if (parsedArgs.containsKey("-o")) {
							if (Bukkit.getServer().getOfflinePlayer(parsedArgs.get("-o")) != null)
								id = Bukkit.getServer().getOfflinePlayer(parsedArgs.get("-o")).getUniqueId();
							else {
								sendPrefixedMessage(sender, "Player not found!");			
								return true;
							}
						}
						boolean prv = parsedArgs.containsKey("-p");
						WarpResult r = PermUtil.canSet((Player)sender, prv, parsedArgs.containsKey("-o"));
						switch (r) {
						case CLEAR:
							WarpUtil.mkWarp(args.get(1), id, ((Player)sender).getLocation(), prv);
							sendPrefixedMessage(sender, "Warp set.");
							break;
						case NO_PERM:
							sendPrefixedMessage(sender, "No permission!");
							break;
						case LIMIT:
							sendPrefixedMessage(sender, "You are at your warp limit!");
							break;
						}
					}
					else {
						sendPrefixedMessage(sender, "Warp already exists!");
					}
				}
				else if (args.get(0).equals("list")) {
					if (parsedArgs.containsKey("--help")) {
						sendPrefixedMessage(sender, "Usage: /warp list [-p] [-o <owner>] [page]");
						sendPrefixedMessage(sender, "Lists warps.");
						sendPrefixedMessage(sender, "-p flag: List private warps");
						sendPrefixedMessage(sender, "-o flag: Designate warp owner");
						sendPrefixedMessage(sender, "page: Page number to view");
						return true;
					}
					
					int page = 1;
					try {
						if (args.size() >= 2) {
							page = Math.max(Integer.parseInt(args.get(1)), 1);
						}
					}
					catch (NumberFormatException ex) {
						sendPrefixedMessage(sender, "Page number must be a real integer!");
						return true;
					}
					
					if (!PermUtil.canGet((Player)sender, parsedArgs.containsKey("-p"), parsedArgs.containsKey("-o"))) {
						sendPrefixedMessage(sender, "No permission!");
						return true;
					}
					
					List<Warp> warps;
					if (parsedArgs.containsKey("-o")) {
						if (Bukkit.getServer().getOfflinePlayer(parsedArgs.get("-o")) != null)
								warps = WarpUtil.listPaginatedWarps(page - 1, parsedArgs.containsKey("-p"), Bukkit.getServer().getOfflinePlayer(parsedArgs.get("-o")).getUniqueId());
						else {
							sendPrefixedMessage(sender, "Player not found!");			
							return true;
						}
					}
					else {
						warps = WarpUtil.listPaginatedWarps(page - 1, parsedArgs.containsKey("-p"));
					}
					sendPrefixedMessage(sender, "Listing warps starting at page " + page + "...");
					for (Warp w : warps) {
						Location l = w.resolveLocation();
						sender.sendMessage(ChatColor.GREEN + w.name + " | by " + Bukkit.getOfflinePlayer(w.owner).getName() + " at " + w.getCreationTime() + String.format(" | @(%d,%d,%d)", l.getBlockX(), l.getBlockY(), l.getBlockZ()));
					}
				}
				else if (args.get(0).equals("del")) {
					if (parsedArgs.containsKey("--help") || args.size() == 1) {
						sendPrefixedMessage(sender, "Usage: /warp del <warpname>");
						sendPrefixedMessage(sender, "Deletes a warp.");
						sendPrefixedMessage(sender, "warpname: Name of the warp to remove");
						return true;
					}
					if (db.dataSet.containsKey(args.get(1))) {
						if (!PermUtil.canRm((Player)sender, args.get(1))) {
							sendPrefixedMessage(sender, "No permission!");
							return true;
						}
						db.dataSet.remove(args.get(1));
						sendPrefixedMessage(sender, "Warp removed.");
					}
					else {
						sendPrefixedMessage(sender, "Warp doesn't exist!");
					}
				}
				else if (args.get(0).equals("clear")) {
					if (parsedArgs.containsKey("--help")) {
						sendPrefixedMessage(sender, "Usage: /warp clear <--confirm> [-o <owner>]");
						sendPrefixedMessage(sender, "Clears a player's warps.");
						sendPrefixedMessage(sender, "--confirm flag: Confirm the deletion.");
						sendPrefixedMessage(sender, "-o flag: Designate warp owner");
						return true;
					}
					if (!parsedArgs.containsKey("--confirm")) {
						sendPrefixedMessage(sender, "--confirm flag required to clear warps!");
						return true;
					}
					
					if (!PermUtil.canClr((Player)sender, parsedArgs.containsKey("-o"))) {
						sendPrefixedMessage(sender, "No permission!");
						return true;
					}
					
					UUID id = ((Player)sender).getUniqueId();
					if (parsedArgs.containsKey("-o")) {
						if (Bukkit.getServer().getOfflinePlayer(parsedArgs.get("o")) != null)
							id = Bukkit.getServer().getOfflinePlayer(parsedArgs.get("o")).getUniqueId();
						else {
							sendPrefixedMessage(sender, "Player not found!");			
							return true;
						}
					}
					WarpUtil.rmDashRf(id);
					sendPrefixedMessage(sender, "Warps cleared.");
				}
				else {
					if (parsedArgs.containsKey("--help")) {
						sendPrefixedMessage(sender, "Usage: /warp <warpname>");
						sendPrefixedMessage(sender, "warpname: Name of the warp to travel to");
						return true;
					}
					if (db.dataSet.containsKey(args.get(0))) {
						Warp w = db.dataSet.get(args.get(0));
						if (!PermUtil.canGo((Player)sender, w)) {
							sendPrefixedMessage(sender, "No permission!");
							return true;
						}
						w.warpPlayer((Player)sender);
						sendPrefixedMessage(sender, "Warped to " + w.name + ".");
					}
					else {
						sendPrefixedMessage(sender, "Warp doesn't exist!");
					}
				}
			}
		}
		
		return true;
	}
	
	public static Map<String, String> parseArgs(List<String> args) {
		Map<String, String> rtnValues = new HashMap<>();
		for (String arg : args) {
			if (arg.matches("-+[A-Za-z0-9_]*")) {
				if (args.indexOf(arg) != args.size() - 1)
					rtnValues.put(arg, args.get(args.indexOf(arg) + 1));
				else
					rtnValues.put(arg, null);
			}
		}
		return rtnValues;
	}
	
	public static List<String> sanitizeArgs(String[] args) {
		List<String> argList = new LinkedList<String>(Arrays.asList(args));
		List<String> rtnValues = new LinkedList<String>(argList);
		for (String arg : argList) {
			if (arg.equals("-p") || arg.equals("--help") || arg.equals("--confirm")) {
				rtnValues.remove(arg);
			}
			else if (arg.matches("-+[A-Za-z0-9_]*")) {
				if (argList.indexOf(arg) != argList.size() - 1)
					rtnValues.remove(argList.get(argList.indexOf(arg) + 1));
				rtnValues.remove(arg);
			}
		}
		return rtnValues;
	}
	
	public static void sendPrefixedMessage(CommandSender target, String msg) {
		target.sendMessage(msgPrefix + msg);
	}

}
