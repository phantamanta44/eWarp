package io.github.phantamanta44.ewarp;

import io.github.phantamanta44.ewarp.WarpDB.Warp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
		ew.db.dataSet.forEach((name, warp) -> {
			if (warp.owner.equals(player))
				toBeRm.add(name);
		});
		for (String name : toBeRm) {
			ew.db.dataSet.remove(name);
		}
		ew.db.writeOut();
	}
	
	public static List<Warp> listPaginatedWarps(int page, boolean priv) {
		List<Warp> rtnValues = new ArrayList<>();
		Warp[] entrySet = ew.db.dataSet.values().toArray(new Warp[0]);
		Arrays.sort(entrySet, (comp1, comp2) -> {
			return (int)(comp1.creationTime.getTime() - comp2.creationTime.getTime());
		});
		List<Warp> entryList = new LinkedList<Warp>(Arrays.asList(entrySet));
		if (!priv) {
			entryList.removeIf(w -> {
				return w.priv;
			});
		}
		rtnValues = new LinkedList<>(entryList);
		rtnValues.removeIf(w -> {
			return (entryList.indexOf(w) < page * 10) || (entryList.indexOf(w) >= page * 10 + 10);
		});
		return rtnValues;
	}
	
	public static List<Warp> listPaginatedWarps(int page, boolean priv, UUID p) {
		List<Warp> rtnValues = listPaginatedWarps(page, priv);
		rtnValues.removeIf(w-> {
			return !w.owner.equals(p);
		});
		return rtnValues;
	}
	
	public static void teleportEffect(Location loc) {
		loc.getWorld().playEffect(loc, Effect.SMOKE, BlockFace.UP);
		loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.LAVA);
		for (float i = 0.5F; i <= 2; i += 0.25F) {
			loc.getWorld().playSound(loc, Sound.ENDERMAN_TELEPORT, 1.0F, i);
		}
	}
}
