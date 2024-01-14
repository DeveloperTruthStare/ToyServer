package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamGameServerNetworking;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamMatchmakingServers;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.smith.toyserver.GameManager;

import java.awt.SystemColor;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int defaultChannel = 1;
    private static final Charset messageCharset = StandardCharsets.UTF_8;

    private static final int readBufferCapacity = 4096;
    private static final int sendBufferCapacity = 4096;

    private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);
    private ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBufferCapacity);



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

  /*              try {
                    // run about 40 times a second
//                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/
            }
        }
    }
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
            try {
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
            } catch (SteamException e) {
                e.printStackTrace();
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
            currentLobby = steamIDLobby;

            // get host steamID
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
            currentLobby = steamIDLobby;
            matchmaking.setLobbyData(steamIDLobby, "hostSteamID", String.valueOf(user.getSteamID().getAccountID()));
        }
        @Override
        public void onFavoritesListAccountsUpdated(SteamResult result) {

        }
    };
    private SteamFriendsCallback steamFriendsCallback = new SteamFriendsCallback() {
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
    private Map<Integer, SteamID> remoteUserIDs = new ConcurrentHashMap<Integer, SteamID>();
    private SteamID currentLobby = null;
    private SteamUser user;
    private SteamID hostSteamID;

    private SteamAuthTicket userAuthTicket;
    private ByteBuffer userAuthTicketData = ByteBuffer.allocateDirect(256);

    private SteamID remoteAuthUser;
    private ByteBuffer remoteAuthTicketData = ByteBuffer.allocateDirect(256);

    private final byte[] AUTH = "AUTH".getBytes(Charset.defaultCharset());



    private GameManager gameManager;
    public Server(GameManager gameManager) {
        this.gameManager = gameManager;

        // Load SteamGameServerAPI
        try {
            SteamGameServerAPI.loadLibraries();
            if (!SteamGameServerAPI.init((127 << 24) + 1, (short) 27010, (short) 27011,
                    SteamGameServerAPI.ServerMode.NoAuthentication, "0.0.1")) {
                System.out.println("SteamGameServerAPI.init() failed");
            }
        } catch (SteamException e) {
            throw new RuntimeException(e);
        }

        networking = new SteamNetworking(peer2peerCallback);
        networking.allowP2PPacketRelay(true);

        matchmaking = new SteamMatchmaking(matchmakingCallback);
        steamFriends = new SteamFriends(steamFriendsCallback);

        user = new SteamUser(userCallback);
        SteamID localUser = user.getSteamID();
        System.out.println("Local User: " + localUser.getAccountID());
    }

    public void start() {
        InputHandler inputHandler = new InputHandler(Thread.currentThread());
        Thread inputThread = new Thread(inputHandler);
        inputThread.start();


        GameServer server = new GameServer(Thread.currentThread());
        Thread serverThread = new Thread(server);
        serverThread.start();

        // ALlows other people to join through steam interface
        matchmaking.createLobby(SteamMatchmaking.LobbyType.FriendsOnly, 2);
    }

    public void processInput(String input) throws SteamException {
        System.out.println(input);
        if (input.startsWith("test")) {

            packetSendBuffer.clear(); // pos=0, limit=cap

            String msg = "Hello World";
            byte[] bytes = msg.getBytes();
            packetSendBuffer.put(bytes);

            packetSendBuffer.flip(); // limit=pos, pos=0
            System.out.println("Pack Send " + messageCharset.decode(packetSendBuffer).toString());
            try {
                networking.sendP2PPacket(hostSteamID, packetSendBuffer,
                        SteamNetworking.P2PSend.UnreliableNoDelay, defaultChannel);
            } catch (SteamException e) {
                throw new RuntimeException(e);
            }

        }
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

            // Error checking
            if (packetReadSize == 0) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but got none from " + steamIDSender.getAccountID());
            } else if (packetReadSize < packetSize[0]) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but only got " + packetReadSize);
            }

            packetReadBuffer.limit(packetReadSize);

            if (packetReadSize > 0) {
                // We have recv a packet

                // Register the sender if unknown
                registerRemoteSteamID(steamIDSender);

                int bytesReceived = packetReadBuffer.limit();
                System.out.println("Rcv packet: userID=" + steamIDSender.getAccountID() + ", " + bytesReceived + " bytes");

                byte[] bytes = new byte[bytesReceived];
                packetReadBuffer.get(bytes);

                String message = new String(bytes, messageCharset);
                System.out.println("Rcv message: \"" + message + "\"");
            }

        }

        // Server Update method here I think
        gameManager.update(0);

    }


    private void registerRemoteSteamID(SteamID steamIDUser) {
        if (!remoteUserIDs.containsKey(steamIDUser.getAccountID())) {
            remoteUserIDs.put(steamIDUser.getAccountID(), steamIDUser);
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
    private void endAuthSession() {
        if (remoteAuthUser != null) {
            System.out.println("End auth session with user: " + remoteAuthUser.getAccountID());
            user.endAuthSession(remoteAuthUser);
            remoteAuthUser = null;
        }
    }
}
