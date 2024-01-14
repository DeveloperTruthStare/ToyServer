package com.smith.toyserver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.smith.toyserver.*;
import com.smith.toyserver.networking.GameClient;
import com.smith.toyserver.screens.GameScreen;
import com.smith.toyserver.screens.TitleScreen;

public class ToyServer extends Game {
	public InputProcessor inputProcessor = new InputProcessor()  {

		@Override
		public boolean keyDown(int keycode) {
			if (currentScreen != null)
				currentScreen.keyDown(keycode);
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			if (currentScreen != null)
				currentScreen.keyUp(keycode);
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			if (currentScreen != null)
				currentScreen.keyTyped(character);
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			if (currentScreen != null)
				currentScreen.touchDown(screenX, screenY, pointer, button);
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			if (currentScreen != null)
				currentScreen.touchUp(screenX, screenY, pointer, button);
			return false;
		}

		@Override
		public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
			if (currentScreen != null)
				currentScreen.touchCancelled(screenX, screenY, pointer, button);
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			if (currentScreen != null)
				currentScreen.touchDragged(screenX, screenY, pointer);
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			if (currentScreen != null)
				currentScreen.mouseMoved(screenX, screenY);
			return false;
		}

		@Override
		public boolean scrolled(float amountX, float amountY) {
			if (currentScreen != null)
				currentScreen.scrolled(amountX, amountY);
			return false;
		}
	};
	public SteamMatchmakingCallback matchmakingCallback = new SteamMatchmakingCallback() {
		@Override
		public void onFavoritesListChanged(int ip, int queryPort, int connPort, int appID, int flags, boolean add, int accountID) {

		}
		@Override
		public void onLobbyInvite(SteamID steamIDUser, SteamID steamIDLobby, long gameID) {

		}
		@Override
		public void onLobbyEnter(SteamID steamIDLobby, int chatPermissions, boolean blocked, SteamMatchmaking.ChatRoomEnterResponse response) {
			int accountId = Integer.parseInt(matchmaking.getLobbyData(steamIDLobby, "hostAccountId"));
			int friendCount = steamFriends.getFriendCount(SteamFriends.FriendFlags.Immediate);

			for(int i = 0; i < friendCount; ++i) {
				SteamID friend = steamFriends.getFriendByIndex(i, SteamFriends.FriendFlags.Immediate);
				if (friend.getAccountID() == accountId) {
					gameManager = new GameManager(false);
					openGameScreen(new GameScreen(gameManager));
					gameManager.setHostId(friend);
				}
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
			matchmaking.setLobbyData(steamIDLobby, "hostAccountId", String.valueOf(user.getSteamID().getAccountID()));
			gameManager = new GameManager(false);
			openGameScreen(new GameScreen(gameManager));
		}
		@Override
		public void onFavoritesListAccountsUpdated(SteamResult result) {

		}
	};
	public SteamFriendsCallback friendsCallback = new SteamFriendsCallback() {
		@Override
		public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result) {

		}

		@Override
		public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change) {

		}

		@Override
		public void onGameOverlayActivated(boolean active) {

		}

		@Override
		public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend) {
			matchmaking.joinLobby(steamIDLobby);
		}

		@Override
		public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height) {

		}

		@Override
		public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID) {

		}

		@Override
		public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect) {

		}

		@Override
		public void onGameServerChangeRequested(String server, String password) {

		}
	};
	public SteamUserCallback userCallback = new SteamUserCallback() {
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
	};
	public SpriteBatch batch;
	private TitleScreen titleScreen;
	private InputProcessor currentScreen;
	private SteamMatchmaking matchmaking;
	private SteamFriends steamFriends;
	private SteamUser user;
	public GameManager gameManager;
	@Override
	public void create () {
		matchmaking = new SteamMatchmaking(matchmakingCallback);
		steamFriends = new SteamFriends(friendsCallback);
		user = new SteamUser(userCallback);
		batch = new SpriteBatch();
		titleScreen = new TitleScreen(this);
		setScreen(this.titleScreen);
		this.currentScreen = titleScreen;
		Gdx.input.setInputProcessor(inputProcessor);

	}
	public void startServer() {
		System.out.println("Starting Server");
		matchmaking.createLobby(SteamMatchmaking.LobbyType.FriendsOnly, 2);
		gameManager = new GameManager(true);
		openGameScreen(new GameScreen(gameManager));
	}
	private void openGameScreen(GameScreen gameScreen) {
		setScreen(gameScreen);
		this.currentScreen = gameScreen;
	}
}
