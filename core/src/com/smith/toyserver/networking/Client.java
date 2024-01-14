package com.smith.toyserver.networking;

import com.badlogic.gdx.Game;
import com.codedisaster.steamworks.SteamAPIWarningMessageHook;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamGameServerNetworking;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.codedisaster.steamworks.SteamUtils;
import com.codedisaster.steamworks.SteamUtilsCallback;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    protected SteamUtils clientUtils;


    private final SteamAPIWarningMessageHook clMessageHook = new SteamAPIWarningMessageHook() {
        @Override
        public void onWarningMessage(int severity, String message) {
            System.err.println("[client debug message] (" + severity + ") " + message);
        }
    };

    private final SteamUtilsCallback clUtilsCallback = new SteamUtilsCallback() {
        @Override
        public void onSteamShutdown() {
            System.err.println("Steam client requested to shut down!");
        }
    };
    private static final int defaultChannel = 1;
    private static final Charset messageCharset = StandardCharsets.UTF_8;

    private static final int sendBufferCapacity = 4096;

    private ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBufferCapacity);

    private class InputHandler implements Runnable {

        private volatile boolean alive;
        private final Thread mainThread;
        private final Scanner scanner;

        public InputHandler(Thread mainThread) {
            this.alive = true;
            this.mainThread = mainThread;

            this.scanner = new Scanner(System.in, "UTF-8");
            scanner.useDelimiter("[\r\n\t]");
        }

        @Override
        public void run() {
            while (alive && mainThread.isAlive()) {
                if (scanner.hasNext()) {
                    String input = scanner.next();
                    if (input.equals("quit") || input.equals("exit")) {
                        alive = false;
                    } else {
                        try {
                            processInput(input);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public boolean alive() {
            return alive;
        }
    }


    private SteamNetworkingCallback peer2peerCallback = new SteamNetworkingCallback() {
        @Override
        public void onP2PSessionConnectFail(SteamID steamIDRemote, SteamNetworking.P2PSessionError sessionError) {
            System.out.println("P2P connection failed: userID=" + steamIDRemote.getAccountID() +
                    ", error: " + sessionError);

        }

        @Override
        public void onP2PSessionRequest(SteamID steamIDRemote) {
            System.out.println("P2P connection requested by userID " + steamIDRemote.getAccountID());
            networking.acceptP2PSessionWithUser(steamIDRemote);
        }
    };
    private SteamMatchmakingCallback matchmakingCallback = new SteamMatchmakingCallback() {
        @Override
        public void onFavoritesListChanged(int ip, int queryPort, int connPort, int appID, int flags, boolean add, int accountID) {

        }

        @Override
        public void onLobbyInvite(SteamID steamIDUser, SteamID steamIDLobby, long gameID) {

        }

        @Override
        public void onLobbyEnter(SteamID steamIDLobby, int chatPermissions, boolean blocked, SteamMatchmaking.ChatRoomEnterResponse response) {
            // Get Host Id
            System.out.println("Joined Lobby");
            int accountId = Integer.parseInt(matchmaking.getLobbyData(steamIDLobby, "hostSteamID"));

            int friends = steamFriends.getFriendCount(SteamFriends.FriendFlags.Immediate);

            for(int i = 0; i < friends; ++i) {
                SteamID friend = steamFriends.getFriendByIndex(i, SteamFriends.FriendFlags.Immediate);
                if (friend.getAccountID() == accountId) {
                    hostSteamID = friend;
                    return;
                }
            }
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

        }

        @Override
        public void onFavoritesListAccountsUpdated(SteamResult result) {

        }
    };
    private SteamFriendsCallback friendsCallback = new SteamFriendsCallback() {
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

        }

        @Override
        public void onGameServerChangeRequested(String server, String password) {

        }
    };

    private Map<Integer, SteamID> remoteUserIDs = new ConcurrentHashMap<Integer, SteamID>();

    private SteamAuthTicket userAuthTicket;
    private ByteBuffer userAuthTicketData = ByteBuffer.allocateDirect(256);

    private SteamID remoteAuthUser;
    private ByteBuffer remoteAuthTicketData = ByteBuffer.allocateDirect(256);

    private final byte[] AUTH = "AUTH".getBytes(Charset.defaultCharset());
    private SteamUser user;


    private SteamUserCallback userCallback = new SteamUserCallback() {
        @Override
        public void onAuthSessionTicket(SteamAuthTicket authTicket, SteamResult result) {

        }

        @Override
        public void onValidateAuthTicket(SteamID steamID,
                                         SteamAuth.AuthSessionResponse authSessionResponse,
                                         SteamID ownerSteamID) {

            System.out.println("Auth session response for userID " + steamID.getAccountID() + ": " +
                    authSessionResponse.name() + ", borrowed=" + (steamID.equals(ownerSteamID) ? "yes" : "no"));

            if (authSessionResponse == SteamAuth.AuthSessionResponse.AuthTicketCanceled) {
                // ticket owner has cancelled the ticket, end the session
                endAuthSession();
            }
        }

        @Override
        public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {

        }

        @Override
        public void onEncryptedAppTicket(SteamResult result) {

        }
    };


    private SteamNetworking networking;
    private SteamMatchmaking matchmaking;
    private SteamFriends steamFriends;


    private SteamID hostSteamID;
    public Client() {
        networking = new SteamNetworking(peer2peerCallback);
        matchmaking = new SteamMatchmaking(matchmakingCallback);
        steamFriends = new SteamFriends(friendsCallback);
        user = new SteamUser(userCallback);

        clientUtils = new SteamUtils(clUtilsCallback);
        clientUtils.setWarningMessageHook(clMessageHook);
    }
    public void start() {
        Client.InputHandler inputHandler = new Client.InputHandler(Thread.currentThread());
        Thread inputThread = new Thread(inputHandler);
        inputThread.start();
    }
    public void processInput(String msg) {
        if (hostSteamID == null) return;
        try {
            broadcastAuthTicket();
        } catch (SteamException e) {
            throw new RuntimeException(e);
        }
        /*

        packetSendBuffer.clear(); // pos=0, limit=cap

        byte[] bytes = msg.getBytes();
        packetSendBuffer.put(bytes);

        packetSendBuffer.flip(); // limit=pos, pos=0
        System.out.println("Pack Send " + messageCharset.decode(packetSendBuffer).toString());
        try {
            networking.sendP2PPacket(hostSteamID, packetSendBuffer,
                    SteamNetworking.P2PSend.Unreliable, defaultChannel);
        } catch (SteamException e) {
            throw new RuntimeException(e);
        }
         */
    }



    private void registerRemoteSteamID(SteamID steamIDUser) {
        if (!remoteUserIDs.containsKey(steamIDUser.getAccountID())) {
            remoteUserIDs.put(steamIDUser.getAccountID(), steamIDUser);
        }
    }

    private void unregisterRemoteSteamID(SteamID steamIDUser) {
        remoteUserIDs.remove(steamIDUser.getAccountID());
    }

    private void getAuthTicket() throws SteamException {
        cancelAuthTicket();
        userAuthTicketData.clear();
        int[] sizeRequired = new int[1];
        userAuthTicket = user.getAuthSessionTicket(userAuthTicketData, sizeRequired);
        if (userAuthTicket.isValid()) {
            int numBytes = userAuthTicketData.limit();
            System.out.println("Auth session ticket length: " + numBytes);
            System.out.println("Auth ticket created: " + userAuthTicketData.toString() +
                    " [hash: " + userAuthTicketData.hashCode() + "]");
        } else {
            if (sizeRequired[0] < userAuthTicketData.capacity()) {
                System.out.println("Error: failed creating auth ticket");
            } else {
                System.out.println("Error: buffer too small for auth ticket, need " + sizeRequired[0] + " bytes");
            }
        }
    }

    private void cancelAuthTicket() {
        if (userAuthTicket != null && userAuthTicket.isValid()) {
            System.out.println("Auth ticket cancelled");
            user.cancelAuthTicket(userAuthTicket);
            userAuthTicket = null;
        }
    }

    private void beginAuthSession(SteamID steamIDSender) throws SteamException {
        endAuthSession();
        System.out.println("Starting auth session with user: " + steamIDSender.getAccountID());
        remoteAuthUser = steamIDSender;
        user.beginAuthSession(remoteAuthTicketData, remoteAuthUser);
    }

    private void endAuthSession() {
        if (remoteAuthUser != null) {
            System.out.println("End auth session with user: " + remoteAuthUser.getAccountID());
            user.endAuthSession(remoteAuthUser);
            remoteAuthUser = null;
        }
    }

    private void broadcastAuthTicket() throws SteamException {
        if (userAuthTicket == null || !userAuthTicket.isValid()) {
            System.out.println("Error: won't broadcast nil auth ticket");
            return;
        }

        for (Map.Entry<Integer, SteamID> remoteUser : remoteUserIDs.entrySet()) {

            System.out.println("Send auth to remote user: " + remoteUser.getKey() +
                    "[hash: " + userAuthTicketData.hashCode() + "]");

            packetSendBuffer.clear(); // pos=0, limit=cap

            packetSendBuffer.put(AUTH); // magic bytes
            packetSendBuffer.put(userAuthTicketData);

            userAuthTicketData.flip(); // limit=pos, pos=0
            packetSendBuffer.flip(); // limit=pos, pos=0

            networking.sendP2PPacket(remoteUser.getValue(), packetSendBuffer,
                    SteamNetworking.P2PSend.Reliable, defaultChannel);
        }
    }

    private int checkMagicBytes(ByteBuffer buffer, byte[] magicBytes) {
        for (int b = 0; b < magicBytes.length; b++) {
            if (buffer.get(b) != magicBytes[b]) {
                return 0;
            }
        }
        return magicBytes.length;
    }
}
