package fr.badblock.bungee.link.processing.bungee;

import fr.badblock.bungee.link.processing.bungee.abstracts._BungeeProcessing;
import net.md_5.bungee.api.ProxyServer;

public class BungeeBroadcastProcessing extends _BungeeProcessing
{

	@SuppressWarnings("deprecation")
	@Override
	public void done(String message)
	{
		for (String string : message.split(System.lineSeparator()))
		{
			ProxyServer.getInstance().broadcast(string);
		}
	}

}
