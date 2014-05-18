package me.sablednah.MinerBuddyServer;

public enum Actions {
	INVALID,
	PLAYERS,
	PLAYER,
	PLAYERCOUNT,
	SERVERINFO,
	COMMAND, 
	WORLDS,
	WORLD,
	WORLDCOUNT,
	;

	public static Actions toAction(String str) {
		try {
			if (str != null) {        	
				return valueOf(str.toUpperCase());
			} else {
				return INVALID;
			}
		} catch (Exception ex) {
			return INVALID;
		}
	}
}
