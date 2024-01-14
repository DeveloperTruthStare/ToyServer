package com.smith.toyserver.networking;

public class GameClient implements Runnable {

    private Thread mainThread;
    public GameClient(Thread mainThread) {
        this.mainThread = mainThread;
    }
    @Override
    public void run() {

    }
}
