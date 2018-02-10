package fr.badblock.bungee.api.events.objects.friendlist;

import fr.badblock.bungee.api.CancellableEvent;
import fr.badblock.bungee.players.BadPlayer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
public class FriendListRequestEvent extends CancellableEvent {
    private final BadPlayer wantPlayer;
    private final BadPlayer wantedPlayer;
    private FriendListRequestStatus status;

    public enum FriendListRequestStatus {
        PLAYER_SCHIZOPHRENIA,
        PLAYERS_ALREADY_FRIENDS,
        PLAYERS_NOW_FRIENDS,
        PLAYER_ALREADY_REQUESTED,
        PLAYER_RECEIVE_REQUEST,
        PLAYER_DONT_ACCEPT_REQUEST,
        UNKNOWN_ERROR
    }
}
