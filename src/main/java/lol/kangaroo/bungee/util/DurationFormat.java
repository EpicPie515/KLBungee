package lol.kangaroo.bungee.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import lol.kangaroo.common.util.MSG;

public class DurationFormat {
	
	/**
	 * Includes years, days, hours, minutes.
	 */
	public static String getFormattedDuration(Duration dur) {
		long years = dur.get(ChronoUnit.YEARS);
		dur = dur.minus(years, ChronoUnit.YEARS);
		long days = dur.get(ChronoUnit.DAYS);
		dur = dur.minusDays(days);
		long hours = dur.get(ChronoUnit.HOURS);
		dur = dur.minusHours(hours);
		long minutes = dur.get(ChronoUnit.MINUTES);
		String ds = "";
		if(years > 0) ds += years + MSG.TIMEFORMAT_YEARS.getMessage(Locale.getDefault()) + ", ";
		if(days > 0) ds += days + MSG.TIMEFORMAT_DAYS.getMessage(Locale.getDefault()) + ", ";
		if(hours > 0) ds += hours + MSG.TIMEFORMAT_HOURS.getMessage(Locale.getDefault()) + ", ";
		ds += minutes + MSG.TIMEFORMAT_MINUTES.getMessage(Locale.getDefault());
		return ds;
	}
	
}
