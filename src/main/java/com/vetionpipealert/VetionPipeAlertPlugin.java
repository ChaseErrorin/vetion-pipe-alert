package com.vetionpipealert;

import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Vetion Pipe Alert"
)
public class VetionPipeAlertPlugin extends Plugin
{
	private static final Set<Integer> LAIR_REGIONS = new HashSet<>();
	private static final String PIPE_SOUND_RESOURCE = "pipebang.wav";
	private static final int COOLDOWN_TICKS = 8;

	static
	{
		LAIR_REGIONS.add(7604);   // Calvar'ion's lair (Skeletal Tomb)
		LAIR_REGIONS.add(13215);  // Vet'ion's Rest
	}

	@Inject
	private Client client;

	@Inject
	private VetionPipeAlertConfig config;

	@Inject
	private AudioPlayer audioPlayer;

	private boolean inLair = false;
	private boolean settled = false;
	private int lastPipeTick = -COOLDOWN_TICKS;

	@Override
	protected void startUp() throws Exception
	{
		inLair = false;
		settled = false;
		lastPipeTick = -COOLDOWN_TICKS;
	}

	@Override
	protected void shutDown() throws Exception
	{
		inLair = false;
		settled = false;
		lastPipeTick = -COOLDOWN_TICKS;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		Player local = client.getLocalPlayer();
		boolean nowInLair = false;
		if (local != null)
		{
			WorldPoint location = local.getWorldLocation();
			nowInLair = location != null && LAIR_REGIONS.contains(location.getRegionID());
		}

		if (!nowInLair)
		{
			inLair = false;
			settled = false;
			return;
		}

		if (!inLair)
		{
			// Just entered the lair this tick. Wait until next tick before treating player
			// spawns as drop-ins, so the players already in the room when we arrive don't
			// trigger the alert.
			inLair = true;
			settled = false;
		}
		else
		{
			settled = true;
		}
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned playerSpawned)
	{
		if (!inLair || !settled)
		{
			return;
		}

		Player player = playerSpawned.getPlayer();
		if (player == client.getLocalPlayer())
		{
			return;
		}

		if (config.ignoreFriends() && isFriend(player))
		{
			return;
		}

		if (config.ignoreClanMembers() && isClanMember(player))
		{
			return;
		}

		if (config.cooldown() && client.getTickCount() - lastPipeTick < COOLDOWN_TICKS)
		{
			return;
		}

		lastPipeTick = client.getTickCount();
		playPipeSound();
	}

	private boolean isFriend(Player player)
	{
		String name = player.getName();
		return name != null && client.isFriended(name, false);
	}

	private boolean isClanMember(Player player)
	{
		String name = player.getName();
		if (name == null)
		{
			return false;
		}

		FriendsChatManager friendsChatManager = client.getFriendsChatManager();
		if (friendsChatManager != null && friendsChatManager.findByName(name) != null)
		{
			return true;
		}

		ClanChannel clanChannel = client.getClanChannel();
		return clanChannel != null && clanChannel.findMember(name) != null;
	}

	private void playPipeSound()
	{
		int volumePercent = config.volume();
		if (volumePercent <= 0)
		{
			return;
		}

		float gainDb = (float) (20 * Math.log10(volumePercent / 100.0));

		try
		{
			audioPlayer.play(VetionPipeAlertPlugin.class, PIPE_SOUND_RESOURCE, gainDb);
		}
		catch (Exception e)
		{
			log.warn("Unable to play pipe banging sound", e);
		}
	}

	@Provides
	VetionPipeAlertConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VetionPipeAlertConfig.class);
	}
}
