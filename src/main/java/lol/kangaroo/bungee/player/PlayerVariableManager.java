package lol.kangaroo.bungee.player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import lol.kangaroo.common.database.DatabaseManager;
import lol.kangaroo.common.player.IPlayerVariableManager;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.DoubleObject;
import lol.kangaroo.common.util.ObjectMutable;

public class PlayerVariableManager implements IPlayerVariableManager {
	
	private DatabaseManager db;
	
	public PlayerVariableManager(DatabaseManager db) {
		this.db = db;
	}
	
	public Object getPlayerVariable(UUID uuid, PlayerVariable v) {
		ObjectMutable<Object> om = new ObjectMutable<Object>(null);
		if(v.getDBTable().equals("player_data")) {
			db.query("SELECT `VALUE` FROM `" + v.getDBTable() + "` WHERE `UUID`=? AND `TYPE`=?", new Consumer<ResultSet>() {
				@Override
				public void accept(ResultSet t) {
					try {
						if(t.next()) {
							String val = t.getString(1);
							Class<?> cl = v.getVariableClass();
							Method m = cl.getMethod(v.getValueOfMethod(), v.getValueOfArg());
							if(val != null)
								om.set(m.invoke(null, val));
							else om.set(null);
						}
					} catch (SQLException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
					
				}
			}, uuid, v.getDBColumn());
		} else {
			db.query("SELECT `" + v.getDBColumn() + "` FROM `" + v.getDBTable() + "` WHERE `UUID`=?", new Consumer<ResultSet>() {
				@Override
				public void accept(ResultSet t) {
					try {
						if(t.next()) {
							String val = t.getString(1);
							Class<?> cl = v.getVariableClass();
							Method m = cl.getMethod(v.getValueOfMethod(), v.getValueOfArg());
							if(val != null)
								om.set(m.invoke(null, val));
							else om.set(null);
						}
					} catch (SQLException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
					
				}
			}, uuid);
		}
		return om.get();
	}
	
	public Map<PlayerVariable, Object> getAllPlayerVariables(UUID uuid, Set<PlayerVariable> variables) {
		Map<PlayerVariable, Object> objects = new HashMap<>();
		Map<DoubleObject<String, Object[]>, Consumer<ResultSet>> queries = new HashMap<>();
		for(PlayerVariable pv : variables) {
			String queryStr;
			Object[] objs;
			if(pv.getDBTable().equals("player_data")) {
				queryStr = "SELECT `VALUE` FROM `" + pv.getDBTable() + "` WHERE `UUID`=? AND `TYPE`=?";
				objs = new Object[] {uuid, pv.getDBColumn()};
			} else {
				queryStr = "SELECT `" + pv.getDBColumn() + "` FROM `" + pv.getDBTable() + "` WHERE `UUID`=?";
				objs = new Object[] {uuid};
			}
			Consumer<ResultSet> con =  new Consumer<ResultSet>() {
				@Override
				public void accept(ResultSet t) {
					try {
						if(t.next()) {
							String val = t.getString(1);
							Class<?> cl = pv.getVariableClass();
							Method m = cl.getMethod(pv.getValueOfMethod(), pv.getValueOfArg());
							if(val != null)
								objects.put(pv, m.invoke(null, val));
							else objects.put(pv, null);
						}
					} catch (SQLException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						System.out.println(pv.toString() + " " + queryStr);
						e.printStackTrace();
					}
				}
			};
			queries.put(new DoubleObject<String, Object[]>(queryStr, objs), con);
		}
		db.multiQuery(queries);
		return objects;
	}
	
	public void setPlayerVariable(UUID uuid, PlayerVariable v, Object value) {
		try {
			Method toStr = v.getVariableClass().getMethod(v.getToStringMethod());
			if(v.getDBTable().equals("player_data")) {
				db.update("INSERT INTO `" + v.getDBTable() + "` (`UUID`, `TYPE`, `VALUE`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `VALUE`=?", uuid, v.getDBColumn(), toStr.invoke(value), toStr.invoke(value));
			} else {
				db.update("INSERT INTO `" + v.getDBTable() + "` (`UUID`, `" + v.getDBColumn() + "`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `" + v.getDBColumn() + "`=?", uuid, toStr.invoke(value), toStr.invoke(value));
			}
	} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		e.printStackTrace();
	}
	}
	
	public void setAllPlayerVariables(UUID uuid, Map<PlayerVariable, Object> variables) {
		Set<DoubleObject<String, Object[]>> queries = new HashSet<>();
		for(Entry<PlayerVariable, Object> entry : variables.entrySet()) {
			PlayerVariable pv = entry.getKey();
			Object value = entry.getValue();
			try {
				if(pv.getVariableClass() != value.getClass()) {
					//TODO Debug
					System.out.println("PV Defines Class as " + pv.getVariableClass() + ", Value Class is " + value.getClass());
				}
				Method toStr = pv.getVariableClass().getMethod(pv.getToStringMethod());
				String queryStr;
				Object[] objs;
				if(pv.getDBTable().equals("player_data")) {
					queryStr = "INSERT INTO `" + pv.getDBTable() + "` (`UUID`, `TYPE`, `VALUE`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `VALUE`=?";
					objs = new Object[] {uuid, pv.getDBColumn(), toStr.invoke(value), toStr.invoke(value)};
				} else {
					queryStr = "INSERT INTO `" + pv.getDBTable() + "` (`UUID`, `" + pv.getDBColumn() + "`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `" + pv.getDBColumn() + "`=?";
					objs = new Object[] {uuid, toStr.invoke(value), toStr.invoke(value)};
				}
				queries.add(new DoubleObject<>(queryStr, objs));
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		db.multiUpdate(queries);
	}
	
}
