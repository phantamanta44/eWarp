package io.github.phantamanta44.ewarp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

public class WarpDB {
	
	private File dbFile;
	private FileConfiguration db;
	private static Map<String, Warp> dataSet = new HashMap<>();
	
	public WarpDB(JavaPlugin source) {
		dbFile = new File(source.getDataFolder(), "warps.yml");
		db = YamlConfiguration.loadConfiguration(dbFile);
		if (!dbFile.exists())
			this.writeOut();
		this.populateDb();
	}
	
	public void writeOut() {
		db = YamlConfiguration.loadConfiguration(new File("twidashiscoollessthanthree")); // Roundabout way of getting a blank config
		Gson gson = new Gson();
		for (Entry<String, Warp> entry : dataSet.entrySet()) {
			String serialized = gson.toJson(entry.getValue(), Warp.class);
			db.set(entry.getKey(), serialized);
		}
		try {
			db.save(dbFile);
		}
		catch (IOException ex) {
			System.out.println("[eWarp] Warp database writing failed!");
			ex.printStackTrace();
		}
	}
	
	public void populateDb() {
		dataSet.clear();
		Gson gson = new Gson();
		for (Object sec : db.getValues(false).values()) {
			Warp entry = gson.fromJson((String)sec, Warp.class);
			dataSet.put(entry.name, entry);
		}
	}
	
	public void reload() {
		db = YamlConfiguration.loadConfiguration(dbFile);
		this.populateDb();
	}
	
	public static class Warp {
		private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		public final String name;
		public final UUID owner;
		public final Date creationTime;
		public Location loc;
		
		public Warp(String n, Location l, UUID o) {
			name = n;
			loc = l;
			owner = o;
			creationTime = new Date();
		}
		
		public String getCreationTime() {
			return df.format(creationTime);
		}
		
		public void warpPlayer(Player p) {
			p.teleport(loc);
		}
	}
}
