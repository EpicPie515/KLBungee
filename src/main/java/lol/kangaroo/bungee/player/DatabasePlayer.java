package lol.kangaroo.bungee.player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lol.kangaroo.bungee.player.punish.PunishManager;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.punish.Punishment;

public class DatabasePlayer extends BasePlayer {
	
	private PlayerVariableManager pvm;
	
	DatabasePlayer(UUID uuid, PlayerVariableManager pvm, PunishManager pm) {
		super(uuid, pvm, pm);
		this.pvm = pvm;
	}	

	public Object getVariable(PlayerVariable pv) {
		return pvm.getPlayerVariable(uuid, pv);
	}
	
	public void setVariable(PlayerVariable pv, Object value) {
		pvm.setPlayerVariable(uuid, pv, value);
	}
	
	/**
	 * Gets the full map of variables from the database.
	 * @return Map of all PlayerVariables.
	 */
	public Map<PlayerVariable, Object> getAllVariablesMap() {
		Set<PlayerVariable> varKeys = new HashSet<>();
		for(PlayerVariable pv : PlayerVariable.values())
			varKeys.add(pv);
		return pvm.getAllPlayerVariables(uuid, varKeys);
	}
	
	/**
	 * Updates the database with all of the new variables.
	 * 
	 * Unlike @CachedPlayer, this doesn't require ALL variables defined in the map.
	 * 
	 * @param map the new variables.
	 */
	public void setAllVariablesMap(Map<PlayerVariable, Object> map) {
		pvm.setAllPlayerVariables(uuid, map);
	}

	@Override
	public Set<Punishment> getActivePunishments() {
		return pm.getActivePunishments(getUniqueId());
	}

	@Override
	public Set<Punishment> getPunishments() {
		return pm.getPunishments(getUniqueId());
	}
	
}
