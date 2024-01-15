package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smith.toyserver.GameManager;
import com.smith.toyserver.GameState;
import com.smith.toyserver.ToyServer;
import com.smith.toyserver.Vector2;
import com.smith.toyserver.screens.GameScreen;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// Controller
public class NetworkGameController {
    // Model
    private GameManager gameModel;

    private static final int defaultChannel = 1;
    private static final Charset messageCharset = StandardCharsets.UTF_8;
    private static final int readBufferCapacity = 4096;
    private static final int sendBufferCapacity = 4096;
    private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);
    private ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBufferCapacity);





    private MySteamNetworkCallbacks networkCallbacks;
    public SteamNetworking networking;
    private MySteamMatchmakingCallbacks matchmakingCallbacks;
    public SteamMatchmaking matchmaking;
    private MySteamFriendsCallbacks steamFriendsCallbacks;
    public SteamFriends steamFriends;
    private MySteamUserCallbacks steamUserCallbacks;
    public SteamUser user;

    private ToyServer application;
    public SteamID hostId;
    public SteamID clientId;
    public NetworkGameController(ToyServer app, GameManager gameManager) {
        this.application = app;
        this.gameModel = gameManager;
        this.networkCallbacks = new MySteamNetworkCallbacks(this);
        networking = new SteamNetworking(networkCallbacks);
        networking.allowP2PPacketRelay(true);

        this.matchmakingCallbacks = new MySteamMatchmakingCallbacks(this);
        matchmaking = new SteamMatchmaking(matchmakingCallbacks);

        this.steamFriendsCallbacks = new MySteamFriendsCallbacks(this);
        steamFriends = new SteamFriends(steamFriendsCallbacks);

        steamUserCallbacks = new MySteamUserCallbacks(this);
        user = new SteamUser(steamUserCallbacks);
    }
    private boolean host = false;
    private GameServer gameServer;
    private GameClient client;
    public void startServer() {
        host = true;
        gameServer = new GameServer(Thread.currentThread(), this);
        Thread gameServerThread = new Thread(gameServer);
        gameServerThread.start();
        matchmaking.createLobby(SteamMatchmaking.LobbyType.FriendsOnly, 2);
    }
    public void startClient() {
        host = false;
        client = new GameClient(this, Thread.currentThread());
        Thread gameClientThread = new Thread(client);
        gameClientThread.start();
        application.startClient();
    }
    public void setPlayerVelocity(Vector2 velocity) {
        if (host) {
            this.gameModel.setVelocity(1, velocity);
            // Send to clients
            sendTo(clientId, velocity);
        } else {
            this.gameModel.setVelocity(2, velocity);
            // Send to server
            sendTo(hostId, velocity);
        }
    }
    public void sendMsg(SteamID dest, String msg) {
        if (dest == null) return;
        packetSendBuffer.clear(); // pos=0, limit=cap

        byte[] bytes = msg.getBytes();
        packetSendBuffer.put(bytes);

        packetSendBuffer.flip(); // limit=pos, pos=0
        try {
            networking.sendP2PPacket(dest, packetSendBuffer,
                    SteamNetworking.P2PSend.UnreliableNoDelay, defaultChannel);
        } catch (SteamException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendTo(SteamID dest, Vector2 velocity) {
        String msg = "SetVelocity:" + velocity.x + "," + velocity.y;
        sendMsg(dest, msg);
    }
    public GameState getGameState() {
        return this.gameModel.getGameState();
    }
    public void syncClients() {
        gameServer.syncClients(gameModel.getGameState());
    }
    public void processUpdates() throws SteamException {
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
            if (host) clientId = steamIDSender;
            // Error checking
            if (packetReadSize == 0) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but got none from " + steamIDSender.getAccountID());
            } else if (packetReadSize < packetSize[0]) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but only got " + packetReadSize);
            }

            packetReadBuffer.limit(packetReadSize);

            if (packetReadSize > 0) {
                int bytesReceived = packetReadBuffer.limit();

                byte[] bytes = new byte[bytesReceived];
                packetReadBuffer.get(bytes);

                String message = new String(bytes, messageCharset);
                processMessage(message);
            }
        }
    }
    public void processMessage(String message) {
        if (message.startsWith("SetVelocity:")) {
            String[] parts = message.substring("SetVelocity:".length()).split(",");
            Vector2 velocity = new Vector2(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
            // Server has recv set velocity
            if (host) {
                gameModel.setVelocity(2, velocity);
            } else {
                this.gameModel.setVelocity(1, velocity);
            }
        } else if(message.startsWith("SYNC:")) {
            message = message.substring("SYNC:".length());
            try {
                GameState gameState = new ObjectMapper().readValue(message, GameState.class);
                gameModel.sync(gameState);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
