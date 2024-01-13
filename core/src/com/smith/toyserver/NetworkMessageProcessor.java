package com.smith.toyserver;

public interface NetworkMessageProcessor {
    public void processNetworkMessage(String msg);
    public void processGameState(GameController state);
}
