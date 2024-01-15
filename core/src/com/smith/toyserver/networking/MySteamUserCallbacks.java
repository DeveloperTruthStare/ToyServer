package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUserCallback;

public class MySteamUserCallbacks implements SteamUserCallback {
    private NetworkGameController ngm;
    public MySteamUserCallbacks(NetworkGameController ngm) {
        this.ngm = ngm;
    }
    @Override
    public void onAuthSessionTicket(SteamAuthTicket authTicket, SteamResult result) {

    }

    @Override
    public void onValidateAuthTicket(SteamID steamID, SteamAuth.AuthSessionResponse authSessionResponse, SteamID ownerSteamID) {

    }

    @Override
    public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {

    }

    @Override
    public void onEncryptedAppTicket(SteamResult result) {

    }
}
