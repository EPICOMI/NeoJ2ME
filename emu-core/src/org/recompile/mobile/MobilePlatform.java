/*
	This file is part of FreeJ2ME.

	FreeJ2ME is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	FreeJ2ME is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with FreeJ2ME.  If not, see http://www.gnu.org/licenses/
*/
package org.recompile.mobile;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.Image;

import java.awt.image.BufferedImage;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.POV;

/*
	Mobile Platform
*/

public class MobilePlatform
{

	// JInput specific members
	private List<Controller> gamepads;
	private volatile boolean gamepadPollingActive = false;
	private Thread gamepadPollingThread;
	// To store previous state of POV and axes for correct key release events
	private Map<String, Float> previousPovValues; // Key: controllerName + componentId, Value: last POV value
	private Map<String, Float> previousAxisValues; // Key: controllerName + componentId, Value: last axis value
	private Map<Integer, Boolean> customMappedActionStates; // Key: j2meCanvasKeyCode, Value: isPressed


	private static final Map<String, Integer> DEFAULT_BUTTON_MAP = new HashMap<>();
	private static final Map<String, Integer> J2ME_ACTION_TO_KEYCODE_MAP = new HashMap<>();

	static {
		// Example mapping, component names ("0", "1", etc.) might vary by gamepad
		DEFAULT_BUTTON_MAP.put("0", Canvas.FIRE); // Typically 'A' or 'X' (cross)
		DEFAULT_BUTTON_MAP.put("1", Canvas.GAME_A); // Typically 'B' or 'O' (circle)
		DEFAULT_BUTTON_MAP.put("2", Canvas.GAME_B); // Typically 'X' or 'Square'
		DEFAULT_BUTTON_MAP.put("3", Canvas.GAME_C); // Typically 'Y' or 'Triangle'
		DEFAULT_BUTTON_MAP.put("4", Canvas.KEY_SOFT_LEFT);  // L1 or LB
		DEFAULT_BUTTON_MAP.put("5", Canvas.KEY_SOFT_RIGHT); // R1 or RB
		DEFAULT_BUTTON_MAP.put("6", Canvas.KEY_STAR);    // Select or Back
		DEFAULT_BUTTON_MAP.put("7", Canvas.KEY_POUND);   // Start
		// Numerical keys for typical phone layouts, can be mapped to other gamepad buttons if desired
		DEFAULT_BUTTON_MAP.put("8", Canvas.KEY_NUM1); // Example: L2/LT
		DEFAULT_BUTTON_MAP.put("9", Canvas.KEY_NUM3); // Example: R2/RT

		// Initialize J2ME_ACTION_TO_KEYCODE_MAP
		J2ME_ACTION_TO_KEYCODE_MAP.put("UP", Canvas.UP);
		J2ME_ACTION_TO_KEYCODE_MAP.put("DOWN", Canvas.DOWN);
		J2ME_ACTION_TO_KEYCODE_MAP.put("LEFT", Canvas.LEFT);
		J2ME_ACTION_TO_KEYCODE_MAP.put("RIGHT", Canvas.RIGHT);
		J2ME_ACTION_TO_KEYCODE_MAP.put("FIRE", Canvas.FIRE);
		J2ME_ACTION_TO_KEYCODE_MAP.put("GAME_A", Canvas.GAME_A);
		J2ME_ACTION_TO_KEYCODE_MAP.put("GAME_B", Canvas.GAME_B);
		J2ME_ACTION_TO_KEYCODE_MAP.put("GAME_C", Canvas.GAME_C);
		J2ME_ACTION_TO_KEYCODE_MAP.put("GAME_D", Canvas.GAME_D);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM0", Canvas.KEY_NUM0);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM1", Canvas.KEY_NUM1);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM2", Canvas.KEY_NUM2);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM3", Canvas.KEY_NUM3);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM4", Canvas.KEY_NUM4);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM5", Canvas.KEY_NUM5);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM6", Canvas.KEY_NUM6);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM7", Canvas.KEY_NUM7);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM8", Canvas.KEY_NUM8);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_NUM9", Canvas.KEY_NUM9);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_STAR", Canvas.KEY_STAR);
		J2ME_ACTION_TO_KEYCODE_MAP.put("KEY_POUND", Canvas.KEY_POUND);
		J2ME_ACTION_TO_KEYCODE_MAP.put("SOFT_LEFT", Canvas.KEY_SOFT_LEFT); // Or map to KEY_SOFTKEY1 etc. if Canvas has them
		J2ME_ACTION_TO_KEYCODE_MAP.put("SOFT_RIGHT", Canvas.KEY_SOFT_RIGHT); // Or map to KEY_SOFTKEY2 etc.
	}

	private static PlatformImage lcd;
	private PlatformGraphics gc;
	public static int lcdWidth;
	public static int lcdHeight;

	// Frame Limit Variables
	private long lastRenderTime = System.nanoTime();
	private long requiredFrametime = 0;
	private long elapsedTime = 0;
	private long sleepTime = 0;

	// Whether the user has toggled the ShowFPS option
	private final int OVERLAY_WIDTH = 100;
	private final int OVERLAY_HEIGHT = 20;
	private String showFPS = "Off";
	private int frameCount = 0;
	private long lastFpsTime = System.nanoTime();
    private int fps = 0;

	public static boolean isLibretro = false;

	public MIDletLoader loader;
	public static Displayable displayable;

	public static boolean isPaused = false;

	public String dataPath = "";

	public volatile static int keyState = 0;
	public volatile static int vodafoneKeyState = 0;
	public volatile static int DoJaKeyState = 0;

	// MobilePlatform will handle the input repeats as well
	public static boolean[] pressedKeys = new boolean[22];

	public static Runnable painter;

	public MobilePlatform(int width, int height)
	{
		resizeLCD(width, height);
		previousPovValues = new HashMap<>();
		previousAxisValues = new HashMap<>();
		customMappedActionStates = new HashMap<>();

		painter = new Runnable()
		{
			public void run()
			{
					// Placeholder //
			}
		};

		initializeGamepadDetection();
	}

	private void initializeGamepadDetection() {
		gamepads = new ArrayList<>();
		try {
			ControllerEnvironment ca = ControllerEnvironment.getDefaultEnvironment();
			if (ca == null) {
				Mobile.log(Mobile.LOG_WARNING, "MobilePlatform: JInput ControllerEnvironment is null. Gamepad support may be unavailable.");
				return;
			}
			Controller[] controllers = ca.getControllers();
			Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Initializing JInput - Found " + controllers.length + " controllers total.");
			for (Controller controller : controllers) {
				Controller.Type type = controller.getType();
				Mobile.log(Mobile.LOG_DEBUG, "JInput: Found controller: " + controller.getName() + ", Type: " + type.toString());
				if (type == Controller.Type.STICK || type == Controller.Type.GAMEPAD) {
					gamepads.add(controller);
					Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Added gamepad: " + controller.getName() + " (Type: " + type.toString() + ")");
				}
			}
		} catch (Throwable e) {
			Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: Error initializing JInput or finding gamepads: " + e.getMessage());
		}
	}

	public void resizeLCD(int width, int height)
	{
		// No need to waste time here if the screen dimensions haven't changed (screen was just rotated for example)
		if(lcdWidth == width && lcdHeight == height) { return; }

		lcdWidth = width;
		lcdHeight = height;

		Font.setScreenSize(width, height);
		com.nttdocomo.ui.Font.setScreenSize(width, height);

		lcd = new PlatformImage(width, height);

		if(!Mobile.isDoJa) { gc = lcd.getMIDPGraphics(); }
		else { gc = lcd.getDoJaGraphics(); }
		
		/* 
		 * Try to have the jar scale as well. If this doesn't work,
		 * a simple restart is all it takes, just like before.
		 */

		if(!Mobile.isDoJa && Mobile.getDisplay() != null) 
		{ 
			Mobile.getDisplay().getCurrent().doSizeChanged(width, height);
			Mobile.getDisplay().getCurrent().platformImage = lcd; 
			Mobile.getDisplay().getCurrent().graphics = (Graphics) gc; 
		}
		else if(Mobile.isDoJa && com.nttdocomo.ui.Display.getCurrent() != null) // Doja's current Frames (Displayables) are static
		{
			// TODO: DoJa, it doesn't even render to screen yet, so i doubt this also works
			com.nttdocomo.ui.Display.getCurrent().platformImage = lcd; 
			com.nttdocomo.ui.Display.getCurrent().graphics = (com.nttdocomo.ui.Graphics) gc; 
		}
		
	}

	public BufferedImage getLCD() { return lcd.getCanvas(); }

	public void setPainter(Runnable r) { painter = r; }

	public static void pauseResumeApp() 
	{
		if(!Mobile.isDoJa) 
		{
			displayable = Mobile.getDisplay().getCurrent();
			if (!(displayable instanceof Canvas)) { return; }
			
			if(!isPaused) 
			{
				((Canvas) displayable).hideNotify();
				
				try { Mobile.midlet.callPauseApp(); } 
				catch (Exception e) { e.printStackTrace(); }

				isPaused = true;

				painter.run();
			}
			else 
			{
				isPaused = false;
				
				((Canvas) displayable).showNotify();
				
				try { Mobile.midlet.resumeRequest(); } 
				catch (Exception e) { e.printStackTrace(); }

				painter.run();
			}
		}
		else 
		{
			// TODO: DoJa pause/resume
		}
	}

	public static void keyPressed(int keycode)
	{
		if(!MIDletLoader.MIDletSelected) { MIDletLoader.keyPress(Mobile.getGameAction(keycode)); }
		else if (!isPaused)
		{
			updateKeyState(Mobile.getGameAction(keycode), 1);
			updateVodafoneKeyState(Mobile.getGameAction(keycode), 1);
			updateDoJaKeyState(Mobile.getGameAction(keycode), 1);
			if (!Mobile.isDoJa && Mobile.getDisplay() != null && (displayable = Mobile.getDisplay().getCurrent()) != null) 
			{ 
				displayable.keyPressed(keycode); 
				handleCommands(Mobile.getCanvasAction(keycode));
			}
		}
	}

	public static void keyReleased(int keycode)
	{
		if(!isPaused && MIDletLoader.MIDletSelected) 
		{
			updateKeyState(Mobile.getGameAction(keycode), 0);
			updateVodafoneKeyState(Mobile.getGameAction(keycode), 0);
			updateDoJaKeyState(Mobile.getGameAction(keycode), 0);
			if (!Mobile.isDoJa && Mobile.getDisplay() != null && (displayable = Mobile.getDisplay().getCurrent()) != null && MIDletLoader.MIDletSelected) { displayable.keyReleased(keycode); }
		}
	}

	public static void keyRepeated(int keycode)
	{
		if (!isPaused && MIDletLoader.MIDletSelected && !Mobile.isDoJa && Mobile.getDisplay() != null && (displayable = Mobile.getDisplay().getCurrent()) != null)  { displayable.keyRepeated(keycode); }
		// TODO: DoJa
	}

	public static void pointerDragged(int x, int y)
	{
		if (!isPaused && MIDletLoader.MIDletSelected && !Mobile.isDoJa && Mobile.getDisplay() != null && (displayable = Mobile.getDisplay().getCurrent()) != null)  { displayable.pointerDragged(x, y); }
		// TODO: DoJa
	}

	public static void pointerPressed(int x, int y)
	{
		if (!isPaused && MIDletLoader.MIDletSelected && !Mobile.isDoJa && Mobile.getDisplay() != null && (displayable = Mobile.getDisplay().getCurrent()) != null)  { displayable.pointerPressed(x, y); }
		// TODO: DoJa
	}

	public static void pointerReleased(int x, int y)
	{
		if (!isPaused && MIDletLoader.MIDletSelected && !Mobile.isDoJa && Mobile.getDisplay() != null && (displayable = Mobile.getDisplay().getCurrent()) != null)  { displayable.pointerReleased(x, y); }
		// TODO: DoJa
	}

	private static void updateKeyState(int key, int val)
	{
		int mask=0;
		switch (key)
		{
			case Canvas.KEY_NUM2: mask = GameCanvas.UP_PRESSED;     break;
			case Canvas.KEY_NUM4: mask = GameCanvas.LEFT_PRESSED;   break;
			case Canvas.KEY_NUM6: mask = GameCanvas.RIGHT_PRESSED;  break;
			case Canvas.KEY_NUM8: mask = GameCanvas.DOWN_PRESSED;   break;
			case Canvas.KEY_NUM5: mask = GameCanvas.FIRE_PRESSED;   break;
			case Canvas.KEY_NUM1: mask = GameCanvas.GAME_A_PRESSED; break;
			case Canvas.KEY_NUM3: mask = GameCanvas.GAME_B_PRESSED; break;
			case Canvas.KEY_NUM7: mask = GameCanvas.GAME_C_PRESSED; break;
			case Canvas.KEY_NUM9: mask = GameCanvas.GAME_D_PRESSED; break;
			case Canvas.UP:       mask = GameCanvas.UP_PRESSED;     break;
			case Canvas.LEFT:     mask = GameCanvas.LEFT_PRESSED;   break;
			case Canvas.RIGHT:    mask = GameCanvas.RIGHT_PRESSED;  break;
			case Canvas.DOWN:     mask = GameCanvas.DOWN_PRESSED;   break;
			case Canvas.FIRE:     mask = GameCanvas.FIRE_PRESSED;   break;
		}
		if(val == 1) { keyState |= mask; }
		else { keyState ^= mask; }
	}

	// Original implementation by Yury Kharchenko (J2ME-Loader)
	private static void updateVodafoneKeyState(int key, int val)
	{
		int mask=0;
		switch (key) 
		{
			case Canvas.UP:
				mask = 1 << 12; // 12 Up
				break;
			case Canvas.LEFT:
				mask = 1 << 13; // 13 Left
				break;
			case Canvas.RIGHT:
				mask = 1 << 14; // 14 Right
				break;
			case Canvas.DOWN:
				mask = 1 << 15; // 15 Down
				break;
			case Canvas.FIRE:
				mask = 1 << 16; // 16 Select
				break;
			case Canvas.GAME_C:
				mask = 1 << 19; // 19 Softkey 3
				break;
			case Canvas.KEY_NUM0:
				mask = 1; //  0 0
				break;
			case Canvas.KEY_NUM1:
				mask = 1 << 1; //  1 1
				break;
			case Canvas.KEY_NUM2:
				mask = 1 << 2; //  2 2
				break;
			case Canvas.KEY_NUM3:
				mask = 1 << 3; //  3 3
				break;
			case Canvas.KEY_NUM4:
				mask = 1 << 4; //  4 4
				break;
			case Canvas.KEY_NUM5:
				mask = 1 << 5; //  5 5
				break;
			case Canvas.KEY_NUM6:
				mask = 1 << 6; //  6 6
				break;
			case Canvas.KEY_NUM7:
				mask = 1 << 7; //  7 7
				break;
			case Canvas.KEY_NUM8:
				mask = 1 << 8; //  8 8
				break;
			case Canvas.KEY_NUM9:
				mask = 1 << 9; //  9 9
				break;
			case Canvas.KEY_STAR:
				mask = 1 << 10; // 10 *
				break;
			case Canvas.KEY_POUND:
				mask = 1 << 11; // 11 #
				break;
			case Canvas.KEY_SOFT_LEFT:
				mask = 1 << 17; // 17 Softkey 1
				break;
			case Canvas.KEY_SOFT_RIGHT:
				mask = 1 << 18; // 18 Softkey 2
				break;
			default:
				mask = 0;
		}
		if(val == 1) { vodafoneKeyState |= mask; }
		else { vodafoneKeyState ^= mask; }
	}

	// For a reference of these shift values, look into com.nttdocomo.ui.Display
	private static void updateDoJaKeyState(int key, int val)
	{
		int mask=0;
		switch (key) 
		{
			case Canvas.UP:
				mask = 1 << 0x11;
				break;
			case Canvas.LEFT:
				mask = 1 << 0x10;
				break;
			case Canvas.RIGHT:
				mask = 1 << 0x12; 
				break;
			case Canvas.DOWN:
				mask = 1 << 0x13; 
				break;
			case Canvas.FIRE:
				mask = 1 << 0x14;
				break;
			case Canvas.GAME_C:
				mask = 1 << 19; 
				break;
			case Canvas.KEY_NUM0:
				mask = 1; 
				break;
			case Canvas.KEY_NUM1:
				mask = 1 << 1; 
				break;
			case Canvas.KEY_NUM2:
				mask = 1 << 2; 
				break;
			case Canvas.KEY_NUM3:
				mask = 1 << 3; 
				break;
			case Canvas.KEY_NUM4:
				mask = 1 << 4;
				break;
			case Canvas.KEY_NUM5:
				mask = 1 << 5; 
				break;
			case Canvas.KEY_NUM6:
				mask = 1 << 6; 
				break;
			case Canvas.KEY_NUM7:
				mask = 1 << 7; 
				break;
			case Canvas.KEY_NUM8:
				mask = 1 << 8; 
				break;
			case Canvas.KEY_NUM9:
				mask = 1 << 9; 
				break;
			case Canvas.KEY_STAR:
				mask = 1 << 0x0a;
				break;
			case Canvas.KEY_POUND:
				mask = 1 << 0x0b;
				break;
			case Canvas.KEY_SOFT_LEFT:
				mask = 1 << 0x15;
				break;
			case Canvas.KEY_SOFT_RIGHT:
				mask = 1 << 0x16;
				break;
			default:
				mask = 0;
		}
		if(val == 1) { DoJaKeyState |= mask; }
		else { DoJaKeyState ^= mask; }
	}

	private static void handleCommands(int key) 
	{
		boolean canvasFullscreen = false; // Default to false, as all other displayables can show commands at all times
		if(displayable instanceof Canvas) { canvasFullscreen = ((Canvas)displayable).getFullScreen(); }

		if(!canvasFullscreen)
		{
			if (displayable.listCommands) 
			{ 
				if(key == Canvas.KEY_NUM2 || key == Canvas.UP) 
				{
					displayable.currentCommand--;
					if(displayable.currentCommand<0) { displayable.currentCommand = displayable.commands.size()-1; }
				}
				else if(key == Canvas.KEY_NUM8 || key == Canvas.DOWN) 
				{
					displayable.currentCommand++;
					if(displayable.currentCommand>=displayable.commands.size()) { displayable.currentCommand = 0; }
				}
				else if (key == Canvas.KEY_SOFT_LEFT) 
				{
					displayable.doLeftCommand();
					displayable.currentCommand = 0;
				}
				else if (key == Canvas.KEY_SOFT_RIGHT) 
				{
					displayable.listCommands = false;
					displayable.doRightCommand();
					displayable.currentCommand = 0;
				}

				displayable._invalidate(); 
			}
			else 
			{
				boolean handled = displayable.screenKeyPressed(key);
				if (!handled)
				{
					if (key == Canvas.KEY_SOFT_LEFT) 
					{
						displayable.doLeftCommand();
					} 
					else if (key == Canvas.KEY_SOFT_RIGHT) 
					{
						displayable.doRightCommand();
					}
				}
			}
		}
		
		
	}

