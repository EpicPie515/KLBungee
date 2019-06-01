package lol.kangaroo.bungee.permissions;

import java.util.UUID;

import lol.kangaroo.bungee.player.DatabasePlayer;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.permissions.IRankManager;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import net.md_5.bungee.api.ChatColor;

public class RankManager implements IRankManager {
	
	private PlayerManager pm;
	
	public RankManager(PlayerManager pm) {
		this.pm = pm;
	}
	
	@Override
	public String getPrefix(UUID uuid) {
		CachedPlayer pl = pm.getCachedPlayer(uuid);
		if(pl == null) return Rank.PLAYER.getRawPrefix();
		Rank rank = (Rank) pl.getVariable(PlayerVariable.RANK);
		String prefix = rank.getColor() + rank.getRawPrefix();
		if(rank.isPrefixFormatted()) {
			ChatColor c1 = (ChatColor) pl.getVariable(PlayerVariable.PREFIX_C1);
			ChatColor c2 = (ChatColor) pl.getVariable(PlayerVariable.PREFIX_C2);
			prefix = String.format(prefix, c1, c2);
		}
		return prefix;
	}

	@Override
	public String getPrefix(BasePlayer pl) {
		Rank rank = (Rank) pl.getVariable(PlayerVariable.RANK);
		String prefix = rank.getColor() + rank.getRawPrefix();
		if(rank.isPrefixFormatted()) {
			ChatColor c1 = (ChatColor) pl.getVariable(PlayerVariable.PREFIX_C1);
			ChatColor c2 = (ChatColor) pl.getVariable(PlayerVariable.PREFIX_C2);
			prefix = String.format(prefix, c1, c2);
		}
		return prefix;
	}

	@Override
	public String getPrefixDirect(UUID uuid) {
		DatabasePlayer pl = pm.getDatabasePlayer(uuid);
		if(pl == null) return Rank.PLAYER.getRawPrefix();
		Rank rank = (Rank) pl.getVariable(PlayerVariable.RANK);
		String prefix = rank.getColor() + rank.getRawPrefix();
		if(rank.isPrefixFormatted()) {
			ChatColor c1 = (ChatColor) pl.getVariable(PlayerVariable.PREFIX_C1);
			ChatColor c2 = (ChatColor) pl.getVariable(PlayerVariable.PREFIX_C2);
			prefix = String.format(prefix, c1, c2);
		}
		return prefix;
	}

	@Override
	public Rank getRank(UUID uuid) {
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		if(cp == null) return null;
		return (Rank) cp.getVariable(PlayerVariable.RANK);
	}

	@Override
	public Rank getRank(BasePlayer pl) {
		return (Rank) pl.getVariable(PlayerVariable.RANK);
	}

	@Override
	public Rank getRankDirect(UUID uuid) {
		DatabasePlayer dp = pm.getDatabasePlayer(uuid);
		if(dp == null) return null;
		return (Rank) dp.getVariable(PlayerVariable.RANK);
	}

}
