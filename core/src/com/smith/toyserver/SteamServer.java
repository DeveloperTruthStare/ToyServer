package com.smith.toyserver;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServer;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamGameServerCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamResult;

public class SteamServer implements SteamGameServerCallback {
    private SteamGameServer gameServer;
    public SteamServer() {
        try {
            initSteamGameServer();
        } catch (SteamException e) {
            System.out.println("initSteamGameServer error");
        }
    }
    SteamLobby lobby;
    private void initSteamGameServer() throws SteamException {
        SteamGameServerAPI.loadLibraries();
        if (!SteamGameServerAPI.init((127 << 24) + 1, (short) 27016, (short) 27017,
                SteamGameServerAPI.ServerMode.NoAuthentication, "0.0.1")) {
            System.out.println("SteamGameServerAPI init error");
        }
        gameServer = new SteamGameServer(this);
        gameServer.setProduct("ToyServer");
        gameServer.setGameDescription("Toy Server");
        gameServer.logOnAnonymous();

    }
    @Override
    public void onValidateAuthTicketResponse(SteamID steamID, SteamAuth.AuthSessionResponse authSessionResponse, SteamID ownerSteamID) {

    }

    @Override
    public void onSteamServersConnected() {

    }

    @Override
    public void onSteamServerConnectFailure(SteamResult result, boolean stillRetrying) {

    }

    @Override
    public void onSteamServersDisconnected(SteamResult result) {

    }

    @Override
    public void onClientApprove(SteamID steamID, SteamID ownerSteamID) {

    }

    @Override
    public void onClientDeny(SteamID steamID, SteamGameServer.DenyReason denyReason, String optionalText) {

    }

    @Override
    public void onClientKick(SteamID steamID, SteamGameServer.DenyReason denyReason) {

    }

    @Override
    public void onClientGroupStatus(SteamID steamID, SteamID steamIDGroup, boolean isMember, boolean isOfficer) {

    }

    @Override
    public void onAssociateWithClanResult(SteamResult result) {

    }

    @Override
    public void onComputeNewPlayerCompatibilityResult(SteamResult result, int playersThatDontLikeCandidate, int playersThatCandidateDoesntLike, int clanPlayersThatDontLikeCandidate, SteamID steamIDCandidate) {

    }

    public SteamLobby getLobby() {
        return this.lobby;
    }
}
