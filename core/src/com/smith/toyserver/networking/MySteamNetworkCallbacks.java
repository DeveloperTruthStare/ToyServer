package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;

public class MySteamNetworkCallbacks implements SteamNetworkingCallback {
    private NetworkGameController ngm;
    public MySteamNetworkCallbacks(NetworkGameController ngm) {
        this.ngm = ngm;
    }
    @Override
    public void onP2PSessionConnectFail(SteamID steamIDRemote, SteamNetworking.P2PSessionError sessionError) {
        System.out.println("P2P connection failed: userID=" + steamIDRemote.getAccountID() +
                ", error: " + sessionError);
    }
    @Override
    public void onP2PSessionRequest(SteamID steamIDRemote) {
        System.out.println("P2P connection requested by userID " + steamIDRemote.getAccountID());
        ngm.networking.acceptP2PSessionWithUser(steamIDRemote);
    }
}
