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

	@ConfigItem(
		keyName = "cooldown",
		name = "Only 1 alert per team",
		description = "When enabled, only play one sound every few seconds so a team dropping in together triggers a single bang instead of many overlapping ones",
		position = 3
	)
	default boolean cooldown()
	{
		return false;
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Volume of the pipe banging sound, can be boosted up to 200",
		position = 4
	)
	default int volume()
	{
		return 100;
	}
}
