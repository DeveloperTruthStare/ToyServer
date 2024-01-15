package com.smith.toyserver.networking;

public class GameClient extends MySteamNetworkConnection implements Runnable {
    private Thread mainThread;
    private NetworkGameController controller;
    public GameClient(NetworkGameController networkGameController, Thread mainThread) {
        this.controller = networkGameController;
        this.mainThread = mainThread;
    }
    @Override
    public void run() {
        while(mainThread.isAlive()) {
            try {
                controller.processUpdates();
                Thread.sleep(1000 / 160);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
