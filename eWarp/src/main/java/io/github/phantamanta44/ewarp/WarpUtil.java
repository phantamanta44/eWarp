package io.github.phantamanta44.ewarp;

import io.github.phantamanta44.ewarp.WarpDB.Warp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

public class WarpUtil {
	
	private static eWarp ew;
	
	public static void passPlugin(eWarp plugin) {
		ew = plugin;
	}
	
	public static void mkWarp(String name, UUID owner, Location loc, boolean prv) {
		if (!ew.db.dataSet.containsKey(name)) {
			Warp w = new Warp(name, loc, owner, prv);
			ew.db.dataSet.put(name, w);
			ew.db.writeOut();
		}
	}
	
	public static void rmWarp(String name) {
		if (ew.db.dataSet.containsKey(name)) {
			ew.db.dataSet.remove(name);
			ew.db.writeOut();
		}
	}
	public static void rmDashRf(UUID player) {
		ew.db.dataSet.forEach((String name, Warp warp) -> {
			if (warp.owner.equals(player))
				ew.db.dataSet.remove(name);
			});
		ew.db.writeOut();
	}
	
	public static List<Warp> listPaginatedWarps(int page, boolean priv) {
		List<Warp> rtnValues = new ArrayList<>();
		Warp[] entrySet = (Warp[])ew.db.dataSet.values().toArray();
		Arrays.sort(entrySet, (Warp comp1, Warp comp2) -> {
			return (int)(comp1.creationTime.getTime() - comp2.creationTime.getTime());
			});
		List<Warp> entryList = Arrays.asList(entrySet);
		if (!priv) {
			entryList.removeIf((Warp w) -> {
				return w.priv;
				});
		}
		rtnValues = entryList;
		rtnValues.removeIf((Warp w) -> {
			return (entryList.indexOf(w) < page * 10) || (entryList.indexOf(w) > page * 10 + 10);
			});
		return rtnValues;
	}
	
	public static List<Warp> listPaginatedWarps(int page, boolean priv, UUID p) {
		List<Warp> rtnValues = listPaginatedWarps(page, priv);
		rtnValues.removeIf((Warp w) -> {
			return !w.owner.equals(p);
			});
		return rtnValues;
	}
}
