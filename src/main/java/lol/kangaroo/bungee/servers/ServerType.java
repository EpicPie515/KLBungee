package lol.kangaroo.bungee.servers;

public enum ServerType {
	HUB("hub", 1, 50),
	UHC("uhc", 50, 100),
	BATTLEMATCH("blm", 100, 200),
	MINIBM("mbm", 200, 400),
	UNKNOWN("***", 3000, 3500),
	
	; // Ends the enum.
	
	private String namePrefix;
	private int minId;
	private int maxId;
	
	/**
	 * @param namePrefix can contain wildcard '*' character.
	 * @param minId Minimum ID in range for this game type, inclusive.
	 * @param maxId Maximum ID in range, exclusive.
	 */
	ServerType(String namePrefix, int minId, int maxId) {
		this.namePrefix = namePrefix;
		this.minId = minId;
		this.maxId = maxId;
	}
	
	public String getNamePrefix() {
		return namePrefix;
	}
	
	public int getMinID() {
		return minId;
	}
	
	public int getMaxID() {
		return maxId;
	}
	
	/**
	 * Returns the server type containing that id in range, or null if none.
	 */
	public static ServerType getFromID(int id) {
		for(ServerType t : values()) {
			if(id >= t.minId && id < t.maxId) return t;
		}
		return null;
	}
}
