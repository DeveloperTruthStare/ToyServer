package com.smith.toyserver;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int defaultChannel = 1;
    private static final Charset messageCharset = Charset.forName("UTF-8");
    private static final int readBufferCapacity = 4096;
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
                    Thread.sleep(32);
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

            unregisterRemoteSteamID(steamIDRemote);
        }

        @Override
        public void onP2PSessionRequest(SteamID steamIDRemote) {
            System.out.println("P2P connection requested by userID " + steamIDRemote.getAccountID());
            registerRemoteSteamID(steamIDRemote);
            networking.acceptP2PSessionWithUser(steamIDRemote);
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


    public SteamUser user;
    private Map<Integer, SteamID> remoteUserIDs = new ConcurrentHashMap<Integer, SteamID>();

    public FriendsMixin friends;
    private SteamNetworking networking;

    private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);
    private SteamAuthTicket userAuthTicket;
    private ByteBuffer userAuthTicketData = ByteBuffer.allocateDirect(256);
    private SteamID remoteAuthUser;
    private ByteBuffer remoteAuthTicketData = ByteBuffer.allocateDirect(256);
    private final byte[] AUTH = "AUTH".getBytes(Charset.defaultCharset());

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

        registerInterfaces();

    }

    public void start() {
        InputHandler inputHandler = new InputHandler(Thread.currentThread());
        Thread inputThread = new Thread(inputHandler);
        inputThread.start();


        GameServer server = new GameServer(Thread.currentThread());
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    public void processInput(String input) throws SteamException {
        System.out.println(input);
    }
    public void processUpdate() throws SteamException {
        // Server Update method here I think

        int[] packetSize = new int[1];
        if (networking.isP2PPacketAvailable(defaultChannel, packetSize)) {

            SteamID steamIDSender = new SteamID();

            if (packetSize[0] > packetReadBuffer.capacity()) {
                throw new SteamException("incoming packet larger than read buffer can handle");
            }

            packetReadBuffer.clear();
            // this isn't needed actually, buffer passed in can be larger than message to read
            packetReadBuffer.limit(packetSize[0]);

            int packetReadSize = networking.readP2PPacket(steamIDSender, packetReadBuffer, defaultChannel);

            if (packetReadSize == 0) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but got none");
            } else if (packetReadSize < packetSize[0]) {
                System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but only got " + packetReadSize);
            }

            // limit to actual data received
            packetReadBuffer.limit(packetReadSize);

            if (packetReadSize > 0) {

                // register, if unknown
                registerRemoteSteamID(steamIDSender);

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
    protected void registerInterfaces() {
        user = new SteamUser(userCallback);

        friends = new FriendsMixin();
        networking = new SteamNetworking(peer2peerCallback);

        networking.allowP2PPacketRelay(true);
    }

    protected void unregisterInterfaces() {
        cancelAuthTicket();
        user.dispose();
        friends.dispose();
        networking.dispose();
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
    private int checkMagicBytes(ByteBuffer buffer, byte[] magicBytes) {
        for (int b = 0; b < magicBytes.length; b++) {
            if (buffer.get(b) != magicBytes[b]) {
                return 0;
            }
        }
        return magicBytes.length;
    }
}
