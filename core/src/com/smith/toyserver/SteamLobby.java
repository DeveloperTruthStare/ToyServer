package com.smith.toyserver;

import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamMatchmakingKeyValuePair;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;

public class SteamLobby implements SteamMatchmakingCallback, SteamFriendsCallback, SteamUserCallback {
    private SteamMatchmaking matchmaking;
    private SteamFriends steamFriends;
    private SteamUser steamUser;
    private String hostAddressKey = "HostAddress";
    public SteamLobby() {
        steamFriends = new SteamFriends(this);
        steamUser = new SteamUser(this);

        matchmaking = new SteamMatchmaking(this);
        matchmaking.createLobby(SteamMatchmaking.LobbyType.FriendsOnly, 5);

    }

    @Override
    public void onFavoritesListChanged(int ip, int queryPort, int connPort, int appID, int flags, boolean add, int accountID) {

    }

    @Override
    public void onLobbyInvite(SteamID steamIDUser, SteamID steamIDLobby, long gameID) {

    }

    @Override
    public void onLobbyEnter(SteamID steamIDLobby, int chatPermissions, boolean blocked, SteamMatchmaking.ChatRoomEnterResponse response) {
        System.out.println("Lobby entered");
        System.out.println("Entered " + matchmaking.getLobbyData(steamIDLobby, "name"));

    }

    @Override
    public void onLobbyDataUpdate(SteamID steamIDLobby, SteamID steamIDMember, boolean success) {

    }

    @Override
    public void onLobbyChatUpdate(SteamID steamIDLobby, SteamID steamIDUserChanged, SteamID steamIDMakingChange, SteamMatchmaking.ChatMemberStateChange stateChange) {

    }

    @Override
    public void onLobbyChatMessage(SteamID steamIDLobby, SteamID steamIDUser, SteamMatchmaking.ChatEntryType entryType, int chatID) {

    }

    @Override
    public void onLobbyGameCreated(SteamID steamIDLobby, SteamID steamIDGameServer, int ip, short port) {
    }

    @Override
    public void onLobbyMatchList(int lobbiesMatching) {

    }

    @Override
    public void onLobbyKicked(SteamID steamIDLobby, SteamID steamIDAdmin, boolean kickedDueToDisconnect) {

    }

    @Override
    public void onLobbyCreated(SteamResult result, SteamID steamIDLobby) {
        matchmaking.setLobbyData(steamIDLobby, new SteamMatchmakingKeyValuePair(hostAddressKey, steamUser.getSteamID().toString()));
        matchmaking.setLobbyData(steamIDLobby, "name", steamFriends.getPersonaName() + "'s Lobby");
        System.out.println("Lobby Created");

    }

    @Override
    public void onFavoritesListAccountsUpdated(SteamResult result) {

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
        System.out.println("Lobby join request");
        matchmaking.joinLobby(steamIDLobby);
    }

    @Override
    public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height) {

    }

    @Override
    public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID) {

    }

    @Override
    public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect) {
        System.out.println("Rich Presence Join Request");
    }

    @Override
    public void onGameServerChangeRequested(String server, String password) {

    }

    @Override
    public void onAuthSessionTicket(SteamAuthTicket authTicket, SteamResult result) {

    }

    @Override
    public void onValidateAuthTicket(SteamID steamID, SteamAuth.AuthSessionResponse authSessionResponse, SteamID ownerSteamID) {

    }

    @Override
    public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {

    }

    @Override
    public void onEncryptedAppTicket(SteamResult result) {

    }
}
