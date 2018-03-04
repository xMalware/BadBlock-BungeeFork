package fr.badblock.bungee.link.processing.players.abstracts;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class _PlayerProcessing
{
	
	public void work(PlayerPacket playerPacket)
	{
		ProxiedPlayer proxiedPlayer = getProxiedPlayerByPacket(playerPacket);
		if (proxiedPlayer == null)
		{
			return;
		}
		done(proxiedPlayer, playerPacket);
	}
	
	protected ProxiedPlayer getProxiedPlayerByPacket(PlayerPacket playerPacket)
	{
		String playerName = playerPacket.getPlayerName();
		BungeeCord bungeeCord = BungeeCord.getInstance();
		return bungeeCord.getPlayer(playerName);
	}
	
	public abstract void done(ProxiedPlayer proxiedPlayer, PlayerPacket playerPacket);
	
}
