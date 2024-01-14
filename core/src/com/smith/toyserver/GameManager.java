package com.smith.toyserver;


import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

public class GameManager {

    private static final int defaultChannel = 1;
    private static final Charset messageCharset = StandardCharsets.UTF_8;
    private static final int readBufferCapacity = 4096;
    private static final int sendBufferCapacity = 4096;
    private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);
    private ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBufferCapacity);
    SteamID hostId;
    SteamID clientId;

    public class GameServer implements Runnable {
        private Thread mainThread;
        public GameServer(Thread mainThread) {
            this.mainThread = mainThread;
            try {
                SteamGameServerAPI.loadLibraries();
                if (!SteamGameServerAPI.init((127 << 24) + 1, (short) 27010, (short) 27011,
                        SteamGameServerAPI.ServerMode.NoAuthentication, "0.0.1")) {
                    System.out.println("SteamGameServerAPI.init() failed");
                }
            } catch (SteamException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void run() {
            while (mainThread.isAlive()) {
                SteamGameServerAPI.runCallbacks();

                try {
                    processUpdates();
                    synClients();
                    Thread.sleep(1000 / 40);
                } catch (Exception e ) {
                    System.err.println(e.getMessage());
                }
            }
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
                clientId = steamIDSender;

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
                    clientId = steamIDSender;

                    int bytesReceived = packetReadBuffer.limit();
                    System.out.println("Rcv packet: userID=" + steamIDSender.getAccountID() + ", " + bytesReceived + " bytes");

                    byte[] bytes = new byte[bytesReceived];
                    packetReadBuffer.get(bytes);

                    String message = new String(bytes, messageCharset);
                    System.out.println("Rcv message: \"" + message + "\"");
                    processMessage(message);
                }
            }
        }
        public void processMessage(String message) {
            if (message.startsWith("SetVelocity:")) {
                String[] parts = message.substring("SetVelocity:".length()).split(",");
                Vector2 velocity = new Vector2(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                // Server has recv set velocity
                gameState.player2.velocity = velocity;
            }
        }
        public void synClients() {

        }
        public void sendToClients(Vector2 velocity) {
            if (clientId == null) return;
            String msg = "SetVelocity:" + velocity.x + "," + velocity.y;
            packetSendBuffer.clear(); // pos=0, limit=cap

            byte[] bytes = msg.getBytes();
            packetSendBuffer.put(bytes);

            packetSendBuffer.flip(); // limit=pos, pos=0
            System.out.println("Pack Send " + messageCharset.decode(packetSendBuffer).toString());
            try {
                networking.sendP2PPacket(clientId, packetSendBuffer,
                        SteamNetworking.P2PSend.UnreliableNoDelay, defaultChannel);
            } catch (SteamException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public class GameClient implements Runnable {
        private Thread mainThread;
        public GameClient(Thread mainThread) {
            this.mainThread = mainThread;
        }
        @Override
        public void run() {
            while(mainThread.isAlive()) {
                try {
                    processUpdates();
                    Thread.sleep(1000 / 40);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
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
                    clientId = steamIDSender;

                    int bytesReceived = packetReadBuffer.limit();
                    System.out.println("Rcv packet: userID=" + steamIDSender.getAccountID() + ", " + bytesReceived + " bytes");

                    byte[] bytes = new byte[bytesReceived];
                    packetReadBuffer.get(bytes);

                    String message = new String(bytes, messageCharset);
                    System.out.println("Rcv message: \"" + message + "\"");
                    processMessage(message);
                }
            }
        }
        public void processMessage(String message) {
            if (message.startsWith("SetVelocity:")) {
                String[] parts = message.substring("SetVelocity:".length()).split(",");
                Vector2 velocity = new Vector2(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                // Server has recv set velocity
                gameState.player1.velocity = velocity;
            }

        }
        public void sendToServer(Vector2 velocity) {
            if (hostId == null) return;
            String msg = "SetVelocity:" + velocity.x + "," + velocity.y;
            packetSendBuffer.clear(); // pos=0, limit=cap

            byte[] bytes = msg.getBytes();
            packetSendBuffer.put(bytes);

            packetSendBuffer.flip(); // limit=pos, pos=0
            System.out.println("Pack Send " + messageCharset.decode(packetSendBuffer).toString());
            try {
                networking.sendP2PPacket(hostId, packetSendBuffer,
                        SteamNetworking.P2PSend.UnreliableNoDelay, defaultChannel);
            } catch (SteamException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public static class GameState {
        public GameObject player1;
        public GameObject player2;
        public GameObject ball;
        public boolean gameStarted;
        public GameState() {
            player1 = new GameObject();
            player2 = new GameObject();
            ball = new GameObject();

            player1.position = new Vector2(50, 490);
            player1.size = new Vector2(20, 100);

            player2.position = new Vector2(1870, 490);
            player2.size = new Vector2(20, 100);

            ball.position = new Vector2(1920/2, 540);
            ball.size = new Vector2(25, 25);
            ball.velocity = new Vector2(5, 0);

            gameStarted = false;
        }
    }
    private final boolean host;
    private GameState gameState;
    private GameServer server;
    private GameClient client;
    public SteamNetworking networking;

    public GameManager(boolean isHost, SteamNetworking networking) {
        this.host = isHost;
        this.networking = networking;

        if (this.host) {
            // Start Steam Server Stuff
            server = new GameServer(Thread.currentThread());
            Thread serverThread = new Thread(server);
            serverThread.start();
        } else {
            // Start Client Stuff
            client = new GameClient(Thread.currentThread());
            Thread clientThread = new Thread(client);
            clientThread.start();
        }

        // Initialize Game State
        gameState = new GameState();
    }
    public void update(float dt) {

    }
    public GameState getGameState() {
        return this.gameState;
    }
    public void setPlayerVelocity(Vector2 velocity) {
        if (host) {
            this.gameState.player1.velocity = velocity;
            // Send to clients
            this.server.sendToClients(velocity);
        } else {
            this.gameState.player2.velocity = velocity;
            // Send to server
            this.client.sendToServer(velocity);
        }
    }
    public void tick(float dt) {
        if (gameState.gameStarted) {
            gameState.ball.update(dt);
        }
        gameState.player1.update(dt);
        gameState.player2.update(dt);

        if (gameState.ball.contains(gameState.player1)) {
            gameState.ball.velocity = new Vector2(5, 0);
        } else if (gameState.ball.contains(gameState.player2)) {
            gameState.ball.velocity = new Vector2(-5, 0);
        }
    }
    public void setHostId(SteamID host) {
        this.hostId = host;
    }

    public void updateGameStateFromServer(GameState serverState) {
        // Update each value in the game state if it is different enough from our local value
        gameState.player1.syncWith(serverState.player1);
        gameState.player2.syncWith(serverState.player2);
        gameState.ball.syncWith(serverState.ball);


    }
}
