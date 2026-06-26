package com.vetionpipealert;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VetionPipeAlertPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VetionPipeAlertPlugin.class);
		RuneLite.main(args);
	}
}
