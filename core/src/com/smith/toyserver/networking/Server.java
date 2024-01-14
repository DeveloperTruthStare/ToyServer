package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
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
    private static final int defaultChannel = 0;
    private static final Charset messageCharset = StandardCharsets.UTF_8;
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
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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
            System.out.println("On Lobby Enter");
            System.out.println(steamIDLobby);
            System.out.println(chatPermissions);
            System.out.println(blocked);
            System.out.println(response);

        }
        @Override
        public void onLobbyDataUpdate(SteamID steamIDLobby, SteamID steamIDMember, boolean success) {
            // Called when lobby created
            System.out.println("Lobby Data Update");
            System.out.println(steamIDLobby);
            System.out.println(steamIDMember);
            System.out.println(success);
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
            System.out.println("Lobby Created");
            System.out.println(result);
            System.out.println(steamIDLobby);
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
            /*
            System.out.println("Persona State Change");
            System.out.println(steamID);
            System.out.println(change);
            */
        }

        @Override
        public void onGameOverlayActivated(boolean active) {

        }

        @Override
        public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend) {
            System.out.println("Game Lobby Join Requested");
            System.out.println(steamIDLobby);
            System.out.println(steamIDFriend);
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
    private SteamNetworking networking;
    private SteamMatchmaking matchmaking;
    private SteamFriends steamFriends;
    private Map<Integer, SteamID> remoteUserIDs = new ConcurrentHashMap<Integer, SteamID>();

    private GameManager gameManager;
    public Server(GameManager gameManager) {
        this.gameManager = gameManager;

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
        matchmaking = new SteamMatchmaking(matchmakingCallback);
        steamFriends = new SteamFriends(steamFriendsCallback);
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
    }
    public void processUpdate() throws SteamException {
        // Check if a packet has been recv
        int[] packetSize = new int[1];
        if (networking.isP2PPacketAvailable(defaultChannel, packetSize)) {
            SteamID steamIDSender = new SteamID();

            int readBufferCapacity = 4096;
            ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);

            if (packetSize[0] > packetReadBuffer.capacity()) {
                throw new SteamException("incoming packet larger than read buffer can handle");
            }

            // Clear previous message
            packetReadBuffer.clear();
            // this isn't needed actually, buffer passed in can be larger than message to read
            packetReadBuffer.limit(packetSize[0]);

            int packetReadSize = networking.readP2PPacket(steamIDSender, packetReadBuffer, defaultChannel);

            // Error checking
            if (packetReadSize == 0) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but got none");
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

}