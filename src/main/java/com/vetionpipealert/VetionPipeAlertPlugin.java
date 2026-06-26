package com.vetionpipealert;

import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Vet'ion Pipe Alert"
)
public class VetionPipeAlertPlugin extends Plugin
{
	private static final Set<String> LAIR_BOSS_NAMES = new HashSet<>();
	private static final String PIPE_SOUND_RESOURCE = "pipebang.wav";

	static
	{
		LAIR_BOSS_NAMES.add("Vet'ion");
		LAIR_BOSS_NAMES.add("Vet'ion Reborn");
		LAIR_BOSS_NAMES.add("Calvar'ion");
		LAIR_BOSS_NAMES.add("Calvar'ion Reborn");
	}

	@Inject
	private Client client;

	@Inject
	private VetionPipeAlertConfig config;

	@Inject
	private AudioPlayer audioPlayer;

	private int lairBossCount = 0;
	private boolean settled = false;

	@Override
	protected void startUp() throws Exception
	{
		lairBossCount = 0;
		settled = false;
	}

	@Override
	protected void shutDown() throws Exception
	{
		lairBossCount = 0;
		settled = false;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (LAIR_BOSS_NAMES.contains(npc.getName()))
		{
			lairBossCount++;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		if (LAIR_BOSS_NAMES.contains(npc.getName()))
		{
			lairBossCount = Math.max(0, lairBossCount - 1);
			if (lairBossCount == 0)
			{
				settled = false;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		// Wait one tick after entering the lair before treating spawns as drop-ins, so the
		// burst of PlayerSpawned events for players already in the room when we arrive
		// doesn't get mistaken for them dropping in.
		if (lairBossCount > 0 && !settled)
		{
			settled = true;
		}
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned playerSpawned)
	{
		if (lairBossCount == 0 || !settled)
		{
			return;
		}

		Player player = playerSpawned.getPlayer();
		if (player == client.getLocalPlayer())
		{
			return;
		}

		if (config.ignoreFriendsAndClanmates() && isFriendOrClanmate(player))
		{
			return;
		}

		playPipeSound();
	}

	private boolean isFriendOrClanmate(Player player)
	{
		String name = player.getName();
		if (name == null)
		{
			return false;
		}

		if (client.isFriended(name, false))
		{
			return true;
		}

		if (client.getFriendsChatManager() != null && client.getFriendsChatManager().findByName(name) != null)
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
		catch (UnsupportedAudioFileException | LineUnavailableException | java.io.IOException e)
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
