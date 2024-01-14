package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int defaultChannel = 1;
    private static final Charset messageCharset = StandardCharsets.UTF_8;

    private static final int readBufferCapacity = 4096;
    private static final int sendBufferCapacity = 4096;
    private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);
    private ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBufferCapacity);
    private Map<Integer, SteamID> remoteUserIDs = new ConcurrentHashMap<Integer, SteamID>();

    private SteamAuthTicket userAuthTicket;
    private ByteBuffer userAuthTicketData = ByteBuffer.allocateDirect(256);

    private SteamID remoteAuthUser;
    private ByteBuffer remoteAuthTicketData = ByteBuffer.allocateDirect(256);

    private final byte[] AUTH = "AUTH".getBytes(Charset.defaultCharset());

    private class GameServer implements  Runnable {
        private final Thread mainThread;
        public GameServer(Thread mainThread) {
            this.mainThread = mainThread;
        }
        @Override
        public void run() {
            while(mainThread.isAlive()) {
                SteamGameServerAPI.runCallbacks();

                try {
                    processUpdate();
                } catch (SteamException e) {
                    throw new RuntimeException(e);
                }

                try {
                    // run about 40 times a second
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private SteamNetworkingCallback peer2peerCallback = new SteamNetworkingCallback() {
        @Override
        public void onP2PSessionConnectFail(SteamID steamIDRemote, SteamNetworking.P2PSessionError sessionError) {
            System.out.println("P2P connection failed: userID=" + steamIDRemote.getAccountID() +
                    ", error: " + sessionError);
            unregisterRemoteSteamID(steamIDRemote);
        }

        @Override
        public void onP2PSessionRequest(SteamID steamIDRemote) {
            System.out.println("P2P connection requested by userID " + steamIDRemote.getAccountID());
            registerRemoteSteamID(steamIDRemote);
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
            matchmaking.setLobbyData(steamIDLobby, "hostSteamID", String.valueOf(user.getSteamID().getAccountID()));
        }
        @Override
        public void onFavoritesListAccountsUpdated(SteamResult result) {

        }
    };
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
    private SteamUser user;

    public Server() {

        // Load SteamGameServerAPI
        try {
            SteamGameServerAPI.loadLibraries();
            if (!SteamGameServerAPI.init((127 << 24) + 1, (short) 27016, (short) 27017,
                    SteamGameServerAPI.ServerMode.NoAuthentication, "0.0.1")) {
                System.out.println("SteamGameServerAPI.init() failed");
            }
        } catch (SteamException e) {
            throw new RuntimeException(e);
        }

        networking = new SteamNetworking(peer2peerCallback);
        networking.allowP2PPacketRelay(true);

        matchmaking = new SteamMatchmaking(matchmakingCallback);

        user = new SteamUser(userCallback);
    }

    public void start() {
        System.out.println("Starting Server");
        GameServer server = new GameServer(Thread.currentThread());
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Allows other people to join through steam interface
        matchmaking.createLobby(SteamMatchmaking.LobbyType.Public, 5);
    }

    public void processUpdate() throws SteamException {
        // Check if a packet has been recv
        int[] packetSize = new int[1];
        if (networking.isP2PPacketAvailable(defaultChannel, packetSize)) {

            SteamID steamIDSender = new SteamID();

            if (packetSize[0] > packetReadBuffer.capacity()) {
                throw new SteamException("incoming packet larger than read buffer can handle");
            }

            // Clear previous message
            packetReadBuffer.clear();

            int packetReadSize = networking.readP2PPacket(steamIDSender, packetReadBuffer, defaultChannel);
            System.out.println(packetReadSize);
            // Error checking
            if (packetReadSize == 0) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but got none from " + steamIDSender.getAccountID());
            } else if (packetReadSize < packetSize[0]) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but only got " + packetReadSize);
            }

            packetReadBuffer.limit(packetReadSize);

            if (packetReadSize > 0) {
                int bytesReceived = packetReadBuffer.limit();
                System.out.println("Rcv packet: userID=" + steamIDSender.getAccountID() + ", " + bytesReceived + " bytes");

                byte[] bytes = new byte[bytesReceived];
                packetReadBuffer.get(bytes);

                // check for magic bytes first
                int magicBytes = checkMagicBytes(packetReadBuffer, AUTH);
                if (magicBytes > 0) {
                    // extract ticket
                    remoteAuthTicketData.clear();
                    remoteAuthTicketData.put(bytes, magicBytes, bytesReceived - magicBytes);
                    remoteAuthTicketData.flip();
                    System.out.println("Auth ticket received: " + remoteAuthTicketData.toString() +
                            " [hash: " + remoteAuthTicketData.hashCode() + "]");
                    // auth
                    beginAuthSession(steamIDSender);
                } else {
                    // plain text message
                    String message = new String(bytes, messageCharset);
                    System.out.println("Rcv message: \"" + message + "\"");
                }

                String message = new String(bytes, messageCharset);
                System.out.println("Rcv message: \"" + message + "\"");
            }
        }
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
