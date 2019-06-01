package lol.kangaroo.bungee.util;

public class DurationStringCalc {
	
	public static long calculate(long num, char timeUnit) {
		long mult = 1;
		switch (timeUnit) {
		case 's': break;
		case 'm': 
			mult = 60;
			break;
		case 'h':
			mult = 3600;
			break;
		case 'd':
			mult = 24*3600;
			break;
		case 'w': 
			mult = 7*24*3600;
			break;
		case 'y':
			mult = 365*24*3600;
			break;
		default: break;
		}
		return num*mult*1000;
	}
	
}
