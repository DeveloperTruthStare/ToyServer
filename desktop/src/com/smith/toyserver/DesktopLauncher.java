package com.smith.toyserver;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPIWarningMessageHook;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;
import com.codedisaster.steamworks.SteamUtils;
import com.codedisaster.steamworks.SteamUtilsCallback;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static class SteamThread implements Runnable {
		public Thread mainThread;
		public SteamThread(Thread mainThread) {
			this.mainThread = mainThread;
		}
		@Override
		public void run() {
			while (mainThread.isAlive()) {
				try {
					Thread.sleep(1000  / 60);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				SteamAPI.runCallbacks();
			}
		}
	}
	private static final SteamAPIWarningMessageHook clMessageHook = new SteamAPIWarningMessageHook() {
		@Override
		public void onWarningMessage(int severity, String message) {
			System.err.println("[client debug message] (" + severity + ") " + message);
		}
	};
	private static final SteamUtilsCallback clUtilsCallback = new SteamUtilsCallback() {
		@Override
		public void onSteamShutdown() {
			System.err.println("Steam client requested to shut down!");
		}
	};
	public static boolean isServer = true;

	public static SteamUtils clientUtils;

	public static void main (String[] arg) {
		// Initialize Steam
		try {
			SteamGameServerAPI.loadLibraries();
			SteamAPI.loadLibraries();

			if (!SteamAPI.init()) {
				SteamAPI.printDebugInfo(System.err);
				System.out.println("Steam is likely not running");
				return;
			}
		} catch (SteamException e) {
			throw new RuntimeException(e);
		}
		clientUtils = new SteamUtils(clUtilsCallback);
		clientUtils.setWarningMessageHook(clMessageHook);

		// Start Steam callbacks
		SteamThread steam = new SteamThread(Thread.currentThread());
		Thread steamThread = new Thread(steam);
		steamThread.start();


		// Initialize Application
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Toy Server");
		config.setWindowedMode(1920, 1080);
		new Lwjgl3Application(new ToyServer(), config);
	}
}