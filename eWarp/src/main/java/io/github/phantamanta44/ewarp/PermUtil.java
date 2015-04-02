package io.github.phantamanta44.ewarp;

import io.github.phantamanta44.ewarp.WarpDB.Warp;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PermUtil {
	
	private static eWarp ew;
	
	public static void passPlugin(eWarp plugin) {
		ew = plugin;
	}
	
	public static boolean can(Permissible p, Perm t) {
		return p.hasPermission(t.p);
	}
	
	public static WarpResult canSet(Player p, boolean prv, boolean other) {
		if (!can(p, Perm.SET)) return WarpResult.NO_PERM;
		if (prv && !can(p, Perm.SET_PR)) return WarpResult.NO_PERM;
		if (other && !can(p, Perm.SET_TG)) return WarpResult.NO_PERM;
		if (!can(p, Perm.LIMIT_NONE)) {
			int count = 0;
			for (Warp w : ew.db.dataSet.values()) {
				if (w.owner.equals(p.getUniqueId()))
					count++;
			}
			if (count >= 10) {
				return WarpResult.LIMIT;
			}
		}
		return WarpResult.CLEAR;
	}
	
	public static boolean canGo(Player p, Warp w) {
		if (!can(p, Perm.GO)) return false;
		if (w.priv && !can(p, Perm.GO_PR)) {
			if (w.owner != p.getUniqueId())
				return false;
		}
		return true;
	}
	
	public static boolean canGet(Player p, boolean prv, boolean other) {
		if (!can(p, Perm.GET)) return false;
		if (prv && !can(p, Perm.GET_PR)) return false;
		if (other && !can(p, Perm.GET_TG)) return false;
		return true;
	}
	
	public static boolean canRm(Player p, String key) {
		if (!can(p, Perm.RM)) return false;
		if (ew.db.dataSet.containsKey(key)) {
			Warp w = ew.db.dataSet.get(key);
			if (w.owner != p.getUniqueId() && !can(p, Perm.RM_TG))
				return false;
		}
		return true;
	}
	
	public static boolean canClr(Player p, boolean other) {
		if (!can(p, Perm.CLR)) return false;
		if (other && !can(p, Perm.CLR_TG)) return false;
		return true;
	}
	
	public static enum Perm {
		SET("ewarp.set"), SET_PR("ewarp.set.private"), SET_TG("ewarp.set.other"),
		GET("ewarp.list"), GET_PR("ewarp.list.private"), GET_TG("ewarp.list.other"),
		GO("ewarp.warp"), GO_PR("ewarp.warp.private"),
		RM("ewarp.rm"), RM_TG("ewarp.rm.other"),
		CLR("ewarp.clear"), CLR_TG("ewarp.clear.other"),
		LIMIT_NONE("ewarp.limit.none");
		
		public final String p;
		
		private Perm(String perm) {
			p = perm;
		}
	}
	
	public static enum WarpResult {
		CLEAR, LIMIT, NO_PERM;
	}
}
