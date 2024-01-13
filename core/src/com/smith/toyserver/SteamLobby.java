package com.smith.toyserver;

import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamMatchmakingKeyValuePair;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

import Screens.PlayScreen;

public class SteamLobby implements SteamMatchmakingCallback, SteamFriendsCallback, SteamUserCallback {
    private final SteamMatchmaking matchmaking;
    private final SteamFriends steamFriends;
    private final SteamUser steamUser;

    private String hostName = "";

    public boolean isHosting = false;
    public boolean isConnected = false;
    public boolean isConnecting = false;
    public SteamID currentSteamLobbyId;
    public NetworkMessageProcessor msgProcessor;
    private ToyServer game;
    public GameController gameData;

    public SteamID steamUserID;
    public void updateGameState(GameController state) throws JsonProcessingException {
        String msg = new ObjectMapper().writeValueAsString(state);
        this.matchmaking.setLobbyData(currentSteamLobbyId, "gameState", msg);
    }

    public SteamLobby(ToyServer game) {
        this.game = game;
        steamFriends = new SteamFriends(this);
        steamUser = new SteamUser(this);

        steamUserID = steamUser.getSteamID();
        matchmaking = new SteamMatchmaking(this);
    }

    public void createLobby() {
        if (isConnected || isConnecting) return;
        matchmaking.createLobby(SteamMatchmaking.LobbyType.FriendsOnly, 4);
        isConnecting = true;
    }

    @Override
    public void onFavoritesListChanged(int ip, int queryPort, int connPort, int appID, int flags, boolean add, int accountID) {

    }

    @Override
    public void onLobbyInvite(SteamID steamIDUser, SteamID steamIDLobby, long gameID) {

    }

    @Override
    // Us Entering a lobby
    public void onLobbyEnter(SteamID steamIDLobby, int chatPermissions, boolean blocked, SteamMatchmaking.ChatRoomEnterResponse response) {
        System.out.println("Entered " + matchmaking.getLobbyData(steamIDLobby, "name"));
        this.hostName = matchmaking.getLobbyData(steamIDLobby, "name");
        isConnected = true;
        isConnecting = false;
        this.currentSteamLobbyId = steamIDLobby;
        // Call main and tell them we joined a lobby
        PlayScreen playScreen = new PlayScreen(this.game);
        playScreen.client();
        this.game.swapScreen(playScreen);

    }

    public void sendNetworkMessage(String message) {
        this.matchmaking.sendLobbyChatMsg(currentSteamLobbyId, message);
    }
    public void disconnect() {
        matchmaking.leaveLobby(currentSteamLobbyId);
        this.isConnected = false;
        this.isConnecting = false;
    }
    @Override
    public void onLobbyDataUpdate(SteamID steamIDLobby, SteamID steamIDMember, boolean success) {
        if (steamIDMember == this.steamUserID) return;
        System.out.println("Lobby Data Updated");
        // Assume it's the game data
        String msg = matchmaking.getLobbyData(currentSteamLobbyId, "gameState");
        try {
            GameController state = new ObjectMapper().readValue(msg, GameController.class);
            msgProcessor.processGameState(state);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLobbyChatUpdate(SteamID steamIDLobby, SteamID steamIDUserChanged, SteamID steamIDMakingChange, SteamMatchmaking.ChatMemberStateChange stateChange) {

    }


    @Override
    public void onLobbyChatMessage(SteamID steamIDLobby, SteamID steamIDUser, SteamMatchmaking.ChatEntryType entryType, int chatID) {
        SteamMatchmaking.ChatEntry chatEntry = new SteamMatchmaking.ChatEntry();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        try {
            int thing = matchmaking.getLobbyChatEntry(steamIDLobby, chatID, chatEntry, buffer);

            byte[] bytes = new byte[thing];
            buffer.get(bytes);
            String msg = new String(bytes);
            msgProcessor.processNetworkMessage(msg);

        } catch (SteamException e) {
            System.out.println(e.getMessage());
        }
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
        System.out.println("Lobby Created: " + steamIDLobby);
        String hostAddressKey = "HostAddress";
        matchmaking.setLobbyData(steamIDLobby, new SteamMatchmakingKeyValuePair(hostAddressKey, steamUser.getSteamID().toString()));
        matchmaking.setLobbyData(steamIDLobby, "name", steamFriends.getPersonaName());
        this.hostName = steamFriends.getPersonaName();
        this.isConnected = true;
        this.isHosting = true;
        this.isConnecting = false;
        currentSteamLobbyId = steamIDLobby;
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

    public String getHostName() {
        return this.hostName;
    }
}
