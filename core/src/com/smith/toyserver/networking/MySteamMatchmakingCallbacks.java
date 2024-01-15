package com.smith.toyserver.networking;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamResult;

public class MySteamMatchmakingCallbacks implements SteamMatchmakingCallback {
    private NetworkGameController ngm;
    public MySteamMatchmakingCallbacks(NetworkGameController ngm) {
        this.ngm = ngm;
    }

    @Override
    public void onFavoritesListChanged(int ip, int queryPort, int connPort, int appID, int flags, boolean add, int accountID) {

    }
    @Override
    public void onLobbyInvite(SteamID steamIDUser, SteamID steamIDLobby, long gameID) {

    }
    @Override
    public void onLobbyEnter(SteamID steamIDLobby, int chatPermissions, boolean blocked, SteamMatchmaking.ChatRoomEnterResponse response) {
        String hostAccountId = ngm.matchmaking.getLobbyData(steamIDLobby, "hostAccountId");
        System.out.println(hostAccountId);
        try {
            int accountId = Integer.parseInt(ngm.matchmaking.getLobbyData(steamIDLobby, "hostAccountId"));
            int friendCount = ngm.steamFriends.getFriendCount(SteamFriends.FriendFlags.Immediate);

            for(int i = 0; i < friendCount; ++i) {
                SteamID friend = ngm.steamFriends.getFriendByIndex(i, SteamFriends.FriendFlags.Immediate);
                if (friend.getAccountID() == accountId) {
                    ngm.hostId = friend;
                    ngm.startClient();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public void onLobbyDataUpdate(SteamID steamIDLobby, SteamID steamIDMember, boolean success) {

    }
    @Override
    public void onLobbyChatUpdate(SteamID steamIDLobby, SteamID steamIDUserChanged, SteamID steamIDMakingChange, SteamMatchmaking.ChatMemberStateChange stateChange) {

    }
    @Override
    public void onLobbyChatMessage(SteamID steamIDLobby, SteamID steamIDUser, SteamMatchmaking.ChatEntryType entryType, int chatID) {

    }
    @Override
    public void onLobbyGameCreated(SteamID steamIDLobby, SteamID steamIDGameServer, int ip, short port) {

    }
    @Override
    public void onLobbyMatchList(int lobbiesMatching) {

    }
    @Override
    public void onLobbyKicked(SteamID steamIDLobby, SteamID steamIDAdmin, boolean kickedDueToDisconnect) {

    }
    @Override
    public void onLobbyCreated(SteamResult result, SteamID steamIDLobby) {
        ngm.matchmaking.setLobbyData(steamIDLobby, "hostAccountId", String.valueOf(ngm.user.getSteamID().getAccountID()));
    }
    @Override
    public void onFavoritesListAccountsUpdated(SteamResult result) {

    }
}
