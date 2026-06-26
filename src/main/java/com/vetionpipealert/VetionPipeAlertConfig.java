package com.vetionpipealert;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("vetionpipealert")
public interface VetionPipeAlertConfig extends Config
{
	@ConfigItem(
		keyName = "ignoreFriendsAndClanmates",
		name = "Ignore friends & clanmates",
		description = "Do not play the sound when a friend or clan member drops into the lair",
		position = 1
	)
	default boolean ignoreFriendsAndClanmates()
	{
		return true;
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Volume of the pipe banging sound, as a percentage of the source clip's volume",
		position = 2
	)
	default int volume()
	{
		return 100;
	}
}
