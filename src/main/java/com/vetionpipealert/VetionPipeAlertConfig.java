package com.vetionpipealert;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("vetionpipealert")
public interface VetionPipeAlertConfig extends Config
{
	@ConfigItem(
		keyName = "ignoreFriends",
		name = "Ignore friends",
		description = "Do not play the sound when someone on your friends list drops into the lair",
		position = 1
	)
	default boolean ignoreFriends()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ignoreClanMembers",
		name = "Ignore clan members",
		description = "Do not play the sound when a member of your clan or friends chat drops into the lair",
		position = 2
	)
	default boolean ignoreClanMembers()
	{
		return true;
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Volume of the pipe banging sound, as a percentage of the source clip's volume",
		position = 3
	)
	default int volume()
	{
		return 100;
	}
}
