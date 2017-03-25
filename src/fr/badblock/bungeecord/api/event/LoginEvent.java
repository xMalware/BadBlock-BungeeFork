package fr.badblock.bungeecord.api.event;

import fr.badblock.bungeecord.api.Callback;
import fr.badblock.bungeecord.api.connection.PendingConnection;
import fr.badblock.bungeecord.api.plugin.Cancellable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Event called to represent a player logging in.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class LoginEvent extends AsyncEvent<LoginEvent> implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    private String cancelReason;
    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;

    public LoginEvent(PendingConnection connection, Callback<LoginEvent> done)
    {
        super( done );
        this.connection = connection;
    }
}
