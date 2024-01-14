package com.smith.toyserver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.smith.toyserver.screens.GameScreen;
import com.smith.toyserver.screens.TitleScreen;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class ToyServer extends Game {

	public GameManager manager;
	private GameServer server;
	public class GameServer implements Runnable {
		private Thread mainThread;
		public GameServer(Thread mainThread) {
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
					manager.update(0);
					processUpdates();
					synClients();
					Thread.sleep(1000 / 40);
				} catch (Exception e ) {
					System.err.println(e.getMessage());
				}
			}
		}
		public void synClients() {

		}
	}
	private GameClient client;
	public class GameClient implements Runnable {
		private Thread mainThread;
		public GameClient(Thread mainThread) {
			this.mainThread = mainThread;
		}
		@Override
		public void run() {
			while(mainThread.isAlive()) {
				try {
					processUpdates();
					manager.update(0);
					Thread.sleep(1000 / 40);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}

	private static final int defaultChannel = 1;
	private static final Charset messageCharset = StandardCharsets.UTF_8;
	private static final int readBufferCapacity = 4096;
	private static final int sendBufferCapacity = 4096;
	private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);
	private ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBufferCapacity);
	SteamID hostId;
	SteamID clientId;

	private InputProcessor currentScreen;
	public InputProcessor inputProcessor = new InputProcessor()  {

		@Override
		public boolean keyDown(int keycode) {
			if (keycode == Input.Keys.S)
				setPlayerVelocity(new Vector2(0, -10));
			else if (keycode == Input.Keys.W)
				setPlayerVelocity(new Vector2(0, 10));
			if (currentScreen != null)
				currentScreen.keyDown(keycode);
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Input.Keys.S || keycode == Input.Keys.W)
				setPlayerVelocity(new Vector2(0, 0));
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


	private SteamNetworking networking;
	private SteamNetworkingCallback peer2peerCallback = new SteamNetworkingCallback() {
		@Override
		public void onP2PSessionConnectFail(SteamID steamIDRemote, SteamNetworking.P2PSessionError sessionError) {
			System.out.println("P2P connection failed: userID=" + steamIDRemote.getAccountID() +
					", error: " + sessionError);

		}

		@Override
		public void onP2PSessionRequest(SteamID steamIDRemote) {
			System.out.println("P2P connection requested by userID " + steamIDRemote.getAccountID());
			networking.acceptP2PSessionWithUser(steamIDRemote);
		}
	};

	private SteamMatchmaking matchmaking;
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
					hostId = friend;
					startClient();
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
		}
		@Override
		public void onFavoritesListAccountsUpdated(SteamResult result) {

		}
	};

	private SteamFriends steamFriends;
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

	private SteamUser user;
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
	private GameScreen gameScreen;
	@Override
	public void create () {
		// Load SteamGameServerAPI
		networking = new SteamNetworking(peer2peerCallback);
		networking.allowP2PPacketRelay(true);

		matchmaking = new SteamMatchmaking(matchmakingCallback);
		steamFriends = new SteamFriends(friendsCallback);
		user = new SteamUser(userCallback);

		this.manager = new GameManager();

		batch = new SpriteBatch();
		titleScreen = new TitleScreen(this);
		setScreen(this.titleScreen);
		this.currentScreen = titleScreen;
		Gdx.input.setInputProcessor(inputProcessor);
	}

	public void startServer() {
		System.out.println("Starting Server");
		host = true;
		GameServer gameServer = new GameServer(Thread.currentThread());
		Thread gameServerThread = new Thread(gameServer);
		gameServerThread.start();
		matchmaking.createLobby(SteamMatchmaking.LobbyType.FriendsOnly, 2);

		openGameScreen();
	}
	public void startClient() {
		System.out.println("Starting Client");
		host = false;
		client = new GameClient(Thread.currentThread());
		Thread gameClientThread = new Thread(client);
		gameClientThread.start();

		openGameScreen();
	}
	private void openGameScreen() {
		gameScreen = new GameScreen();
		gameScreen.setGameState(manager.getGameState());
		setScreen(gameScreen);
		this.currentScreen = gameScreen;
	}

	public void setPlayerVelocity(Vector2 velocity) {
		if (host) {
			this.manager.setVelocity(1, velocity);
			// Send to clients
			sendTo(clientId, velocity);
		} else {
			this.manager.setVelocity(2, velocity);
			// Send to server
			sendTo(hostId, velocity);
		}
		gameScreen.setGameState(manager.getGameState());
	}

	// Networking Calls
	public void sendTo(SteamID dest, Vector2 velocity) {
		if (dest == null) return;
		String msg = "SetVelocity:" + velocity.x + "," + velocity.y;
		packetSendBuffer.clear(); // pos=0, limit=cap

		byte[] bytes = msg.getBytes();
		packetSendBuffer.put(bytes);

		packetSendBuffer.flip(); // limit=pos, pos=0
		try {
			networking.sendP2PPacket(dest, packetSendBuffer,
					SteamNetworking.P2PSend.Unreliable, defaultChannel);
		} catch (SteamException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Pack Sent " + messageCharset.decode(packetSendBuffer).toString());
	}
	public void processUpdates() throws SteamException {
		// Check if a packet has been recv
		int[] packetSize = new int[1];
		if (networking.isP2PPacketAvailable(defaultChannel, packetSize)) {
			System.out.println("Message to recv");
			SteamID steamIDSender = new SteamID();

			if (packetSize[0] > packetReadBuffer.capacity()) {
				throw new SteamException("incoming packet larger than read buffer can handle");
			}

			// Clear previous message
			packetReadBuffer.clear();

			int packetReadSize = networking.readP2PPacket(steamIDSender, packetReadBuffer, defaultChannel);
			if (host) clientId = steamIDSender;
			// Error checking
			if (packetReadSize == 0) {
				System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but got none from " + steamIDSender.getAccountID());
			} else if (packetReadSize < packetSize[0]) {
				System.err.println("Rcv packet: expected " + packetSize[0] + " bytes, but only got " + packetReadSize);
			}

			packetReadBuffer.limit(packetReadSize);

			if (packetReadSize > 0) {
				int bytesReceived = packetReadBuffer.limit();
				System.out.println("Rcv packet: userID=" + steamIDSender.getAccountID() + ", " + bytesReceived + " bytes");

				byte[] bytes = new byte[bytesReceived];
				packetReadBuffer.get(bytes);

				String message = new String(bytes, messageCharset);
				System.out.println("Rcv message: \"" + message + "\"");
				processMessage(message);
			}
		}
	}
	public boolean host = false;
	public void processMessage(String message) {
		if (message.startsWith("SetVelocity:")) {
			String[] parts = message.substring("SetVelocity:".length()).split(",");
			Vector2 velocity = new Vector2(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
			// Server has recv set velocity
			if (host) {
				this.manager.setVelocity(2, velocity);
			} else {
				this.manager.setVelocity(1, velocity);
			}
		}
	}



}