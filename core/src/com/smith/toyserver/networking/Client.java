package com.smith.toyserver.networking;

import com.badlogic.gdx.Game;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import com.codedisaster.steamworks.SteamResult;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final int defaultChannel = 1;
    private static final Charset messageCharset = StandardCharsets.UTF_8;

    private static final int sendBufferCapacity = 4096;

    private ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBufferCapacity);




    public class GameClient implements Runnable {
        private Thread mainThread;
        public GameClient(Thread mainThread) {
            this.mainThread = mainThread;
        }
        @Override
        public void run() {
            while(mainThread.isAlive()) {
                try {
                    // Recv Data from server here
                    // ...


                    Thread.sleep(1000 / 40);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
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





    private SteamNetworking networking;
    private SteamMatchmaking matchmaking;
    private SteamFriends steamFriends;


    private SteamID hostSteamID;
    public Client() {
        networking = new SteamNetworking(peer2peerCallback);
        matchmaking = new SteamMatchmaking(matchmakingCallback);
        steamFriends = new SteamFriends(friendsCallback);
    }
    public void start() {
        Client.InputHandler inputHandler = new Client.InputHandler(Thread.currentThread());
        Thread inputThread = new Thread(inputHandler);
        inputThread.start();

        GameClient client = new GameClient(Thread.currentThread());
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
    public void processInput(String input) {
        if (hostSteamID == null) return;
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
