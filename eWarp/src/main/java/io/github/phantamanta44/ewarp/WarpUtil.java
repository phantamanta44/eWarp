package io.github.phantamanta44.ewarp;

import io.github.phantamanta44.ewarp.WarpDB.Warp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;

public class WarpUtil {
	
	private static eWarp ew;
	
	public static void passPlugin(eWarp plugin) {
		ew = plugin;
		PermUtil.passPlugin(plugin);
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
		Set<String> toBeRm = new HashSet<>();
		for (Entry<String, Warp> e : ew.db.dataSet.entrySet()) {
			if (e.getValue().owner.equals(player))
				toBeRm.add(e.getKey());
		}
		for (String name : toBeRm) {
			ew.db.dataSet.remove(name);
		}
		ew.db.writeOut();
	}
	
	public static List<Warp> listPaginatedWarps(int page, boolean priv) {
		List<Warp> rtnValues = new ArrayList<>();
		Warp[] entrySet = ew.db.dataSet.values().toArray(new Warp[0]);
		Arrays.sort(entrySet, new Comparator<Warp>() {
			public int compare(Warp comp1, Warp comp2) {
				return (int)(comp1.creationTime.getTime() - comp2.creationTime.getTime());
			}
		});
		List<Warp> entryList = new LinkedList<Warp>(Arrays.asList(entrySet));
		if (!priv) {
			Iterator<Warp> iter = entryList.iterator();
			while (iter.hasNext()) {
				Warp w = iter.next();
				if (w.priv)
					iter.remove();
			}
		}
		rtnValues = new LinkedList<>(entryList);
		Iterator<Warp> iter = rtnValues.iterator();
		while (iter.hasNext()) {
			Warp w = iter.next();
			if ((entryList.indexOf(w) < page * 10) || (entryList.indexOf(w) >= page * 10 + 10))
				iter.remove();
		}
		return rtnValues;
	}
	
	public static List<Warp> listPaginatedWarps(int page, boolean priv, UUID p) {
		List<Warp> rtnValues = listPaginatedWarps(page, priv);
		Iterator<Warp> iter = rtnValues.iterator();
		while (iter.hasNext()) {
			Warp w = iter.next();
			if (!w.owner.equals(p))
				iter.remove();
		}
		return rtnValues;
	}
	
	public static void teleportEffect(Location loc) {
		loc.getWorld().playEffect(loc, Effect.SMOKE, BlockFace.UP);
		loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.LAVA);
		loc.getWorld().playSound(loc, Sound.ENDERMAN_TELEPORT, 1.0F, 0.5F);
		loc.getWorld().playSound(loc, Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
		loc.getWorld().playSound(loc, Sound.IRONGOLEM_HIT, 1.0F, 1.73F);
	}
}