/*
	******** Jar/Jad Loading ********
*/

	public boolean load(String fileName) 
	{
        Map<String, String> descriptorProperties = new HashMap<>();

		/* 
		 * Java treats "!/" sequences as a pointer to a file inside a jar, which will cause
		 * issues with MIDletLoader, so convert exclamations beforehand to not confuse it.
		 */
		fileName = fileName.replaceAll("!", "%21");

		if(fileName.toLowerCase().contains(".kjx")) // KDDI KJX parser, originally from J2ME-Loader by @ohayoyogi
		{
			System.out.println("filenamePre:" + fileName);
			try
			{
				File testDir = new File(Mobile.tempKJXDir);
				if(!testDir.isDirectory()) 
				{
					try 
					{
						testDir.mkdirs();
					}
					catch(Exception e) { Mobile.log(Mobile.LOG_ERROR, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Failed to create KDDI temp dir:" + e.getMessage()); }
				}

				File kjxFile = new File(new URI(fileName));
				File tmpfile = null;

				InputStream inputStream = new FileInputStream(kjxFile);
				DataInputStream dis = new DataInputStream(inputStream);
				byte[] magic = new byte[3];
				dis.read(magic, 0, 3);
				if (!Arrays.equals(magic, "KJX".getBytes())) 
				{
					throw new Exception("KJX Header string does not match: " + new String(magic));
				}
	
				byte startJadPos = dis.readByte();
				byte lenKjxFileName = dis.readByte();
				dis.skipBytes(lenKjxFileName);
				int lenJadFileContent = dis.readUnsignedShort();
				byte lenJadFileName = dis.readByte();
				byte[] jadFileName = new byte[lenJadFileName];
				dis.read(jadFileName, 0, lenJadFileName);
				String strJadFileName = new String(jadFileName);
	
				int bufSize = 2048;
				byte[] buf = new byte[bufSize];
	
				// Write jad and parse its descriptors
				tmpfile = new File(Mobile.tempKJXDir, strJadFileName);
				try (FileOutputStream fos = new FileOutputStream(tmpfile)) 
				{
					int restSize = lenJadFileContent;
					while(restSize > 0) 
					{
						int readSize = dis.read(buf, 0, Math.min(restSize, bufSize));
						fos.write(buf, 0, readSize);
						restSize -= readSize;
					}
				}

				try (InputStream targetStream = new FileInputStream(tmpfile)) { MIDletLoader.parseDescriptorInto(targetStream, descriptorProperties); } 
				catch (IOException e) 
				{
					Mobile.log(Mobile.LOG_ERROR, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Failed to load Jad data: " + e.getMessage());
					return false;
				}
	
				// Write jar
				tmpfile = new File(Mobile.tempKJXDir, strJadFileName.substring(0, strJadFileName.length() -4) + ".jar");
				try (FileOutputStream fos = new FileOutputStream(tmpfile)) {
					int length = 0;
					while((length = dis.read(buf)) > 0) {
						fos.write(buf, 0, length);
					}
				}

				// Send dumped jar path to loader
				fileName = "file:" + tmpfile.getAbsolutePath().replace("./", "");

				URL jar = new URL(fileName);
				loader = new MIDletLoader(jar, descriptorProperties);

				if(Mobile.deleteTemporaryKJXFiles) 
				{
					tmpfile.delete(); // Delete the temporary jad file
					tmpfile = new File(Mobile.tempKJXDir, strJadFileName);
					tmpfile.delete(); // Delete the temporary jar file
				}
				
				return true;
			} 
			catch (Exception e) { Mobile.log(Mobile.LOG_INFO, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Couldn't load KJX file:" + e.getMessage()); return false; }
		}
		else // If it's not KJX, it's JAD or JAR
		{
			/*
			 * If loading a jar directly, check if an accompanying jad with the same name 
			 * is present in the directory, to load any platform properties from there.
			 */
			if(fileName.toLowerCase().contains(".jar")) 
			{
				try 
				{
					File checkJad = new File(new URI(fileName.replace(".jar", ".jad")));
					if(checkJad.exists() && !checkJad.isDirectory()) 
					{
						Mobile.log(Mobile.LOG_INFO, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Accompanying JAD found! Parsing additional MIDlet properties.");
						fileName = fileName.replace(".jar", ".jad"); 
					}
				} catch (Exception e) { Mobile.log(Mobile.LOG_INFO, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Couldn't check for accompanying JAD:" + e.getMessage()); }
			}
			
			boolean isJad = fileName.toLowerCase().endsWith(".jad");

			if (isJad) 
			{
				String preparedFileName = fileName.substring(fileName.lastIndexOf(":") + 1).trim();
				try { preparedFileName = URLDecoder.decode(preparedFileName, StandardCharsets.UTF_8.name()); } 
				catch (Exception e) 
				{
					System.err.println("Error decoding file name: " + e.getMessage());
					return false;
				}

				try (InputStream targetStream = new FileInputStream(preparedFileName)) { MIDletLoader.parseDescriptorInto(targetStream, descriptorProperties); } 
				catch (IOException e) 
				{
					Mobile.log(Mobile.LOG_ERROR, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Failed to load Jad data: " + e.getMessage());
					return false;
				}

				// JAD file was parsed, so get the jar path and load it next

				String jarUrl = descriptorProperties.getOrDefault("MIDlet-Jar-URL", preparedFileName.replace(".jad", ".jar"));

				// We will not support downloading jars from the internet on the fly, unless there is a very good reason to do so. Also, unless the jad has a URI for loading the jar, ignore the path as well
				jarUrl = fileName.replace(".jad", ".jar"); // Just try getting the jar in the same directory as the jad in those cases.

				fileName = jarUrl;
			}

			try 
			{
				URL jar = new URL(fileName);
				loader = new MIDletLoader(jar, descriptorProperties);
				return true;
			} 
			catch (Exception e) 
			{
				Mobile.log(Mobile.LOG_ERROR, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Failed to load Jar: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
    }

	public void runJar()
	{
		try {
			loader.start();
			startGamepadPolling(); // Start polling when JAR runs
		}
		catch (Exception e)
		{
			Mobile.log(Mobile.LOG_ERROR, MobilePlatform.class.getPackage().getName() + "." + MobilePlatform.class.getSimpleName() + ": " + "Error Running Jar");
			e.printStackTrace();
		}
	}

	public void startGamepadPolling() {
		if (gamepads == null || gamepads.isEmpty()) {
			Mobile.log(Mobile.LOG_INFO, "MobilePlatform: No gamepads detected, polling not started.");
			return;
		}
		if (!gamepadPollingActive) {
			gamepadPollingActive = true;
			gamepadPollingThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Gamepad polling thread started.");
					net.java.games.input.Event event = new net.java.games.input.Event();
					while (gamepadPollingActive) {
						for (Controller controller : gamepads) {
							if (!controller.poll()) {
								Mobile.log(Mobile.LOG_WARNING, "MobilePlatform: Failed to poll controller: " + controller.getName());
								continue;
							}

							Map<String, String> customMappings = null;
							if (Mobile.config != null) {
								customMappings = Mobile.config.getGamepadMappings(controller.getName());
							}
							boolean useCustom = customMappings != null && !customMappings.isEmpty();
							Mobile.log(Mobile.LOG_DEBUG, "Processing controller '" + controller.getName() + (useCustom ? "' with CUSTOM mappings." : "' with DEFAULT mappings."));

							EventQueue queue = controller.getEventQueue();
							while (queue.getNextEvent(event)) {
								Component component = event.getComponent();
								String componentIdString = component.getIdentifier().toString();
								String componentIdName = component.getIdentifier().getName(); // For config matching (e.g. "0", "x")
								String componentDisplayName = component.getName(); // Human-readable (e.g. "Button 0", "X Axis")
								float value = event.getValue();
								boolean eventProcessed = false;

								Mobile.log(Mobile.LOG_DEBUG, String.format("JInput Event: Controller='%s', CompDisplayName='%s', CompIDStr='%s', CompIDName='%s', Value='%.3f'",
									controller.getName(), componentDisplayName, componentIdString, componentIdName, value));

								if (useCustom) {
									for (Map.Entry<String, String> mappingEntry : customMappings.entrySet()) {
										String j2meActionName = mappingEntry.getKey();
										String mappedJInputComponentId = mappingEntry.getValue(); // This is usually componentIdName from AWTGUI like "0", "x", "pov"

										// Prefer matching by componentIdName (which is what AWTGUI stores), but fallback to componentIdString if needed
										boolean matched = componentIdName.equals(mappedJInputComponentId) || componentIdString.equals(mappedJInputComponentId);

										if (matched) {
											Mobile.log(Mobile.LOG_DEBUG, "Custom map: J2ME Action='" + j2meActionName + "' -> JInput Comp='" + mappedJInputComponentId + "'. Event from CompIDName='" + componentIdName + "'. Evaluating...");
											int j2meCanvasKeyCode = mapJ2MEActionNameToKeyCode(j2meActionName);
											if (j2meCanvasKeyCode == -1) {
												Mobile.log(Mobile.LOG_WARNING, "Custom map: Unknown J2ME Action Name: " + j2meActionName);
												continue;
											}

											boolean pressed = determinePressedState(component, value, j2meActionName, true);
											// Use a more descriptive key for state tracking that includes the J2ME action,
											// as one JInput component (like POV) can map to multiple J2ME actions (UP, DOWN etc.)
											String stateChangeKey = controller.getName() + "_" + mappedJInputComponentId + "_ACTION_" + j2meActionName;

											boolean previousPressedState = customMappedActionStates.getOrDefault(stateChangeKey.hashCode(), false);

											if (previousPressedState != pressed) {
												processGamepadAction(j2meCanvasKeyCode, pressed);
												customMappedActionStates.put(stateChangeKey.hashCode(), pressed);
												Mobile.log(Mobile.LOG_DEBUG, String.format("CustomMap Action: Controller='%s', J2MEAction='%s'(%d), JInputCompID='%s', Value='%.2f', PrevPressed=%b, NewPressed=%b -> PROCESSING",
													controller.getName(), j2meActionName, j2meCanvasKeyCode, mappedJInputComponentId, value, previousPressedState, pressed));
											}
											eventProcessed = true;
											break;
										}
									}
								}

								if (!eventProcessed) {
									// Fallback to default handlers if not processed by custom mapping or no custom map for this controller
									Identifier id = component.getIdentifier(); // JInput's full identifier object
									String defaultHandlerKey = controller.getName() + "_" + componentIdName; // Using componentIdName for state tracking consistency

									if (id == Identifier.Axis.POV) {
										handleDefaultPovEvent(defaultHandlerKey, value, componentDisplayName);
									} else if (id instanceof Identifier.Button && !component.isAnalog()) {
										handleDefaultButtonEvent(componentIdName, value, componentDisplayName); // Pass componentIdName for map lookup
									} else if (component.isAnalog() && (id == Identifier.Axis.X || id == Identifier.Axis.Y)) {
										handleDefaultAxisEvent(defaultHandlerKey, id, value, componentDisplayName);
									}
								}
							}
						}
						try {
							Thread.sleep(20); // Poll at ~50Hz
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: Gamepad polling thread interrupted.");
							break;
						}
					}
					Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Gamepad polling thread stopped.");
				}
			});
			gamepadPollingThread.setName("GamepadPollingThread");
			gamepadPollingThread.setDaemon(true); // So it doesn't prevent JVM shutdown
			gamepadPollingThread.start();
		}
	}

	private int mapJ2MEActionNameToKeyCode(String j2meActionName) {
		return J2ME_ACTION_TO_KEYCODE_MAP.getOrDefault(j2meActionName, -1);
	}

	private boolean determinePressedState(Component component, float value, String j2meActionName, boolean isCustomMapping) {
		Identifier id = component.getIdentifier();
		// For custom mappings, j2meActionName helps interpret POV/axis. For default, it's more direct.
		if (id == Identifier.Axis.POV) {
			if (j2meActionName.equals("UP")) return value == POV.UP || value == POV.UP_LEFT || value == POV.UP_RIGHT;
			if (j2meActionName.equals("DOWN")) return value == POV.DOWN || value == POV.DOWN_LEFT || value == POV.DOWN_RIGHT;
			if (j2meActionName.equals("LEFT")) return value == POV.LEFT || value == POV.UP_LEFT || value == POV.DOWN_LEFT;
			if (j2meActionName.equals("RIGHT")) return value == POV.RIGHT || value == POV.UP_RIGHT || value == POV.DOWN_RIGHT;
			// If j2meActionName is something generic like "POV_TRIGGER", any non-center might be true.
			// For now, assume direct directional mapping from AWTGUI.
			return false; // Not a recognized POV direction for the action
		} else if (id instanceof Identifier.Button) {
			return value == 1.0f;
		} else if (component.isAnalog()) { // Analog axes
			final float TRIGGER_THRESHOLD = 0.5f; // Could be configurable per-mapping too
			if (j2meActionName.equals("UP")) return value < -TRIGGER_THRESHOLD;
			if (j2meActionName.equals("DOWN")) return value > TRIGGER_THRESHOLD;
			if (j2meActionName.equals("LEFT")) return value < -TRIGGER_THRESHOLD;
			if (j2meActionName.equals("RIGHT")) return value > TRIGGER_THRESHOLD;
			// Could also map full axis range to a button, e.g. gas pedal
			// For now, assume axis-to-dpad style mapping from AWTGUI.
		}
		return false;
	}


	private void handleDefaultPovEvent(String povStateKey, float value, String componentDisplayName) {
		float previousValue = previousPovValues.getOrDefault(povStateKey, 0.0f);
		if (value == previousValue) return;

		Mobile.log(Mobile.LOG_DEBUG, String.format("DefaultHandlePOV: Comp='%s', PrevValue=%.2f, NewValue=%.2f", componentDisplayName, previousValue, value));

		// Release previous POV state
		if (previousValue == POV.UP) processGamepadAction(Canvas.UP, false);
		else if (previousValue == POV.DOWN) processGamepadAction(Canvas.DOWN, false);
		else if (previousValue == POV.LEFT) processGamepadAction(Canvas.LEFT, false);
		else if (previousValue == POV.RIGHT) processGamepadAction(Canvas.RIGHT, false);
		else if (previousValue == POV.UP_LEFT) { processGamepadAction(Canvas.UP, false); processGamepadAction(Canvas.LEFT, false); }
		else if (previousValue == POV.UP_RIGHT) { processGamepadAction(Canvas.UP, false); processGamepadAction(Canvas.RIGHT, false); }
		else if (previousValue == POV.DOWN_LEFT) { processGamepadAction(Canvas.DOWN, false); processGamepadAction(Canvas.LEFT, false); }
		else if (previousValue == POV.DOWN_RIGHT) { processGamepadAction(Canvas.DOWN, false); processGamepadAction(Canvas.RIGHT, false); }

		// Press new POV state
		if (value == POV.UP) processGamepadAction(Canvas.UP, true);
		else if (value == POV.DOWN) processGamepadAction(Canvas.DOWN, true);
		else if (value == POV.LEFT) processGamepadAction(Canvas.LEFT, true);
		else if (value == POV.RIGHT) processGamepadAction(Canvas.RIGHT, true);
		else if (value == POV.UP_LEFT) { processGamepadAction(Canvas.UP, true); processGamepadAction(Canvas.LEFT, true); }
		else if (value == POV.UP_RIGHT) { processGamepadAction(Canvas.UP, true); processGamepadAction(Canvas.RIGHT, true); }
		else if (value == POV.DOWN_LEFT) { processGamepadAction(Canvas.DOWN, true); processGamepadAction(Canvas.LEFT, true); }
		else if (value == POV.DOWN_RIGHT) { processGamepadAction(Canvas.DOWN, true); processGamepadAction(Canvas.RIGHT, true); }

		previousPovValues.put(povStateKey, value);
	}

	private void handleDefaultButtonEvent(String buttonIdName, float value, String componentDisplayName) {
		Integer j2meAction = DEFAULT_BUTTON_MAP.get(buttonIdName); // buttonIdName is like "0", "1"
		if (j2meAction != null) {
			boolean pressed = (value == 1.0f);
			// For default buttons, state change is implicit with the event (JInput usually sends one event per press/release)
			processGamepadAction(j2meAction, pressed);
			Mobile.log(Mobile.LOG_DEBUG, String.format("DefaultHandleButton: Comp='%s' (ID='%s'), Value=%.1f. Mapped to J2ME Action=%d (%s)",
					componentDisplayName, buttonIdName, value, j2meAction, (pressed ? "Pressed" : "Released")));
		} else {
			Mobile.log(Mobile.LOG_DEBUG, String.format("DefaultHandleButton: Comp='%s' (ID='%s'), Value=%.1f. No default mapping.",
					componentDisplayName, buttonIdName, value));
		}
	}

	private void handleDefaultAxisEvent(String axisStateKey, Identifier id, float value, String componentDisplayName) {
		final float DEAD_ZONE = 0.25f;
		final float TRIGGER_THRESHOLD = 0.5f;

		float previousValue = previousAxisValues.getOrDefault(axisStateKey, 0.0f);
		Mobile.log(Mobile.LOG_DEBUG, String.format("DefaultHandleAxis: Comp='%s', PrevVal=%.2f, CurrVal=%.2f", componentDisplayName, previousValue, value));

		// X-Axis handling
		if (id == Identifier.Axis.X) {
			boolean currentLeft = value < -TRIGGER_THRESHOLD;
			boolean prevLeftState = previousAxisValues.getOrDefault(axisStateKey + "_LEFT", 0.0f) == 1.0f;
			if (currentLeft && !prevLeftState) { processGamepadAction(Canvas.LEFT, true); previousAxisValues.put(axisStateKey + "_LEFT", 1.0f); }
			else if (!currentLeft && prevLeftState && value >= -DEAD_ZONE) { processGamepadAction(Canvas.LEFT, false); previousAxisValues.put(axisStateKey + "_LEFT", 0.0f); }

			boolean currentRight = value > TRIGGER_THRESHOLD;
			boolean prevRightState = previousAxisValues.getOrDefault(axisStateKey + "_RIGHT", 0.0f) == 1.0f;
			if (currentRight && !prevRightState) { processGamepadAction(Canvas.RIGHT, true); previousAxisValues.put(axisStateKey + "_RIGHT", 1.0f); }
			else if (!currentRight && prevRightState && value <= DEAD_ZONE) { processGamepadAction(Canvas.RIGHT, false); previousAxisValues.put(axisStateKey + "_RIGHT", 0.0f); }
		}
		// Y-Axis handling
		else if (id == Identifier.Axis.Y) {
			boolean currentUp = value < -TRIGGER_THRESHOLD;
			boolean prevUpState = previousAxisValues.getOrDefault(axisStateKey + "_UP", 0.0f) == 1.0f;
			if (currentUp && !prevUpState) { processGamepadAction(Canvas.UP, true); previousAxisValues.put(axisStateKey + "_UP", 1.0f); }
			else if (!currentUp && prevUpState && value >= -DEAD_ZONE) { processGamepadAction(Canvas.UP, false); previousAxisValues.put(axisStateKey + "_UP", 0.0f); }

			boolean currentDown = value > TRIGGER_THRESHOLD;
			boolean prevDownState = previousAxisValues.getOrDefault(axisStateKey + "_DOWN", 0.0f) == 1.0f;
			if (currentDown && !prevDownState) { processGamepadAction(Canvas.DOWN, true); previousAxisValues.put(axisStateKey + "_DOWN", 1.0f); }
			else if (!currentDown && prevDownState && value <= DEAD_ZONE) { processGamepadAction(Canvas.DOWN, false); previousAxisValues.put(axisStateKey + "_DOWN", 0.0f); }
		}
		previousAxisValues.put(axisStateKey, value); // Store current raw axis value for next event comparison
	}

	private static void processGamepadAction(int j2meGameAction, boolean pressed) {
		if (j2meGameAction == -1) return;

		Mobile.log(Mobile.LOG_DEBUG, String.format("processGamepadAction: J2ME ActionCode=%d, Pressed=%b", j2meGameAction, pressed));
		updateKeyState(j2meGameAction, pressed ? 1 : 0);
		updateVodafoneKeyState(j2meGameAction, pressed ? 1 : 0);
		updateDoJaKeyState(j2meGameAction, pressed ? 1 : 0);
		Mobile.log(Mobile.LOG_DEBUG, "processGamepadAction: New keyState = " + Integer.toBinaryString(keyState) +
                                 ", vodafoneKeyState = " + Integer.toBinaryString(vodafoneKeyState) +
                                 ", DoJaKeyState = " + Integer.toBinaryString(DoJaKeyState));
		// TODO: Consider if direct calls to displayable.keyPressed/keyReleased are needed
		// for full compatibility, or if keyState is sufficient.
		// This might require queuing events to the main AWT thread.
		// For now, GameCanvas.getKeyStates() will reflect these changes.
	}

	public void stopGamepadPolling() {
		gamepadPollingActive = false;
		if (gamepadPollingThread != null) {
			try {
				gamepadPollingThread.join(100); // Wait a bit for the thread to finish
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: Interrupted while stopping gamepad polling thread.");
			}
			gamepadPollingThread = null;
		}
		Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Gamepad polling requested to stop.");
		previousPovValues.clear();
		previousAxisValues.clear();
		customMappedActionStates.clear();
	}

/*
	********* Graphics ********
*/

	public void flushGraphics(PlatformImage img, int x, int y, int width, int height)
	{
		if (!isPaused)
		{
			if (gc == null) {
				Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: Graphics context is null, reinitializing...");
				if (lcd != null) {
					gc = Mobile.isDoJa ? lcd.getDoJaGraphics() : lcd.getMIDPGraphics();
				} else {
					Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: LCD is null, cannot reinitialize GC");
					return;
				}
			}
			try {
				gc.flushGraphics(img, x, y, width, height);
				if (!showFPS.equals("Off")) { showFPS(); }
				painter.run();
			} catch (Exception e) {
				Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: Flush graphics failed: " + e.getMessage());
			}
		}
	}

	public void limitFps() 
	{
		frameCount++;
		if(Mobile.limitFPS == 0 || pressedKeys[19]) { lastRenderTime = System.nanoTime(); return; }

		requiredFrametime = 1_000_000_000 / Mobile.limitFPS;
		elapsedTime = System.nanoTime() - lastRenderTime;
		sleepTime = (requiredFrametime - elapsedTime); // Sleep time in nanoseconds

		/* 
		 * TODO: Framerate still deviates a little from the intended lock 
		 * 
		 * Possible solution: Some kind of calibration mechanism to nudge the
		 * actual lock closer to the user's display refresh rate.
		 */
		if (sleepTime > 0) { LockSupport.parkNanos(sleepTime); }

		lastRenderTime = System.nanoTime();
	}

	// For now, the logic here works by updating the framerate counter every second
	private final void showFPS() 
	{
		if (System.nanoTime() - lastFpsTime >= 1_000_000_000) 
		{ 
			fps = frameCount; 
			frameCount = 0; 
			lastFpsTime = System.nanoTime(); 
		}

		BufferedImage overlayImage = new BufferedImage(OVERLAY_WIDTH, OVERLAY_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D overlayGraphics = overlayImage.createGraphics();

        gc.getGraphics2D().setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		gc.getGraphics2D().setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		// Set the overlay background
		overlayGraphics.setColor(new Color(0, 0, 105, 150)); // BG is a semi-transparent dark blue
		overlayGraphics.fillRect(0, 0, OVERLAY_WIDTH, OVERLAY_HEIGHT);
	
		// Adjust the font size
		int fontSize = 21; // Base font size
		overlayGraphics.setFont(overlayGraphics.getFont().deriveFont((float) fontSize));
		overlayGraphics.setColor(new Color(255, 175, 0, 255)); // Text color is orange
	
		// Draw the FPS text
		String fpsText = "FPS: " + fps;
		overlayGraphics.drawString(fpsText, 3, 17);
	
		overlayGraphics.dispose(); // Clean up graphics
	
		// Scale the overlay image to fit the screen
		double scale = Math.min(lcdWidth, lcdHeight);

		int scaledWidth = 0;
		if(scale < 100) { scaledWidth = (int) (lcdWidth / 2);}
		if(scale > 100) { scaledWidth = (int) (lcdWidth / 2.5);}
		if(scale > 200) { scaledWidth = (int) (lcdWidth / 3);}
		if(scale > 300) { scaledWidth = (int) (lcdWidth / 4);}
		if(scale > 400) { scaledWidth = (int) (lcdWidth / 5);}
		int scaledHeight = (int) (scaledWidth / 5);
	
		// Draw the scaled overlay image onto the jar's main screen.
		if(showFPS.equals("TopLeft"))          { gc.getGraphics2D().drawImage(overlayImage, 2, 2, scaledWidth, scaledHeight, null); }
		else if(showFPS.equals("TopRight"))    { gc.getGraphics2D().drawImage(overlayImage, lcdWidth-scaledWidth-2, 2, scaledWidth, scaledHeight, null); }
		else if(showFPS.equals("BottomLeft"))  { gc.getGraphics2D().drawImage(overlayImage, 2, lcdHeight-scaledHeight-2, scaledWidth, scaledHeight, null); }
		else if(showFPS.equals("BottomRight")) { gc.getGraphics2D().drawImage(overlayImage, lcdWidth-scaledWidth-2, lcdHeight-scaledHeight-2, scaledWidth, scaledHeight, null); }
	}

	public void setShowFPS(String show) { showFPS = show; }

	public void stopApp() {
		stopGamepadPolling(); // Stop polling when app stops
		try {
			if (loader != null && Mobile.midlet != null) {
				Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Destroying MIDlet...");
				Mobile.midlet.destroyApp(true);
				Mobile.midlet = null;
			}
			if (Mobile.getDisplay() != null) {
				Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Clearing display...");
				Mobile.getDisplay().setCurrent(null);
			}
			loader = null;
			displayable = null;
			isPaused = false;
			keyState = 0;
			vodafoneKeyState = 0;
			DoJaKeyState = 0;
			Arrays.fill(pressedKeys, false);
		} catch (Exception e) {
			Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: Stop app failed: " + e.getMessage());
		}
	}

	public void reset()
	{
		Mobile.log(Mobile.LOG_INFO, "MobilePlatform: Resetting platform...");
		stopGamepadPolling(); // Stop polling on reset as well
		try {
			stopApp(); // stopApp already calls stopGamepadPolling, but good to be sure
			// Clear graphics buffer
			if (lcd != null) {
				Graphics2D g2d = lcd.getCanvas().createGraphics();
				g2d.setColor(Color.BLACK);
				g2d.fillRect(0, 0, lcdWidth, lcdHeight);
				g2d.dispose();
			}
			//gc = null;
			lcd = new PlatformImage(lcdWidth, lcdHeight);
			gc = Mobile.isDoJa ? lcd.getDoJaGraphics() : lcd.getMIDPGraphics();
			Mobile.log(Mobile.LOG_DEBUG, "MobilePlatform: LCD and GC reinitialized");
		} catch (Exception e) {
			Mobile.log(Mobile.LOG_ERROR, "MobilePlatform: Reset failed: " + e.getMessage());
		}
	}

	public boolean isJarLoaded() {
	return loader != null; }

	public List<Controller> getDetectedGamepads() {
		return gamepads;
	}
}
