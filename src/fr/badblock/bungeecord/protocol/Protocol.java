package fr.badblock.bungeecord.protocol;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import fr.badblock.bungeecord.protocol.packet.BossBar;
import fr.badblock.bungeecord.protocol.packet.Chat;
import fr.badblock.bungeecord.protocol.packet.ClientSettings;
import fr.badblock.bungeecord.protocol.packet.EncryptionRequest;
import fr.badblock.bungeecord.protocol.packet.EncryptionResponse;
import fr.badblock.bungeecord.protocol.packet.Handshake;
import fr.badblock.bungeecord.protocol.packet.KeepAlive;
import fr.badblock.bungeecord.protocol.packet.Kick;
import fr.badblock.bungeecord.protocol.packet.Login;
import fr.badblock.bungeecord.protocol.packet.LoginRequest;
import fr.badblock.bungeecord.protocol.packet.LoginSuccess;
import fr.badblock.bungeecord.protocol.packet.PingPacket;
import fr.badblock.bungeecord.protocol.packet.PlayerListHeaderFooter;
import fr.badblock.bungeecord.protocol.packet.PlayerListItem;
import fr.badblock.bungeecord.protocol.packet.PluginMessage;
import fr.badblock.bungeecord.protocol.packet.Respawn;
import fr.badblock.bungeecord.protocol.packet.ScoreboardDisplay;
import fr.badblock.bungeecord.protocol.packet.ScoreboardObjective;
import fr.badblock.bungeecord.protocol.packet.ScoreboardScore;
import fr.badblock.bungeecord.protocol.packet.SetCompression;
import fr.badblock.bungeecord.protocol.packet.StatusRequest;
import fr.badblock.bungeecord.protocol.packet.StatusResponse;
import fr.badblock.bungeecord.protocol.packet.TabCompleteRequest;
import fr.badblock.bungeecord.protocol.packet.TabCompleteResponse;
import fr.badblock.bungeecord.protocol.packet.Team;
import fr.badblock.bungeecord.protocol.packet.Title;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public enum Protocol
{

    // Undef
    HANDSHAKE
    {

        {
            TO_SERVER.registerPacket(
                    Handshake.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
        }
    },
    // 0
    GAME
    {

        {
            TO_CLIENT.registerPacket(
                    KeepAlive.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x1F )
            );
            TO_CLIENT.registerPacket(
                    Login.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x23 )
            );
            TO_CLIENT.registerPacket(
                    Chat.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x02 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0F )
            );
            TO_CLIENT.registerPacket(
                    Respawn.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x07 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x33 )
            );
            TO_CLIENT.registerPacket(
                    BossBar.class,
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0C )
            );
            TO_CLIENT.registerPacket(
                    PlayerListItem.class, // PlayerInfo
                    map( ProtocolConstants.MINECRAFT_1_8, 0x38 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x2D )
            );
            TO_CLIENT.registerPacket(
                    TabCompleteResponse.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3A ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0E )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardObjective.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3B ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x3F )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardScore.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3C ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x42 )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardDisplay.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3D ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x38 )
            );
            TO_CLIENT.registerPacket(
                    Team.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3E ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x41 )
            );
            TO_CLIENT.registerPacket(
                    PluginMessage.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3F ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x18 )
            );
            TO_CLIENT.registerPacket(
                    Kick.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x40 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x1A )
            );
            TO_CLIENT.registerPacket(
                    Title.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x45 )
            );
            TO_CLIENT.registerPacket(
                    PlayerListHeaderFooter.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x48 ),
                    map( ProtocolConstants.MINECRAFT_1_9_4, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_10, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_11, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_11_1, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_11_1_1, 0x47 )
            );

            TO_SERVER.registerPacket(
                    KeepAlive.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0B )
            );
            TO_SERVER.registerPacket(
                    Chat.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x02 )
            );
            TO_SERVER.registerPacket(
                    TabCompleteRequest.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x14 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x01 )
            );
            TO_SERVER.registerPacket(
                    ClientSettings.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x15 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x04 )
            );
            TO_SERVER.registerPacket(
                    PluginMessage.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x17 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x09 )
            );
        }
    },
    // 1
    STATUS
    {

        {
            TO_CLIENT.registerPacket(
                    StatusResponse.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_CLIENT.registerPacket(
                    PingPacket.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );

            TO_SERVER.registerPacket(
                    StatusRequest.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_SERVER.registerPacket(
                    PingPacket.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );
        }
    },
    //2
    LOGIN
    {

        {
            TO_CLIENT.registerPacket(
                    Kick.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_CLIENT.registerPacket(
                    EncryptionRequest.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );
            TO_CLIENT.registerPacket(
                    LoginSuccess.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x02 )
            );
            TO_CLIENT.registerPacket(
                    SetCompression.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x03 )
            );

            TO_SERVER.registerPacket(
                    LoginRequest.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_SERVER.registerPacket(
                    EncryptionResponse.class,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );
        }
    };
    /*========================================================================*/
    public static final int MAX_PACKET_ID = 0xFF;
    /*========================================================================*/
    public final DirectionData TO_SERVER = new DirectionData(this, ProtocolConstants.Direction.TO_SERVER );
    public final DirectionData TO_CLIENT = new DirectionData(this, ProtocolConstants.Direction.TO_CLIENT );

    @RequiredArgsConstructor
    private static class ProtocolData {

        @SuppressWarnings("unused")
		private final int protocolVersion;
        private final TObjectIntMap<Class<? extends DefinedPacket>> packetMap = new TObjectIntHashMap<>( MAX_PACKET_ID );
        private final TIntObjectMap<Constructor<? extends DefinedPacket>> packetConstructors = new TIntObjectHashMap<>( MAX_PACKET_ID );
    }

    @RequiredArgsConstructor
    private static class ProtocolMapping {
        private final int protocolVersion;
        private final int packetID;
    }
    // Helper method
    private static ProtocolMapping map(int protocol, int id) {
        return new ProtocolMapping(protocol, id);
    }

    @RequiredArgsConstructor
    public static class DirectionData
    {

        private final Protocol protocolPhase;
        private final TIntObjectMap<ProtocolData> protocols = new TIntObjectHashMap<>();
        {
            for ( int protocol : ProtocolConstants.SUPPORTED_VERSION_IDS )
            {
                protocols.put( protocol, new ProtocolData( protocol ) );
            }
        }
        private final TIntObjectMap<List<Integer>> linkedProtocols = new TIntObjectHashMap<>();
        {
            linkedProtocols.put( ProtocolConstants.MINECRAFT_1_8, Arrays.asList(
                    ProtocolConstants.MINECRAFT_1_9
            ) );
            linkedProtocols.put( ProtocolConstants.MINECRAFT_1_9, Arrays.asList(
                    ProtocolConstants.MINECRAFT_1_9_1,
                    ProtocolConstants.MINECRAFT_1_9_2,
                    ProtocolConstants.MINECRAFT_1_9_4,
                    ProtocolConstants.MINECRAFT_1_10,
                    ProtocolConstants.MINECRAFT_1_11,
                    ProtocolConstants.MINECRAFT_1_11_1,
                    ProtocolConstants.MINECRAFT_1_11_1_1
            ) );
        }

        @Getter
        private final ProtocolConstants.Direction direction;

        private ProtocolData getProtocolData(int version)
        {
            ProtocolData protocol = protocols.get( version );
            if ( protocol == null && ( protocolPhase != Protocol.GAME ) )
            {
                protocol = Iterables.getFirst( protocols.valueCollection(), null );
            }
            return protocol;
        }

        public final DefinedPacket createPacket(int id, int version)
        {
            ProtocolData protocolData = getProtocolData( version );
            if (protocolData == null)
            {
                throw new BadPacketException( "Unsupported protocol version" );
            }
            if ( id > MAX_PACKET_ID )
            {
                throw new BadPacketException( "Packet with id " + id + " outside of range " );
            }

            Constructor<? extends DefinedPacket> constructor = protocolData.packetConstructors.get( id );
            try
            {
                return ( constructor == null ) ? null : constructor.newInstance();
            } catch ( ReflectiveOperationException ex )
            {
                throw new BadPacketException( "Could not construct packet with id " + id, ex );
            }
        }

        protected final void registerPacket(Class<? extends DefinedPacket> packetClass, ProtocolMapping ...mappings)
        {
            try
            {
                Constructor<? extends DefinedPacket> constructor = packetClass.getDeclaredConstructor();
                for ( ProtocolMapping mapping : mappings )
                {
                    ProtocolData data = protocols.get( mapping.protocolVersion );
                    data.packetMap.put( packetClass, mapping.packetID );
                    data.packetConstructors.put( mapping.packetID, constructor );

                    List<Integer> links = linkedProtocols.get( mapping.protocolVersion );
                    if ( links != null )
                    {
                        links: for ( int link : links )
                        {
                            // Check for manual mappings
                            for ( ProtocolMapping m : mappings )
                            {
                                if ( m == mapping ) continue;
                                if ( m.protocolVersion == link ) continue links;
                                List<Integer> innerLinks = linkedProtocols.get( m.protocolVersion );
                                if ( innerLinks != null && innerLinks.contains( link ) ) continue links;
                            }
                            registerPacket( packetClass, map( link, mapping.packetID ) );
                        }
                    }
                }
            } catch ( NoSuchMethodException ex )
            {
                throw new BadPacketException( "No NoArgsConstructor for packet class " + packetClass );
            }
        }

        final int getId(Class<? extends DefinedPacket> packet, int version)
        {

            ProtocolData protocolData = getProtocolData( version );
            if (protocolData == null)
            {
                throw new BadPacketException( "Unsupported protocol version" );
            }
            Preconditions.checkArgument( protocolData.packetMap.containsKey( packet ), "Cannot get ID for packet " + packet );

            return protocolData.packetMap.get( packet );
        }
    }
}