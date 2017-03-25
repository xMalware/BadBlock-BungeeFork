package fr.badblock.bungeecord.connection;

import java.io.DataInput;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fr.badblock.bungeecord.ServerConnection;
import fr.badblock.bungeecord.UserConnection;
import fr.badblock.bungeecord.Util;
import fr.badblock.bungeecord.api.ProxyServer;
import fr.badblock.bungeecord.api.chat.TextComponent;
import fr.badblock.bungeecord.api.config.ServerInfo;
import fr.badblock.bungeecord.api.connection.ProxiedPlayer;
import fr.badblock.bungeecord.api.event.PluginMessageEvent;
import fr.badblock.bungeecord.api.event.ServerDisconnectEvent;
import fr.badblock.bungeecord.api.event.ServerKickEvent;
import fr.badblock.bungeecord.api.event.TabCompleteResponseEvent;
import fr.badblock.bungeecord.api.score.Objective;
import fr.badblock.bungeecord.api.score.Position;
import fr.badblock.bungeecord.api.score.Score;
import fr.badblock.bungeecord.api.score.Scoreboard;
import fr.badblock.bungeecord.api.score.Team;
import fr.badblock.bungeecord.chat.ComponentSerializer;
import fr.badblock.bungeecord.netty.ChannelWrapper;
import fr.badblock.bungeecord.netty.PacketHandler;
import fr.badblock.bungeecord.protocol.DefinedPacket;
import fr.badblock.bungeecord.protocol.PacketWrapper;
import fr.badblock.bungeecord.protocol.packet.BossBar;
import fr.badblock.bungeecord.protocol.packet.KeepAlive;
import fr.badblock.bungeecord.protocol.packet.Kick;
import fr.badblock.bungeecord.protocol.packet.PlayerListItem;
import fr.badblock.bungeecord.protocol.packet.PluginMessage;
import fr.badblock.bungeecord.protocol.packet.ScoreboardDisplay;
import fr.badblock.bungeecord.protocol.packet.ScoreboardObjective;
import fr.badblock.bungeecord.protocol.packet.ScoreboardScore;
import fr.badblock.bungeecord.protocol.packet.SetCompression;
import fr.badblock.bungeecord.protocol.packet.TabCompleteResponse;
import fr.badblock.bungeecord.tab.TabList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DownstreamBridge extends PacketHandler
{

    private final ProxyServer bungee;
    private final UserConnection con;
    private final ServerConnection server;

    @Override
    public void exception(Throwable t) throws Exception
    {
        if ( server.isObsolete() )
        {
            // do not perform any actions if the user has already moved
            return;
        }

        ServerInfo def = con.updateAndGetNextServer( server.getInfo() );
        if ( def != null )
        {
            server.setObsolete( true );
            con.connectNow( def );
            con.sendMessage( bungee.getTranslation( "server_went_down" ) );
        } else
        {
            con.disconnect( Util.exception( t ) );
        }
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        // We lost connection to the server
        server.getInfo().removePlayer( con );
        if ( bungee.getReconnectHandler() != null )
        {
            bungee.getReconnectHandler().setServer( con );
        }

        if ( !server.isObsolete() )
        {
            con.disconnect( bungee.getTranslation( "lost_connection" ) );
        }

        ServerDisconnectEvent serverDisconnectEvent = new ServerDisconnectEvent( con, server.getInfo() );
        bungee.getPluginManager().callEvent( serverDisconnectEvent );
    }

    @Override
    public boolean shouldHandle(PacketWrapper packet) throws Exception
    {
        return !server.isObsolete();
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        con.getEntityRewrite().rewriteClientbound( packet.buf, con.getServerEntityId(), con.getClientEntityId() );
        con.sendPacket( packet );
    }

    @Override
    public void handle(KeepAlive alive) throws Exception
    {
        con.setSentPingId( alive.getRandomId() );
        con.setSentPingTime( System.currentTimeMillis() );
    }

    @Override
    public void handle(PlayerListItem playerList) throws Exception
    {
        con.getTabListHandler().onUpdate( TabList.rewrite( playerList ) );
        throw CancelSendSignal.INSTANCE; // Always throw because of profile rewriting
    }

    @Override
    public void handle(ScoreboardObjective objective) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        switch ( objective.getAction() )
        {
            case 0:
                serverScoreboard.addObjective( new Objective( objective.getName(), objective.getValue(), objective.getType() ) );
                break;
            case 1:
                serverScoreboard.removeObjective( objective.getName() );
                break;
            case 2:
                Objective oldObjective = serverScoreboard.getObjective( objective.getName() );
                if ( oldObjective != null )
                {
                    oldObjective.setValue( objective.getValue() );
                }
                break;
            default:
                throw new IllegalArgumentException( "Unknown objective action: " + objective.getAction() );
        }
    }

    @Override
    public void handle(ScoreboardScore score) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        switch ( score.getAction() )
        {
            case 0:
                Score s = new Score( score.getItemName(), score.getScoreName(), score.getValue() );
                serverScoreboard.removeScore( score.getItemName() );
                serverScoreboard.addScore( s );
                break;
            case 1:
                serverScoreboard.removeScore( score.getItemName() );
                break;
            default:
                throw new IllegalArgumentException( "Unknown scoreboard action: " + score.getAction() );
        }
    }

    @Override
    public void handle(ScoreboardDisplay displayScoreboard) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        serverScoreboard.setName( displayScoreboard.getName() );
        serverScoreboard.setPosition( Position.values()[displayScoreboard.getPosition()] );
    }

    @Override
    public void handle(fr.badblock.bungeecord.protocol.packet.Team team) throws Exception
    {
        Scoreboard serverScoreboard = con.getServerSentScoreboard();
        // Remove team and move on
        if ( team.getMode() == 1 )
        {
            serverScoreboard.removeTeam( team.getName() );
            return;
        }

        // Create or get old team
        Team t;
        if ( team.getMode() == 0 )
        {
            t = new Team( team.getName() );
            serverScoreboard.addTeam( t );
        } else
        {
            t = serverScoreboard.getTeam( team.getName() );
        }

        if ( t != null )
        {
            if ( team.getMode() == 0 || team.getMode() == 2 )
            {
                t.setDisplayName( team.getDisplayName() );
                t.setPrefix( team.getPrefix() );
                t.setSuffix( team.getSuffix() );
                t.setFriendlyFire( team.getFriendlyFire() );
                t.setNameTagVisibility( team.getNameTagVisibility() );
                t.setCollisionRule( team.getCollisionRule() );
                t.setColor( team.getColor() );
            }
            if ( team.getPlayers() != null )
            {
                for ( String s : team.getPlayers() )
                {
                    if ( team.getMode() == 0 || team.getMode() == 3 )
                    {
                        t.addPlayer( s );
                    } else
                    {
                        t.removePlayer( s );
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "deprecation" })
	@Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        DataInput in = pluginMessage.getStream();
        PluginMessageEvent event = new PluginMessageEvent( con.getServer(), con, pluginMessage.getTag(), pluginMessage.getData().clone() );

        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            throw CancelSendSignal.INSTANCE;
        }

        if ( pluginMessage.getTag().equals( "MC|Brand" ) )
        {
            ByteBuf brand = Unpooled.wrappedBuffer( pluginMessage.getData() );
            String serverBrand = DefinedPacket.readString( brand );
            brand.release();
            brand = ByteBufAllocator.DEFAULT.heapBuffer();
            DefinedPacket.writeString( bungee.getName() + " (" + bungee.getVersion() + ")" + " <- " + serverBrand, brand );
            pluginMessage.setData( DefinedPacket.readArray( brand ) );
            brand.release();
            // changes in the packet are ignored so we need to send it manually
            con.unsafe().sendPacket( pluginMessage );
            throw CancelSendSignal.INSTANCE;
        }

        if ( pluginMessage.getTag().equals( "BungeeCord" ) )
        {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            String subChannel = in.readUTF();

            if ( subChannel.equals( "ForwardToPlayer" ) )
            {
                ProxiedPlayer target = bungee.getPlayer( in.readUTF() );
                if ( target != null )
                {
                    // Read data from server
                    String channel = in.readUTF();
                    short len = in.readShort();
                    byte[] data = new byte[ len ];
                    in.readFully( data );

                    // Prepare new data to send
                    out.writeUTF( channel );
                    out.writeShort( data.length );
                    out.write( data );
                    byte[] payload = out.toByteArray();

                    target.getServer().sendData( "BungeeCord", payload );
                }

                // Null out stream, important as we don't want to send to ourselves
                out = null;
            }
            if ( subChannel.equals( "Forward" ) )
            {
                // Read data from server
                String target = in.readUTF();
                String channel = in.readUTF();
                short len = in.readShort();
                byte[] data = new byte[ len ];
                in.readFully( data );

                // Prepare new data to send
                out.writeUTF( channel );
                out.writeShort( data.length );
                out.write( data );
                byte[] payload = out.toByteArray();

                // Null out stream, important as we don't want to send to ourselves
                out = null;

                if ( target.equals( "ALL" ) )
                {
                    for ( ServerInfo server : bungee.getServers().values() )
                    {
                        if ( server != con.getServer().getInfo() )
                        {
                            server.sendData( "BungeeCord", payload );
                        }
                    }
                } else if ( target.equals( "ONLINE" ) )
                {
                    for ( ServerInfo server : bungee.getServers().values() )
                    {
                        if ( server != con.getServer().getInfo() )
                        {
                            server.sendData( "BungeeCord", payload, false );
                        }
                    }
                } else
                {
                    ServerInfo server = bungee.getServerInfo( target );
                    if ( server != null )
                    {
                        server.sendData( "BungeeCord", payload );
                    }
                }
            }
            if ( subChannel.equals( "Connect" ) )
            {
                ServerInfo server = bungee.getServerInfo( in.readUTF() );
                if ( server != null )
                {
                    con.connect( server );
                }
            }
            if ( subChannel.equals( "ConnectOther" ) )
            {
                ProxiedPlayer player = bungee.getPlayer( in.readUTF() );
                if ( player != null )
                {
                    ServerInfo server = bungee.getServerInfo( in.readUTF() );
                    if ( server != null )
                    {
                        player.connect( server );
                    }
                }
            }
            if ( subChannel.equals( "IP" ) )
            {
                out.writeUTF( "IP" );
                out.writeUTF( con.getAddress().getHostString() );
                out.writeInt( con.getAddress().getPort() );
            }
            if ( subChannel.equals( "PlayerCount" ) )
            {
                String target = in.readUTF();
                out.writeUTF( "PlayerCount" );
                if ( target.equals( "ALL" ) )
                {
                    out.writeUTF( "ALL" );
                    out.writeInt( 0 );
                } else
                {
                    ServerInfo server = bungee.getServerInfo( target );
                    if ( server != null )
                    {
                        out.writeUTF( server.getName() );
                        out.writeInt( server.getPlayers().size() );
                    }
                }
            }
            if ( subChannel.equals( "PlayerList" ) )
            {
                String target = in.readUTF();
                out.writeUTF( "PlayerList" );
                if ( target.equals( "ALL" ) )
                {
                    out.writeUTF( "ALL" );
                    out.writeUTF( Util.csv( bungee.getPlayers() ) );
                } else
                {
                    ServerInfo server = bungee.getServerInfo( target );
                    if ( server != null )
                    {
                        out.writeUTF( server.getName() );
                        out.writeUTF( Util.csv( server.getPlayers() ) );
                    }
                }
            }
            if ( subChannel.equals( "GetServers" ) )
            {
                out.writeUTF( "GetServers" );
                out.writeUTF( Util.csv( bungee.getServers().keySet() ) );
            }
            if ( subChannel.equals( "Message" ) )
            {
                ProxiedPlayer target = bungee.getPlayer( in.readUTF() );
                if ( target != null )
                {
                    target.sendMessage( in.readUTF() );
                }
            }
            if ( subChannel.equals( "GetServer" ) )
            {
                out.writeUTF( "GetServer" );
                out.writeUTF( server.getInfo().getName() );
            }
            if ( subChannel.equals( "UUID" ) )
            {
                out.writeUTF( "UUID" );
                out.writeUTF( con.getUUID() );
            }
            if ( subChannel.equals( "UUIDOther" ) )
            {
                ProxiedPlayer player = bungee.getPlayer( in.readUTF() );
                if ( player != null )
                {
                    out.writeUTF( "UUIDOther" );
                    out.writeUTF( player.getName() );
                    out.writeUTF( player.getUUID() );
                }
            }
            if ( subChannel.equals( "ServerIP" ) )
            {
                ServerInfo info = bungee.getServerInfo( in.readUTF() );
                if ( info != null )
                {
                    out.writeUTF( "ServerIP" );
                    out.writeUTF( info.getName() );
                    out.writeUTF( info.getAddress().getAddress().getHostAddress() );
                    out.writeShort( info.getAddress().getPort() );
                }
            }
            if ( subChannel.equals( "KickPlayer" ) )
            {
                ProxiedPlayer player = bungee.getPlayer( in.readUTF() );
                if ( player != null )
                {
                    String kickReason = in.readUTF();
                    player.disconnect( new TextComponent( kickReason ) );
                }
            }

            // Check we haven't set out to null, and we have written data, if so reply back back along the BungeeCord channel
            if ( out != null )
            {
                byte[] b = out.toByteArray();
                if ( b.length != 0 )
                {
                    con.getServer().sendData( "BungeeCord", b );
                }
            }

            throw CancelSendSignal.INSTANCE;
        }
    }

	@Override
    public void handle(Kick kick) throws Exception
    {
    	ServerInfo def = con.updateAndGetNextServer( server.getInfo() );
        ServerKickEvent event = bungee.getPluginManager().callEvent( new ServerKickEvent( con, server.getInfo(), ComponentSerializer.parse( kick.getMessage() ), def, ServerKickEvent.State.CONNECTED ) );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            con.connectNow( event.getCancelServer() );
        } else
        {
            con.disconnect0( event.getKickReasonComponent() ); // TODO: Prefix our own stuff.
        }
        server.setObsolete( true );
        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(SetCompression setCompression) throws Exception
    {
        server.getCh().setCompressionThreshold( setCompression.getThreshold() );
    }

    @Override
    public void handle(TabCompleteResponse tabCompleteResponse) throws Exception
    {
        TabCompleteResponseEvent tabCompleteResponseEvent = new TabCompleteResponseEvent( con.getServer(), con, tabCompleteResponse.getCommands() );

        if ( !bungee.getPluginManager().callEvent( tabCompleteResponseEvent ).isCancelled() )
        {
            con.unsafe().sendPacket( tabCompleteResponse );
        }

        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void handle(BossBar bossBar)
    {
        switch ( bossBar.getAction() )
        {
            // Handle add bossbar
            case 0:
                con.getSentBossBars().add( bossBar.getUuid() );
                break;
            // Handle remove bossbar
            case 1:
                con.getSentBossBars().remove( bossBar.getUuid() );
                break;
        }
    }

    @Override
    public String toString()
    {
        return "[" + con.getName() + "] <-> DownstreamBridge <-> [" + server.getInfo().getName() + "]";
    }
}
