package com.smith.toyserver;

public class SteamLobbyView {
    private final SteamLobby lobby;
    private String textToDisplay = "Not Connected";
    public SteamLobbyView(SteamLobby lobby) {
        this.lobby = lobby;
    }

    public void update() {
        if (lobby.isConnecting) {
            textToDisplay = "Connecting...";
        }
        if (lobby.isConnected) {
            if (lobby.isHosting) {
                textToDisplay = "Hosting";
            } else {
                textToDisplay = "Connected to " + lobby.getHostName() + "'s Game";
            }
        }

    }

    public String getTextToDisplay() {
        return textToDisplay;
    }
}
