package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TabCompleteRequest extends DefinedPacket
{

    private int transactionId;
    private String cursor;
    private boolean assumeCommand;
    private boolean hasPositon;
    private long position;

    public TabCompleteRequest(int transactionId, String cursor)
    {
        this.transactionId = transactionId;
        this.cursor = cursor;
    }

    public TabCompleteRequest(String cursor, boolean assumeCommand, boolean hasPosition, long position)
    {
        this.cursor = cursor;
        this.assumeCommand = assumeCommand;
        this.hasPositon = hasPosition;
        this.position = position;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
        {
            transactionId = readVarInt( buf );
        }
        cursor = readString( buf );

        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_13 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                assumeCommand = buf.readBoolean();
            }

            if ( hasPositon = buf.readBoolean() )
            {
                position = buf.readLong();
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
        {
            writeVarInt( transactionId, buf );
        }
        writeString( cursor, buf );

        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_13 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                buf.writeBoolean( assumeCommand );
            }

            buf.writeBoolean( hasPositon );
            if ( hasPositon )
            {
                buf.writeLong( position );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}