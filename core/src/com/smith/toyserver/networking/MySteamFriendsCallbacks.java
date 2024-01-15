package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;

public class MySteamFriendsCallbacks implements SteamFriendsCallback {
    private NetworkGameController ngm;
    public MySteamFriendsCallbacks(NetworkGameController ngm) {
        this.ngm = ngm;
    }
    @Override
    public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result) {

    }

    @Override
    public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change) {

    }

    @Override
    public void onGameOverlayActivated(boolean active) {

    }

    @Override
    public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend) {
        ngm.matchmaking.joinLobby(steamIDLobby);
    }

    @Override
    public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height) {

    }

    @Override
    public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID) {

    }

    @Override
    public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect) {

    }

    @Override
    public void onGameServerChangeRequested(String server, String password) {

    }
}
