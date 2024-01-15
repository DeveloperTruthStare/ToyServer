package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smith.toyserver.GameState;

public class GameServer extends MySteamNetworkConnection implements Runnable {
    private Thread mainThread;
    public SteamID clientId;
    private NetworkGameController networkGameController;
    public GameServer(Thread mainThread, NetworkGameController gameController) {
        this.networkGameController = gameController;
        this.mainThread = mainThread;
        try {
            SteamGameServerAPI.loadLibraries();
            if (!SteamGameServerAPI.init((127 << 24) + 1, (short) 27016, (short) 27017,
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
                networkGameController.processUpdates();
                networkGameController.syncClients();
                Thread.sleep(1000 / 40);
            } catch (Exception e ) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void syncClients(GameState gameState) {
        try {
            String syncMsg = "SYNC:" + new ObjectMapper().writeValueAsString(gameState);
            networkGameController.sendMsg(clientId, syncMsg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
