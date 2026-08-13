package org.adrianvictor.healerbed;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {
	public static int healAmount;
	public static int feedAmount;
	public static int saturationAmount;
	public static boolean requireSleepUntilMorning;
	
	public static void load(File file) {
		Configuration config = new Configuration(file);
		
		try {
			config.load();
			
			healAmount = config.getInt(
					"healAmount",
					Configuration.CATEGORY_GENERAL,
					4,
					0,
					20,
					"Amount of health to restore per night; 2 is one heart.");
			
			saturationAmount = config.getInt(
					"saturationAmount",
					Configuration.CATEGORY_GENERAL,
					0,
					0,
					20,
					"Invisible hunger acumulated when eating multiple times, will be consumed before visible hunger.");
			
			feedAmount = config.getInt(
					"feedAmount",
					Configuration.CATEGORY_GENERAL,
					0,
					0,
					20,
					"Amount of hunger to restore per night; 2 is one slot.");
			
			requireSleepUntilMorning = config.getBoolean(
					"requireSleepUntilMorning",
					Configuration.CATEGORY_GENERAL,
					true,
					"Only heal when player leaves the bed at daytime, otherwise the player can get out the bed early and still heal.");
		} finally {
			if (config.hasChanged()) {
				config.save();
			}
		}
	}
}
