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
					Thread.sleep(1000  / 15);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				SteamAPI.runCallbacks();
			}
		}
	}


	public static void main (String[] arg) {
		// Initialize Steam
		try {
			SteamAPI.loadLibraries();

			if (!SteamAPI.init()) {
				SteamAPI.printDebugInfo(System.err);
				System.out.println("Steam is likely not running");
				return;
			}
		} catch (SteamException e) {
			throw new RuntimeException(e);
		}

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