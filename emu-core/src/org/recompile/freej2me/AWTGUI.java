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
package org.recompile.freej2me;

import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.Component;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;
import java.util.ArrayList;

// import org.libsdl.SDL; // Old import
// import org.libsdl.SDL_Joystick; // Old import
import io.github.libsdl4j.api.Sdl;
import io.github.libsdl4j.api.joystick.SdlJoystick; // Class with static methods
import io.github.libsdl4j.api.joystick.SDL_Joystick; // JNA PointerType handle
import io.github.libsdl4j.api.SdlSubSystemConst; // For SDL_INIT_JOYSTICK
import io.github.libsdl4j.api.joystick.SdlJoystickConst; // For HAT states and other joystick constants
import io.github.libsdl4j.api.error.SdlError; // For GetError

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

public final class AWTGUI 
{
	final String VERSION = "1.45";
	private ArrayList<SDL_Joystick> joysticks = new ArrayList<SDL_Joystick>(); // Use the handle type
	/* This is used to indicate to FreeJ2ME that it has to call "settingsChanged()" to apply changes made here */
	private boolean hasPendingChange;

	/* Indicates whether a jar file was loaded successfully */
	private boolean fileLoaded = false;
	private boolean firstLoad = false;

	/* String that points to the jar file that has to be loaded */
	String jarfile = "";

	/* This is meant to be a local reference of FreeJ2ME's main frame */
	private Frame main;

	/* And this is meant to be a local reference of FreeJ2ME's config */
	private Config config;

	/* AWT's main MenuBar */
	final MenuBar menuBar = new MenuBar();

	/* MenuBar's menus */
	final Menu fileMenu = new Menu("File");
	final Menu optionMenu = new Menu("Settings");
	final Menu speedHackMenu = new Menu("SpeedHacks"); 
	final Menu compatSettingsMenu = new Menu("Compatibility Settings"); 
	final Menu debugMenu = new Menu("Debug");

	/* Sub menus (for now, all of them are located in "Settings") */
	final Menu fpsCap = new Menu("FPS Limit");
	final Menu unlockFPSHack = new Menu("Unlock FPS Hack");
	final Menu showFPS = new Menu("Show FPS Counter");
	final Menu phoneType = new Menu("Phone Key Layout");
	final Menu backlightColor = new Menu("Backlight Color");
	final Menu fontOffset = new Menu("Font Size Offset");

	/* Dialogs for resolution changes, restart notifications, MemStats and info about FreeJ2ME */
	final Dialog[] awtDialogs = 
	{
		new Dialog(main , "Set LCD Resolution", true),
		new Dialog(main , "About FreeJ2ME", true),
		new Dialog(main, "FreeJ2ME MemStat", false),
		new Dialog(main, "Restart Required", true),
		new Dialog(main, "Key Mapping", true),
	};
	
	final Button[] awtButtons = 
	{
		new Button("Close"),
		new Button("Apply"),
		new Button("Cancel"),
		new Button("Close FreeJ2ME"),
		new Button("Restart later"),
		new Button("Apply Inputs"),
		new Button("Cancel")
	};
	

	/* Log Level submenu */
	Menu logLevel = new Menu("Log Level");

	/* M3G Debug submenu */
	Menu M3GDebug = new Menu("M3G Debugging");

	/* Input mapping keys */
	final Button inputButtons[] = new Button[] 
	{
		new Button("Q"),
		new Button("W"),
		new Button("Up"),
		new Button("Left"),
		new Button("Enter"),
		new Button("Right"),
		new Button("Down"),
		new Button("NumPad-7"),
		new Button("NumPad-8"),
		new Button("NumPad-9"),
		new Button("NumPad-4"),
		new Button("NumPad-5"),
		new Button("NumPad-6"),
		new Button("NumPad-1"),
		new Button("NumPad=2"),
		new Button("NumPad-3"),
		new Button("E"),
		new Button("NumPad-0"),
		new Button("R"),
		new Button("Space"),
		new Button("C"),
		new Button("X")
	};

	/* Array of inputs in order to support input remapping */
	int inputKeycodes[]; // Initialized in constructor after config is available
	private int newInputKeycodes[]; // Initialized in constructor

	// Gamepad input buttons and action keys
	final Button gamepadInputButtons[] = new Button[inputButtons.length];
	final String actionKeys[] = new String[] {
		"LeftSoft", "RightSoft", "ArrowUp", "ArrowLeft", "Fire", "ArrowRight", "ArrowDown",
		"Num7", "Num8", "Num9", "Num4", "Num5", "Num6", "Num1", "Num2", "Num3",
		"Star", "Num0", "Pound",
		"FastForward", "Screenshot", "PauseResume"
	};
	// Temporary storage for new gamepad bindings, similar to newInputKeycodes
	private final java.util.HashMap<String, String> newGamepadBindings = new java.util.HashMap<String, String>();
	private int activeGamepadBindingButtonIndex = -1; // Index of the gamepad button currently waiting for input
	private Thread gamepadPollingThread = null;
	private volatile boolean stopGamepadPolling = false;
	private final java.util.HashMap<String, Boolean> previousGamepadInputStates = new java.util.HashMap<>();
	private final java.util.HashMap<String, Integer> actionToKeyboardKeyCodeMap = new java.util.HashMap<>();
	private static final short AXIS_DEADZONE_THRESHOLD = 8000;


	final Choice resChoice = new Choice();

	Label totalMemLabel = new Label("Total Mem: 000000000 KB");
	Label freeMemLabel = new Label("Free Mem : 000000000 KB");
	Label usedMemLabel = new Label("Used Mem : 000000000 KB");
	Label maxMemLabel = new Label("Max Mem  : 000000000 KB");

	/* Items for each of the bar's menus */
	final UIListener menuItemListener = new UIListener();

	final MenuItem aboutMenuItem = new MenuItem("About FreeJ2ME");
	final MenuItem resChangeMenuItem = new MenuItem("Change Phone Resolution");

	final MenuItem openMenuItem = new MenuItem("Open JAR / JAD / KJX File");
	final MenuItem closeMenuItem = new MenuItem("Close Jar (Stub)");
	final MenuItem scrShot = new MenuItem("Take Screenshot (Ctrl+C)");
	final MenuItem pauseRes = new MenuItem("Pause / Resume (Ctrl+X)");
	final MenuItem exitMenuItem = new MenuItem("Exit FreeJ2ME");
	final MenuItem mapInputs = new MenuItem("Manage Inputs");

	final MenuItem showPlayer = new MenuItem("J2ME Media Player");

	final CheckboxMenuItem fullScreen = new CheckboxMenuItem("Toggle Fullscreen (Ctrl+F)");
	final CheckboxMenuItem enableAudio = new CheckboxMenuItem("Enable Audio", false);
	final CheckboxMenuItem enableRotation = new CheckboxMenuItem("Rotate Screen", false);
	final CheckboxMenuItem useCustomMidi = new CheckboxMenuItem("Use custom midi soundfont", false);
	final CheckboxMenuItem useCustomFont = new CheckboxMenuItem("Use custom text font", false);

	final CheckboxMenuItem[] layoutOptions = 
	{
		new CheckboxMenuItem("Default", true),
		new CheckboxMenuItem("LG", false),
		new CheckboxMenuItem("Motorola/SoftBank", false),
		new CheckboxMenuItem("Motorola V8", false),
		new CheckboxMenuItem("Motorola Triplets", false),
		new CheckboxMenuItem("Nokia Full Keyboard", false),
		new CheckboxMenuItem("Sagem", false),
		new CheckboxMenuItem("Siemens", false),
		new CheckboxMenuItem("Siemens Old", false)
	};
	final String[] layoutValues = {"Standard", "LG", "Motorola", "MotoV8", "MotoTriplets", "NokiaKeyboard", "Sagem", "Siemens", "SiemensOld"};
	
	final CheckboxMenuItem[] backlightOptions = 
	{
		new CheckboxMenuItem("White/Disabled", false),
		new CheckboxMenuItem("Green", true),
		new CheckboxMenuItem("Cyan", false),
		new CheckboxMenuItem("Orange", false),
		new CheckboxMenuItem("Violet", false),
		new CheckboxMenuItem("Red", false)
	};
	final String[] backlightValues = {"Disabled", "Green", "Cyan", "Orange", "Violet", "Red"};

	final CheckboxMenuItem[] fpsOptions = 
	{
		new CheckboxMenuItem("No Limit", true),
		new CheckboxMenuItem("60 FPS", false),
		new CheckboxMenuItem("40 FPS", false),
		new CheckboxMenuItem("30 FPS", false),
		new CheckboxMenuItem("20 FPS", false),
		new CheckboxMenuItem("15 FPS", false)
	};
	final String[] fpsValues = {"0", "60", "40", "30", "20", "15"};

	final CheckboxMenuItem[] fpsHackOptions = 
	{
		new CheckboxMenuItem("Disabled", true),
		new CheckboxMenuItem("Safe", false),
		new CheckboxMenuItem("Extended", false),
		new CheckboxMenuItem("Aggressive", false)
	};
	final String[] fpsHackValues = {"Disabled", "Safe", "Extended", "Aggressive"};

	final CheckboxMenuItem[] fpsCounterPos = 
	{
		new CheckboxMenuItem("Off", true),
		new CheckboxMenuItem("Top Left", false),
		new CheckboxMenuItem("Top Right", false),
		new CheckboxMenuItem("Bottom Left", false),
		new CheckboxMenuItem("Bottom Right", false)
	};
	final String[] showFPSValues = {"Off", "TopLeft", "TopRight", "BottomLeft", "BottomRight"};

	final CheckboxMenuItem[] fontOffsets = 
	{
		new CheckboxMenuItem("-4pt", false),
		new CheckboxMenuItem("-3pt", false),
		new CheckboxMenuItem("-2pt", false),
		new CheckboxMenuItem("-1pt", false),
		new CheckboxMenuItem(" 0pt (Default)", true),
		new CheckboxMenuItem(" 1pt", false),
		new CheckboxMenuItem(" 2pt", false),
		new CheckboxMenuItem(" 3pt", false),
		new CheckboxMenuItem(" 4pt", false)
	};
	final String[] fontOffsetValues = {"-4", "-3", "-2", "-1", "0", "1", "2", "3", "4"};

	final CheckboxMenuItem[] logLevels = 
	{
		new CheckboxMenuItem("Disabled", false),
		new CheckboxMenuItem("Debug", false),
		new CheckboxMenuItem("Info", false),
		new CheckboxMenuItem("Warning", false),
		new CheckboxMenuItem("Error", false)
	};
	final String[] logLevelValues = {"0", "1", "2", "3", "4"};

	// Speedhacks
	final CheckboxMenuItem noAlphaOnBlankImages = new CheckboxMenuItem("No alpha on blank images");
	
	// Compatibility settings
	final CheckboxMenuItem NonFatalNullImages = new CheckboxMenuItem("Don't throw Exception on null images");
	final CheckboxMenuItem transToOriginOnReset = new CheckboxMenuItem("Translate to origin on gfx reset");
	final CheckboxMenuItem ignoreGCCalls = new CheckboxMenuItem("Ignore garbage collection calls");

	final CheckboxMenuItem deleteTemporaryKJXFiles = new CheckboxMenuItem("Delete KJX files' temporary JAR/JAD");
	final CheckboxMenuItem dumpAudioData = new CheckboxMenuItem("Dump Audio Streams");
	final CheckboxMenuItem dumpGraphicsData = new CheckboxMenuItem("Dump Graphics Objects");
	final CheckboxMenuItem showMemoryUsage = new CheckboxMenuItem("Show VM Memory Usage");
	
	// M3G Debugging
	final CheckboxMenuItem M3GUntextured = new CheckboxMenuItem("Draw Only Vertex Colors");
	final CheckboxMenuItem M3GWireframe = new CheckboxMenuItem("Wireframe Mode");


	public AWTGUI(Config config)
	{
		this.config = config;
		this.inputKeycodes = config.getInputKeycodes();
		this.newInputKeycodes = Arrays.copyOf(this.inputKeycodes, this.inputKeycodes.length);
		initActionToKeyboardKeyCodeMap();

		// Initialize Gamepad Buttons
		for (int i = 0; i < gamepadInputButtons.length; i++) {
			gamepadInputButtons[i] = new Button("GP: None"); // Default text
			gamepadInputButtons[i].setBackground(FreeJ2ME.freeJ2MEDragColor);
			gamepadInputButtons[i].setForeground(Color.CYAN); // Different color for distinction
			// Set action command to identify the button later
			gamepadInputButtons[i].setActionCommand("SetGamepad_" + actionKeys[i]);
		}

		// Set initial labels for keyboard input buttons
		for(int i = 0; i < inputButtons.length; i++) {
            if (i < this.inputKeycodes.length) {
                inputButtons[i].setLabel(java.awt.event.KeyEvent.getKeyText(this.inputKeycodes[i]));
            } else {
                inputButtons[i].setLabel("Undefined");
            }
        }

		// Initialize SDL joystick subsystem
		if (Sdl.SDL_InitSubSystem(SdlSubSystemConst.SDL_INIT_JOYSTICK) < 0) {
			Mobile.log(Mobile.LOG_ERROR, "Failed to initialize SDL joystick subsystem: " + SdlError.SDL_GetError()); // Use SdlError class
		} else {
			int numJoysticks = SdlJoystick.SDL_NumJoysticks();
			Mobile.log(Mobile.LOG_INFO, "Number of joysticks detected: " + numJoysticks);
			for (int i = 0; i < numJoysticks; i++) {
				SDL_Joystick joystick = SdlJoystick.SDL_JoystickOpen(i); // Returns SDL_Joystick handle
				if (joystick != null) {
					joysticks.add(joystick);
					Mobile.log(Mobile.LOG_INFO, "Opened joystick " + i + ": " + SdlJoystick.SDL_JoystickName(joystick));
					Mobile.log(Mobile.LOG_INFO, "  Axes: " + SdlJoystick.SDL_JoystickNumAxes(joystick));
					Mobile.log(Mobile.LOG_INFO, "  Buttons: " + SdlJoystick.SDL_JoystickNumButtons(joystick));
					Mobile.log(Mobile.LOG_INFO, "  Hats: " + SdlJoystick.SDL_JoystickNumHats(joystick));
				} else {
					Mobile.log(Mobile.LOG_ERROR, "Failed to open joystick " + i + ": " + SdlError.SDL_GetError()); // Use SdlError class
				}
			}
		}

		resChoice.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		totalMemLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
		freeMemLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
		usedMemLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
		maxMemLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));

		awtButtons[0].setBackground(FreeJ2ME.freeJ2MEDragColor);
		awtButtons[0].setForeground(Color.ORANGE);

		awtButtons[1].setBackground(FreeJ2ME.freeJ2MEDragColor);
		awtButtons[1].setForeground(Color.ORANGE);

		awtButtons[2].setBackground(FreeJ2ME.freeJ2MEBGColor);
		awtButtons[2].setForeground(Color.ORANGE);
		
		awtButtons[3].setBackground(FreeJ2ME.freeJ2MEDragColor);
		awtButtons[3].setForeground(Color.ORANGE);

		awtButtons[4].setBackground(FreeJ2ME.freeJ2MEBGColor);
		awtButtons[4].setForeground(Color.ORANGE);

		awtDialogs[1].setBackground(FreeJ2ME.freeJ2MEBGColor);
		awtDialogs[1].setForeground(Color.ORANGE);
		awtDialogs[1].setLayout( new FlowLayout(FlowLayout.CENTER, 200, 0));  
		awtDialogs[1].setUndecorated(true); /* Whenever a Dialog is undecorated, it's because it's meant to look like an internal menu on FreeJ2ME's main Frame */
		awtDialogs[1].setSize(230, 235);
		awtDialogs[1].setResizable(false);
		awtDialogs[1].setLocationRelativeTo(main);
		awtDialogs[1].add(new Label("FreeJ2ME-Plus - A free J2ME emulator"));
		awtDialogs[1].add(new Label("Version " + VERSION));
		awtDialogs[1].add(new Label("--------------------------------"));
		awtDialogs[1].add(new Label("Original Project Authors:"));
		awtDialogs[1].add(new Label("David Richardson (Recompile)"));
		awtDialogs[1].add(new Label("Saket Dandawate (hex007)"));
		awtDialogs[1].add(new Label("--------------------------------"));
		awtDialogs[1].add(new Label("Plus Fork Maintainer:"));
		awtDialogs[1].add(new Label("Paulo Sousa (AShiningRay)"));
		awtDialogs[1].add(awtButtons[0]);


		awtDialogs[0].setBackground(FreeJ2ME.freeJ2MEBGColor);
		awtDialogs[0].setForeground(Color.ORANGE);
		awtDialogs[0].setLayout( new FlowLayout(FlowLayout.CENTER, 60, 5));
		awtDialogs[0].setUndecorated(true);
		awtDialogs[0].setSize(230, 125);
		awtDialogs[0].setResizable(false);
		awtDialogs[0].setLocationRelativeTo(main);
		awtDialogs[0].add(new Label("Select a Resolution from the Dropdown"));
		awtDialogs[0].add(new Label("Then hit 'Apply'!"));
		awtDialogs[0].add(resChoice);
		awtDialogs[0].add(awtButtons[1]);
		awtDialogs[0].add(awtButtons[2]);


		awtDialogs[2].setBackground(FreeJ2ME.freeJ2MEBGColor);
		awtDialogs[2].setForeground(Color.ORANGE);
		awtDialogs[2].setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		awtDialogs[2].setSize(240, 145);
		awtDialogs[2].setResizable(false);
		awtDialogs[2].add(totalMemLabel);
		awtDialogs[2].add(freeMemLabel);
		awtDialogs[2].add(usedMemLabel);
		awtDialogs[2].add(maxMemLabel);

		/* Input mapping dialog: It's a grid, so a few tricks had to be employed to align everything up */
		awtDialogs[4].setBackground(FreeJ2ME.freeJ2MEBGColor);
        awtDialogs[4].setForeground(Color.ORANGE);
        // New layout: Label | Keyboard Button | Gamepad Button
		awtDialogs[4].setLayout(new GridLayout(0, 3));
		awtDialogs[4].setSize(380, 720); // Increased size
		awtDialogs[4].setLocationRelativeTo(main);
		awtDialogs[4].setResizable(true); // Allow resizing for now, consider JScrollPane later
		

		// Setup input button colors
		awtButtons[5].setBackground(FreeJ2ME.freeJ2MEDragColor);
		awtButtons[5].setForeground(Color.GREEN);

		awtButtons[6].setBackground(FreeJ2ME.freeJ2MEDragColor);
		awtButtons[6].setForeground(Color.RED);

		for(int i = 0; i < inputButtons.length; i++) 
		{ 
			inputButtons[i].setBackground(FreeJ2ME.freeJ2MEDragColor);
			inputButtons[i].setForeground(Color.ORANGE);
		}

		awtDialogs[4].add(new Label("Action", Label.CENTER));
		awtDialogs[4].add(new Label("Keyboard", Label.CENTER));
		awtDialogs[4].add(new Label("Gamepad", Label.CENTER));
		
		// Add Apply and Cancel buttons at the top
		awtDialogs[4].add(awtButtons[5]); // Apply Inputs
		awtDialogs[4].add(new Label("")); // Spacer
		awtDialogs[4].add(awtButtons[6]); // Cancel Inputs


		String[] actionLabels = new String[] {
			"Left Soft", "Right Soft", "Arrow Up", "Arrow Left", "Fire", "Arrow Right", "Arrow Down",
			"Num 7", "Num 8", "Num 9", "Num 4", "Num 5", "Num 6", "Num 1", "Num 2", "Num 3",
			"Star", "Num 0", "Pound",
			"Fast Fwd", "Screenshot", "Pause/Res"
		};

		for (int i = 0; i < inputButtons.length; i++) {
			Label actionLabel = new Label(actionLabels[i], Label.LEFT);
			awtDialogs[4].add(actionLabel);
			awtDialogs[4].add(inputButtons[i]);
			awtDialogs[4].add(gamepadInputButtons[i]);
		}


		awtDialogs[3].setBackground(FreeJ2ME.freeJ2MEBGColor);
		awtDialogs[3].setForeground(Color.ORANGE);
		awtDialogs[3].setLayout( new FlowLayout(FlowLayout.CENTER, 10, 10));  
		awtDialogs[3].setUndecorated(true);
		awtDialogs[3].setSize(230, 80);
		awtDialogs[3].setLocationRelativeTo(main);
		awtDialogs[3].add(new Label("This change requires a restart to apply!"));
		awtDialogs[3].add(awtButtons[3]);
		awtDialogs[3].add(awtButtons[4]);
		
		openMenuItem.setActionCommand("Open");
		closeMenuItem.setActionCommand("Close");
		scrShot.setActionCommand("Screenshot");
		pauseRes.setActionCommand("PauseResume");
		exitMenuItem.setActionCommand("Exit");
		aboutMenuItem.setActionCommand("AboutMenu");
		resChangeMenuItem.setActionCommand("ChangeResolution");
		awtButtons[1].setActionCommand("ApplyResChange");
		awtButtons[2].setActionCommand("CancelResChange");
		awtButtons[0].setActionCommand("CloseAboutMenu");
		awtButtons[3].setActionCommand("CloseFreeJ2ME");
		awtButtons[4].setActionCommand("RestartLater");
		mapInputs.setActionCommand("MapInputs");
		awtButtons[5].setActionCommand("ApplyInputs");
		awtButtons[6].setActionCommand("CancelInputs");

		showPlayer.setActionCommand("ShowPlayer");
		
		openMenuItem.addActionListener(menuItemListener);
		closeMenuItem.addActionListener(menuItemListener);
		scrShot.addActionListener(menuItemListener);
		pauseRes.addActionListener(menuItemListener);
		exitMenuItem.addActionListener(menuItemListener);
		aboutMenuItem.addActionListener(menuItemListener);
		resChangeMenuItem.addActionListener(menuItemListener);
		awtButtons[1].addActionListener(menuItemListener);
		awtButtons[2].addActionListener(menuItemListener);
		awtButtons[0].addActionListener(menuItemListener);
		awtButtons[3].addActionListener(menuItemListener);
		awtButtons[4].addActionListener(menuItemListener);
		mapInputs.addActionListener(menuItemListener);
		awtButtons[5].addActionListener(menuItemListener);
		awtButtons[6].addActionListener(menuItemListener);

		showPlayer.addActionListener(menuItemListener);

		addInputButtonListeners();
		addGamepadButtonListeners();

		setActionListeners();

		buildMenuBar();
	}

	private void addInputButtonListeners() 
	{
		for(int i = 0; i < inputButtons.length; i++) 
		{
			final int buttonIndex = i;

			/* Add a focus listener to each input mapping button */
            inputButtons[i].addFocusListener(new FocusAdapter() 
			{
                Button focusedButton;
				String lastButtonKey = new String("");
				boolean keySet = false;

				@Override
				public void focusGained(FocusEvent e) 
				{
					{
						keySet = false;
						focusedButton = (Button) e.getComponent();
						lastButtonKey = focusedButton.getLabel();
						focusedButton.setLabel("Waiting...");

						focusedButton.addKeyListener(new KeyAdapter() 
						{
							public void keyPressed(KeyEvent e) 
							{
								focusedButton.setLabel(KeyEvent.getKeyText(e.getKeyCode()));
								keySet = true;
								/* Save the new key's code into the expected index of newInputKeycodes */
								newInputKeycodes[buttonIndex] = e.getKeyCode();
							}
						});
					}
				}

				/* Only used to restore the last key map if the user doesn't map a new one into the button */
				@Override
				public void focusLost(FocusEvent e) { if(!keySet) { focusedButton.setLabel(lastButtonKey); } }
            });
		}
	}

	private void addGamepadButtonListeners() {
		for (int i = 0; i < gamepadInputButtons.length; i++) {
			final int buttonIndex = i;
			gamepadInputButtons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (activeGamepadBindingButtonIndex != -1 && activeGamepadBindingButtonIndex != buttonIndex) {
						// Another gamepad button was already waiting, restore its label
						gamepadInputButtons[activeGamepadBindingButtonIndex].setLabel(
							newGamepadBindings.getOrDefault("input_" + actionKeys[activeGamepadBindingButtonIndex] + "_Gamepad", "GP: None")
						);
						if (gamepadInputButtons[activeGamepadBindingButtonIndex].getLabel().isEmpty() || gamepadInputButtons[activeGamepadBindingButtonIndex].getLabel().equals("GP: ")) {
							gamepadInputButtons[activeGamepadBindingButtonIndex].setLabel("GP: None");
						}
					}

					if (activeGamepadBindingButtonIndex == buttonIndex) { // Clicked again on the button that was waiting
						stopGamepadPolling = true;
						if (gamepadPollingThread != null && gamepadPollingThread.isAlive()) {
							try { gamepadPollingThread.join(100); } catch (InterruptedException ignored) {}
						}
						gamepadInputButtons[buttonIndex].setLabel(
							newGamepadBindings.getOrDefault("input_" + actionKeys[buttonIndex] + "_Gamepad", "GP: None")
						);
						if (gamepadInputButtons[buttonIndex].getLabel().isEmpty() || gamepadInputButtons[buttonIndex].getLabel().equals("GP: ")) {
							gamepadInputButtons[buttonIndex].setLabel("GP: None");
						}
						activeGamepadBindingButtonIndex = -1;
						return;
					}

					activeGamepadBindingButtonIndex = buttonIndex;
					gamepadInputButtons[buttonIndex].setLabel("Waiting GP...");
					stopGamepadPolling = false;

					gamepadPollingThread = new Thread(new Runnable() {
						public void run() {
							long startTime = System.currentTimeMillis();
							String detectedBinding = "";

							while (!stopGamepadPolling && (System.currentTimeMillis() - startTime) < 5000) { // 5-second timeout
								SdlJoystick.SDL_JoystickUpdate();
								if (joysticks.isEmpty()) {
									break;
								}
								SDL_Joystick joystick = joysticks.get(0); // Use the handle type

								// Check Hats (D-Pad)
								for (int hatIdx = 0; hatIdx < SdlJoystick.SDL_JoystickNumHats(joystick); hatIdx++) {
									byte hatState = SdlJoystick.SDL_JoystickGetHat(joystick, hatIdx);
									if (hatState != SdlJoystickConst.SDL_HAT_CENTERED) {
										if ((hatState & SdlJoystickConst.SDL_HAT_UP) != 0) detectedBinding = "GP0_HAT" + hatIdx + "_UP";
										else if ((hatState & SdlJoystickConst.SDL_HAT_DOWN) != 0) detectedBinding = "GP0_HAT" + hatIdx + "_DOWN";
										else if ((hatState & SdlJoystickConst.SDL_HAT_LEFT) != 0) detectedBinding = "GP0_HAT" + hatIdx + "_LEFT";
										else if ((hatState & SdlJoystickConst.SDL_HAT_RIGHT) != 0) detectedBinding = "GP0_HAT" + hatIdx + "_RIGHT";
										stopGamepadPolling = true; break;
									}
								}
								if (stopGamepadPolling) break;

								// Check Buttons
								for (int btnIdx = 0; btnIdx < SdlJoystick.SDL_JoystickNumButtons(joystick); btnIdx++) {
									if (SdlJoystick.SDL_JoystickGetButton(joystick, btnIdx) == 1) {
										detectedBinding = "GP0_BUTTON_" + btnIdx;
										stopGamepadPolling = true; break;
									}
								}
								if (stopGamepadPolling) break;

								// Check Axes
								for (int axisIdx = 0; axisIdx < SdlJoystick.SDL_JoystickNumAxes(joystick); axisIdx++) {
									short axisValue = SdlJoystick.SDL_JoystickGetAxis(joystick, axisIdx);
									if (axisValue > AXIS_DEADZONE_THRESHOLD) {
										detectedBinding = "GP0_AXIS_" + axisIdx + "_POS";
										stopGamepadPolling = true; break;
									} else if (axisValue < -AXIS_DEADZONE_THRESHOLD) {
										detectedBinding = "GP0_AXIS_" + axisIdx + "_NEG";
										stopGamepadPolling = true; break;
									}
								}
								if (stopGamepadPolling) break;
								try { Thread.sleep(50); } catch (InterruptedException ignored) {}
							}

							final String finalBinding = detectedBinding;
							java.awt.EventQueue.invokeLater(new Runnable() {
								public void run() {
									if (activeGamepadBindingButtonIndex == buttonIndex) { // Check if still the active button
										if (!finalBinding.isEmpty()) {
											gamepadInputButtons[buttonIndex].setLabel("GP: " + finalBinding);
											newGamepadBindings.put("input_" + actionKeys[buttonIndex] + "_Gamepad", finalBinding);
										} else {
											// Timeout or no input, restore previous or "GP: None"
											String previousBinding = newGamepadBindings.getOrDefault("input_" + actionKeys[buttonIndex] + "_Gamepad", "");
											gamepadInputButtons[buttonIndex].setLabel(previousBinding.isEmpty() ? "GP: None" : "GP: " + previousBinding);
										}
										activeGamepadBindingButtonIndex = -1; // Reset active button
									}
								}
							});
						}
					});
					gamepadPollingThread.setDaemon(true);
					gamepadPollingThread.start();
				}
			});
		}
	}

	private void setActionListeners() 
	{
		fullScreen.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(hasLoadedFile()) { FreeJ2ME.app.toggleFullscreen(); }
				else { fullScreen.setState(FreeJ2ME.isFullscreen); }
			}
		});

		enableAudio.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(enableAudio.getState()){ config.updateSound("on"); hasPendingChange = true; }
				else{ config.updateSound("off"); hasPendingChange = true; }
			}
		});

		enableRotation.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(enableRotation.getState()){ config.updateRotate("on"); hasPendingChange = true; }
				else{ config.updateRotate("off"); hasPendingChange = true; }
			}
		});

		useCustomMidi.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(useCustomMidi.getState()){ config.updateSoundfont("Custom"); hasPendingChange = true; }
				else{ config.updateSoundfont("Default"); hasPendingChange = true; }

				awtDialogs[3].setLocationRelativeTo(main);
				awtDialogs[3].setVisible(true);
			}
		});

		useCustomFont.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(useCustomFont.getState()){ config.updateTextFont("Custom"); hasPendingChange = true; }
				else{ config.updateTextFont("Default"); hasPendingChange = true; }

				awtDialogs[3].setLocationRelativeTo(main);
				awtDialogs[3].setVisible(true);
			}
		});

		// Speedhacks
		noAlphaOnBlankImages.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(noAlphaOnBlankImages.getState()){ config.updateAlphaSpeedHack("on"); hasPendingChange = true; }
				else{ config.updateAlphaSpeedHack("off"); hasPendingChange = true; }

				awtDialogs[3].setLocationRelativeTo(main);
				awtDialogs[3].setVisible(true);
			}
		});

		// Compatibility settings
		NonFatalNullImages.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(NonFatalNullImages.getState()){ config.updateCompatNonFatalNullImage("on"); hasPendingChange = true; }
				else{ config.updateCompatNonFatalNullImage("off"); hasPendingChange = true; }

				awtDialogs[3].setLocationRelativeTo(main);
				awtDialogs[3].setVisible(true);
			}
		});

		transToOriginOnReset.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(transToOriginOnReset.getState()){ config.updateCompatTranslateToOriginOnReset("on"); hasPendingChange = true; }
				else{ config.updateCompatTranslateToOriginOnReset("off"); hasPendingChange = true; }

				awtDialogs[3].setLocationRelativeTo(main);
				awtDialogs[3].setVisible(true);
			}
		});

		ignoreGCCalls.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(ignoreGCCalls.getState()){ config.updateCompatIgnoreGCCalls("on"); hasPendingChange = true; }
				else{ config.updateCompatIgnoreGCCalls("off"); hasPendingChange = true; }

				awtDialogs[3].setLocationRelativeTo(main);
				awtDialogs[3].setVisible(true);
			}
		});

		// Layout options
		for(byte i = 0; i < layoutOptions.length; i++) 
		{
			final byte index = i;
			layoutOptions[i].addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if(!layoutOptions[index].getState()){ layoutOptions[index].setState(true); }
					if(layoutOptions[index].getState())
					{ 
						config.updatePhone(layoutValues[index]);
						for(int j = 0; j < layoutOptions.length; j++) 
						{
							if(j != index) { layoutOptions[j].setState(false); }
						}
						hasPendingChange = true;
					}
				}
			});
		}

		for(byte i = 0; i < backlightOptions.length; i++) 
		{
			final byte index = i;
			backlightOptions[i].addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if(!backlightOptions[index].getState()){ backlightOptions[index].setState(true); }
					if(backlightOptions[index].getState())
					{ 
						config.updateBacklight(backlightValues[index]);
						for(int j = 0; j < backlightOptions.length; j++) 
						{
							if(j != index) { backlightOptions[j].setState(false); }
						}
						hasPendingChange = true;
					}
				}
			});
		}

		for(byte i = 0; i < fpsOptions.length; i++) 
		{
			final byte index = i;
			fpsOptions[i].addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if(!fpsOptions[index].getState()){ fpsOptions[index].setState(true); }
					if(fpsOptions[index].getState())
					{ 
						config.updateFPS(fpsValues[index]);
						for(int j = 0; j < fpsOptions.length; j++) 
						{
							if(j != index) { fpsOptions[j].setState(false); }
						}
						hasPendingChange = true;
					}
				}
			});
		}

		for(byte i = 0; i < fpsHackOptions.length; i++) 
		{
			final byte index = i;
			fpsHackOptions[i].addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if(!fpsHackOptions[index].getState()){ fpsHackOptions[index].setState(true); }
					if(fpsHackOptions[index].getState())
					{ 
						config.updateFPSHack(fpsHackValues[index]);
						for(int j = 0; j < fpsHackOptions.length; j++) 
						{
							if(j != index) { fpsHackOptions[j].setState(false); }
						}
						hasPendingChange = true;
					}
				}
			});
		}

		for(byte i = 0; i < fontOffsets.length; i++) 
		{
			final byte index = i;
			fontOffsets[i].addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if(!fontOffsets[index].getState()){ fontOffsets[index].setState(true); }
					if(fontOffsets[index].getState())
					{ 
						config.updateFontOffset(fontOffsetValues[index]);
						for(int j = 0; j < fontOffsets.length; j++) 
						{
							if(j != index) { fontOffsets[j].setState(false); }
						}
					}
				}
			});
		}

		// Sys settings
		for(byte i = 0; i < fpsCounterPos.length; i++) 
		{
			final byte index = i;
			fpsCounterPos[i].addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if(!fpsCounterPos[index].getState()){ fpsCounterPos[index].setState(true); }
					if(fpsCounterPos[index].getState())
					{ 
						config.updatefpsCounterPosition(showFPSValues[index]);
						Mobile.getPlatform().setShowFPS(showFPSValues[index]);
						for(int j = 0; j < fpsCounterPos.length; j++) 
						{
							if(j != index) { fpsCounterPos[j].setState(false); }
						}
					}
				}
			});
		}

		for(byte i = 0; i < logLevels.length; i++) 
		{
			final byte index = i;
			logLevels[i].addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if(!logLevels[index].getState()){ logLevels[index].setState(true); }
					if(logLevels[index].getState())
					{
						config.updateLogLevel(logLevelValues[index]);
						Mobile.logging = (index > 0);
						Mobile.minLogLevel = (byte) (index-1); // This can go negative if index = 0, as it won't log anyway.
						for(int j = 0; j < logLevels.length; j++) 
						{
							if(j != index) { logLevels[j].setState(false); }
						}
					}
				}
			});
		}
		
		deleteTemporaryKJXFiles.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(deleteTemporaryKJXFiles.getState()) { config.updateDeleteTempKJXFiles("on"); Mobile.deleteTemporaryKJXFiles = true; }
				else { config.updateDeleteTempKJXFiles("off"); Mobile.deleteTemporaryKJXFiles = false; }
			}
		});
		

		dumpAudioData.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(dumpAudioData.getState()) { config.updateDumpAudioStreams("on"); Mobile.dumpAudioStreams = true; }
				else { config.updateDumpAudioStreams("off"); Mobile.dumpAudioStreams = false; }
			}
		});

		dumpGraphicsData.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(dumpGraphicsData.getState()) { config.updateDumpGraphicsObjects("on"); }
				else { config.updateDumpGraphicsObjects("off"); }
			}
		});

		M3GUntextured.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(M3GUntextured.getState()) { config.updateM3GUntextured("on"); Mobile.M3GRenderUntexturedPolygons = true; }
				else { config.updateM3GUntextured("off"); Mobile.M3GRenderUntexturedPolygons = false; }
			}
		});

		M3GWireframe.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(M3GWireframe.getState()) { config.updateM3GWireframe("on"); Mobile.M3GRenderWireframe = true; }
				else { config.updateM3GWireframe("on"); Mobile.M3GRenderWireframe = false; }
			}
		});

		// This one is specific to AWTGUI
		showMemoryUsage.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent e) 
			{
				/* Mem stats frame won't be centered on FreeJ2ME's frame, instead, it will sit right by its side, that's why "setLocationRelativeTo(main)" isn't used */
				if(showMemoryUsage.getState()) { awtDialogs[2].setLocation(main.getLocation().x+main.getSize().width, main.getLocation().y); awtDialogs[2].setVisible(true); }
				else { awtDialogs[2].setVisible(false); }
			}
		});
	}

	private void buildMenuBar() 
	{
		//add menu items to menus
		fileMenu.add(openMenuItem);
		fileMenu.add(closeMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(scrShot);
		fileMenu.add(pauseRes);
		fileMenu.addSeparator();
		fileMenu.add(aboutMenuItem);
		fileMenu.add(exitMenuItem);

		optionMenu.add(fullScreen);
		optionMenu.add(enableAudio);
		optionMenu.add(enableRotation);
		optionMenu.add(useCustomMidi);
		optionMenu.add(useCustomFont);
		optionMenu.add(resChangeMenuItem);
		optionMenu.add(mapInputs);
		optionMenu.add(phoneType);
		optionMenu.add(backlightColor);
		optionMenu.add(fpsCap);
		optionMenu.add(unlockFPSHack);
		optionMenu.add(fontOffset);
		optionMenu.add(speedHackMenu);
		optionMenu.add(compatSettingsMenu);

		debugMenu.add(showPlayer);
		debugMenu.addSeparator();
		debugMenu.add(showFPS);
		debugMenu.add(deleteTemporaryKJXFiles);
		debugMenu.add(dumpAudioData);
		debugMenu.add(dumpGraphicsData);
		debugMenu.add(showMemoryUsage);
		debugMenu.add(logLevel);
		debugMenu.add(M3GDebug);
		
		deleteTemporaryKJXFiles.setState(true);

		for(int i = 0; i < logLevels.length; i++) { logLevel.add(logLevels[i]); }
		logLevels[0].setState(false);
		logLevels[1].setState(false);
		logLevels[2].setState(true);
		logLevels[3].setState(false);
		logLevels[4].setState(false);

		M3GDebug.add(M3GUntextured);
		M3GDebug.add(M3GWireframe);

		for(int i = 0; i < config.supportedResolutions.length; i++) { resChoice.add(config.supportedResolutions[i]); }
		for(int i = 0; i < layoutOptions.length; i++) { phoneType.add(layoutOptions[i]); }
		for(int i = 0; i < backlightOptions.length; i++) { backlightColor.add(backlightOptions[i]); }
		for(int i = 0; i < fpsOptions.length; i++) { fpsCap.add(fpsOptions[i]); }
		for(int i = 0; i < fpsHackOptions.length; i++) { unlockFPSHack.add(fpsHackOptions[i]); }
		for(int i = 0; i < fpsCounterPos.length; i++) { showFPS.add(fpsCounterPos[i]); }
		for(int i = 0; i < fontOffsets.length; i++) { fontOffset.add(fontOffsets[i]); }

		speedHackMenu.add(noAlphaOnBlankImages);

		compatSettingsMenu.add(NonFatalNullImages);
		compatSettingsMenu.add(transToOriginOnReset);
		compatSettingsMenu.add(ignoreGCCalls);
		
		// add menus to menubar
		menuBar.add(fileMenu);
		menuBar.add(optionMenu);
		menuBar.add(debugMenu);
	}

	public void updateOptions() 
	{
			fullScreen.setState(FreeJ2ME.isFullscreen);
			enableAudio.setState(config.settings.get("sound").equals("on"));
			enableRotation.setState(config.settings.get("rotate").equals("on"));
			useCustomMidi.setState(config.settings.get("soundfont").equals("Custom"));
			useCustomFont.setState(config.settings.get("textfont").equals("Custom"));

			for(int i = 0; i < fpsOptions.length; i++) { fpsOptions[i].setState(config.settings.get("fps").equals(fpsValues[i])); }

			for(int i = 0; i < fpsHackOptions.length; i++) { fpsHackOptions[i].setState(config.settings.get("fpshack").equals(fpsHackValues[i])); }

			for(int i = 0; i < fontOffsets.length; i++) { fontOffsets[i].setState(config.settings.get("fontoffset").equals(fontOffsetValues[i])); }

			for(int i = 0; i < layoutOptions.length; i++) 
			{
				layoutOptions[i].setState(config.settings.get("phone").equals(layoutValues[i]));
			}

			for(int i = 0; i < backlightOptions.length; i++) 
			{
				backlightOptions[i].setState(config.settings.get("backlightcolor").equals(backlightValues[i]));
			}

			noAlphaOnBlankImages.setState(config.settings.get("spdhacknoalpha").equals("on"));

			NonFatalNullImages.setState(config.settings.get("compatnonfatalnullimage").equals("on"));

			transToOriginOnReset.setState(config.settings.get("compattranstooriginonreset").equals("on"));

			ignoreGCCalls.setState(config.settings.get("compatignoregccalls").equals("on"));
			
			resChoice.select(""+ Integer.parseInt(config.settings.get("scrwidth")) + "x" + ""+ Integer.parseInt(config.settings.get("scrheight")));

			// Sys Settings
			for(int i = 0; i < logLevels.length; i++) { logLevels[i].setState(config.sysSettings.get("logLevel").equals(logLevelValues[i])); }

			for(int i = 0; i < fpsCounterPos.length; i++) { fpsCounterPos[i].setState(config.sysSettings.get("fpsCounterPosition").equals(showFPSValues[i])); }

			dumpGraphicsData.setState(config.sysSettings.get("dumpGraphicsObjects").equals("on"));

			dumpAudioData.setState(config.sysSettings.get("dumpAudioStreams").equals("on"));
			
			M3GWireframe.setState(config.sysSettings.get("M3GWireframe").equals("on"));

			M3GUntextured.setState(config.sysSettings.get("M3GUntextured").equals("on"));

			deleteTemporaryKJXFiles.setState(config.sysSettings.get("deleteTempKJXFiles").equals("on"));

			// Get saved inputs from system config file.
			this.inputKeycodes = config.getInputKeycodes();
			this.newInputKeycodes = Arrays.copyOf(this.inputKeycodes, this.inputKeycodes.length); // Keep temporary copy in sync
			for(int i = 0; i < inputButtons.length; i++) { inputButtons[i].setLabel(KeyEvent.getKeyText(inputKeycodes[i])); }
			
			/* We only need to do this call once, when the jar first loads */
			firstLoad = false;
	}

	public void updateMemStatDialog() 
	{
		totalMemLabel.setText(new String("Total Mem: " + (Runtime.getRuntime().totalMemory() / 1024) + " KB"));
		freeMemLabel.setText(new String("Free Mem : " + (Runtime.getRuntime().freeMemory() / 1024) + " KB"));
		usedMemLabel.setText(new String("Used Mem : " + ((Runtime.getRuntime().totalMemory() / 1024) - (Runtime.getRuntime().freeMemory() / 1024)) + " KB"));
		maxMemLabel.setText(new String("Max Mem  : " + (Runtime.getRuntime().maxMemory() / 1024) + " KB"));
	}

	class UIListener implements ActionListener 
	{
		public void actionPerformed(ActionEvent a) 
		{            

			if(a.getActionCommand() == "Open") 
			{
				FileDialog filePicker = new FileDialog(main, "Open JAR / JAD / KJX File", FileDialog.LOAD);
				String filename;
				filePicker.setFilenameFilter(new FilenameFilter()
				{
					public boolean accept(File dir, String name) 
					{ return name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".jad") || name.toLowerCase().endsWith(".kjx"); }
				});
				filePicker.setVisible(true);

				filename = filePicker.getFile();
				
				if(filename == null) { Mobile.log(Mobile.LOG_DEBUG, AWTGUI.class.getPackage().getName() + "." + AWTGUI.class.getSimpleName() + ": " + "JAR/JAD Loading was cancelled"); }
				else
				{
					try 
					{
						jarfile = new File(filePicker.getDirectory()+filename).toURI().toString();
						loadJarFile(jarfile, true); 
					}
				 	catch(Exception e) { Mobile.log(Mobile.LOG_DEBUG, AWTGUI.class.getPackage().getName() + "." + AWTGUI.class.getSimpleName() + ": " + "Load error:" + e.getMessage()); }
				}
			}

			else if(a.getActionCommand() == "Close") 
			{
				try
				{
					/* TODO: Try closing the loaded jar without closing FreeJ2ME */
				}
				catch (Throwable e) { Mobile.log(Mobile.LOG_ERROR, AWTGUI.class.getPackage().getName() + "." + AWTGUI.class.getSimpleName() + ": " + "Couldn't close jar"); }
			}

			else if(a.getActionCommand() == "Screenshot") { ScreenShot.takeScreenshot(false); }

			else if(a.getActionCommand() == "PauseResume") { MobilePlatform.pauseResumeApp(); }

			// else if(a.getActionCommand() == "Exit") { System.exit(0); }

			else if(a.getActionCommand() == "Exit") { cleanupJoysticks(); Mobile.getPlatform().stopApp(); main.dispose(); }

			else if(a.getActionCommand() == "AboutMenu") { awtDialogs[1].setLocationRelativeTo(main); awtDialogs[1].setVisible(true); }

			else if(a.getActionCommand() == "CloseAboutMenu") { awtDialogs[1].setVisible(false); }

			else if(a.getActionCommand() == "ChangeResolution") { awtDialogs[0].setLocationRelativeTo(main); awtDialogs[0].setVisible(true); }

			else if(a.getActionCommand() == "ApplyResChange") 
			{
				if(fileLoaded) /* Only update res if a jar was loaded, or else AWT throws NullPointerException */
				{
					String[] res = resChoice.getItem(resChoice.getSelectedIndex()).split("x");

					config.updateDisplaySize(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
					hasPendingChange = true;
				}
				awtDialogs[0].setVisible(false);
			}

			else if (a.getActionCommand() == "CancelResChange") { awtDialogs[0].setVisible(false); }

			// else if(a.getActionCommand() == "CloseFreeJ2ME") { System.exit(0); }
			else if(a.getActionCommand() == "CloseFreeJ2ME") { cleanupJoysticks(); Mobile.getPlatform().stopApp(); main.dispose(); }

			else if(a.getActionCommand() == "RestartLater") { awtDialogs[3].setVisible(false); }

			else if(a.getActionCommand() == "MapInputs") {
				// Populate Gamepad Button Labels and clear temporary bindings
				newGamepadBindings.clear();
				for (int i = 0; i < actionKeys.length; i++) {
					String gamepadBinding = config.sysSettings.get("input_" + actionKeys[i] + "_Gamepad");
					if (gamepadBinding != null && !gamepadBinding.isEmpty()) {
						gamepadInputButtons[i].setLabel("GP: " + gamepadBinding);
						newGamepadBindings.put("input_" + actionKeys[i] + "_Gamepad", gamepadBinding); // Store initial binding
					} else {
						gamepadInputButtons[i].setLabel("GP: None");
						newGamepadBindings.put("input_" + actionKeys[i] + "_Gamepad", ""); // Store empty binding
					}
				}
				// Also ensure keyboard buttons are up-to-date with current config
				AWTGUI.this.inputKeycodes = config.getInputKeycodes(); // Refresh from config
				System.arraycopy(AWTGUI.this.inputKeycodes, 0, AWTGUI.this.newInputKeycodes, 0, AWTGUI.this.newInputKeycodes.length); // Sync temp copy
				for(int i = 0; i < inputButtons.length; i++) {
					if (i < AWTGUI.this.inputKeycodes.length) { // Added boundary check for robustness
						inputButtons[i].setLabel(KeyEvent.getKeyText(AWTGUI.this.inputKeycodes[i]));
					}
				}
				awtDialogs[4].setVisible(true);
			}

			else if(a.getActionCommand() == "ApplyInputs") 
			{
				// Update local AWTGUI's working copy for keyboard
				System.arraycopy(AWTGUI.this.newInputKeycodes, 0, AWTGUI.this.inputKeycodes, 0, AWTGUI.this.inputKeycodes.length);
				// Update Config's master copy for keyboard
				config.setInputKeycodes(AWTGUI.this.newInputKeycodes); // This should call config.updateAWTInputs() internally

				// Save Gamepad Bindings from our temporary map
				for (java.util.Map.Entry<String, String> entry : newGamepadBindings.entrySet()) {
					config.sysSettings.put(entry.getKey(), entry.getValue());
				}
				config.saveConfig(); // Save changes to sysSettings (including gamepad)
				awtDialogs[4].setVisible(false); 
			}

			else if(a.getActionCommand() == "CancelInputs") { awtDialogs[4].setVisible(false); }

			else if(a.getActionCommand() == "ShowPlayer") 
			{ 
				// Create FreeJ2MEPlayer Dialog instance and show it;
				FreeJ2MEPlayer playerDialog = new FreeJ2MEPlayer(main);
				playerDialog.setLocationRelativeTo(main);
				playerDialog.setVisible(true);
			}
		}
	}

	public void loadJarFile(String jarpath, boolean firstLoad) 
	{
		jarfile = jarpath;
		fileLoaded = true;
		this.firstLoad = firstLoad;
	}

	public MenuBar getMenuBar() { return menuBar; }

	public boolean hasChanged() { return hasPendingChange; }

	public void clearChanged() { hasPendingChange = false; }

	public boolean hasLoadedFile() { return fileLoaded; }

	public void setMainFrame(Frame main) { this.main = main; }

	public String getJarPath() { return jarfile; }

	public boolean hasJustLoaded() { return firstLoad; }

	public void cleanupJoysticks() {
		for (SDL_Joystick joystick : joysticks) { // Use the handle type
			SdlJoystick.SDL_JoystickClose(joystick);
		}
		joysticks.clear();
		Sdl.SDL_QuitSubSystem(SdlSubSystemConst.SDL_INIT_JOYSTICK); // Keep SDL_ prefix for Sdl methods too
		Mobile.log(Mobile.LOG_INFO, "SDL joystick subsystem cleaned up.");
	}

	public void initActionToKeyboardKeyCodeMap() {
		actionToKeyboardKeyCodeMap.clear();
		int[] currentKeycodes = config.getInputKeycodes(); // Get current keyboard mappings
		if (currentKeycodes.length != actionKeys.length) {
			Mobile.log(Mobile.LOG_ERROR, "AWTGUI: Mismatch between actionKeys and inputKeycodes length. Cannot map actions to keycodes.");
			return;
		}
		for (int i = 0; i < actionKeys.length; i++) {
			actionToKeyboardKeyCodeMap.put("input_" + actionKeys[i], currentKeycodes[i]);
		}
	}

	public void processGamepadInput(Component eventSource) {
        System.out.println("DEBUG_GP_ENTRY: processGamepadInput ENTERED");

		if (joysticks == null || joysticks.isEmpty()) {
            System.out.println("DEBUG_GP_ENTRY: processGamepadInput EXIT - joysticks list is null or empty.");
			return;
		}
		if (config == null || config.sysSettings == null) {
			System.err.println("DEBUG_GP_ENTRY: processGamepadInput EXIT - Config or sysSettings is null.");
			return;
		}
        // Assuming initActionToKeyboardKeyCodeMap() is called if map is empty,
        // and it would log if it's still empty.
		if (actionToKeyboardKeyCodeMap == null || actionToKeyboardKeyCodeMap.isEmpty()) {
			initActionToKeyboardKeyCodeMap();
			if (actionToKeyboardKeyCodeMap.isEmpty()){
				 System.err.println("DEBUG_GP_ENTRY: processGamepadInput EXIT - actionToKeyboardKeyCodeMap is STILL EMPTY after re-init.");
				 return;
			}
		}

		// Assuming we are using the first joystick
		io.github.libsdl4j.api.joystick.SDL_Joystick joystick = joysticks.get(0); // Assuming at least one, given prior checks
		if (joystick == null) {
			System.err.println("DEBUG_GP_ENTRY: processGamepadInput EXIT - Joystick object at index 0 is null.");
			return;
		}
        System.out.println("DEBUG_GP_ENTRY: processGamepadInput PASSED INITIAL CHECKS. Joystick name: " + io.github.libsdl4j.api.joystick.SdlJoystick.SDL_JoystickName(joystick));

		// Update joystick states
		io.github.libsdl4j.api.joystick.SdlJoystick.SDL_JoystickUpdate(); // Corrected to SdlJoystick class
		// System.out.println("DEBUG_GP: SDL_JoystickUpdate() called."); // Optional: spammy

		for (String actionKeyBase : actionKeys) { // actionKeys should be {"LeftSoft", "RightSoft", ...}
			String actionGamepadConfigKey = "input_" + actionKeyBase + "_Gamepad";
			String gamepadBindingString = config.sysSettings.get(actionGamepadConfigKey);

			if (gamepadBindingString == null || gamepadBindingString.isEmpty()) {
				// System.out.println("DEBUG_GP: No gamepad binding for action: " + actionKeyBase); // Optional: spammy
				continue;
			}

			// System.out.println("DEBUG_GP: Action: " + actionKeyBase + ", Binding: " + gamepadBindingString); // Optional: spammy

			boolean currentState = false;
			String[] parts = gamepadBindingString.split("_"); // GP0_BUTTON_1, GP0_HAT_0_UP, GP0_AXIS_0_POS

			if (parts.length < 3) {
				System.err.println("DEBUG_GP: Invalid binding string format: " + gamepadBindingString + " for action " + actionKeyBase);
				continue;
			}

			// int gpIndex = Integer.parseInt(parts[0].substring(2)); // Assuming "GP0" -> 0, currently hardcoded to use joysticks.get(0)

			try {
				String type = parts[1];
				int index = Integer.parseInt(parts[2]);

				if ("BUTTON".equals(type)) {
					if (parts.length == 3) { // GP0_BUTTON_1
						byte buttonState = io.github.libsdl4j.api.joystick.SdlJoystick.SDL_JoystickGetButton(joystick, index);
						currentState = (buttonState == 1);
						// System.out.println("DEBUG_GP:  Button " + index + " state: " + buttonState); // Optional
					}
				} else if ("HAT".equals(type)) { // GP0_HAT_0_UP
					if (parts.length == 4) {
						byte hatState = io.github.libsdl4j.api.joystick.SdlJoystick.SDL_JoystickGetHat(joystick, index);
						String direction = parts[3];
						if ("UP".equals(direction)) currentState = (hatState == io.github.libsdl4j.api.joystick.SdlJoystickConst.SDL_HAT_UP);
						else if ("DOWN".equals(direction)) currentState = (hatState == io.github.libsdl4j.api.joystick.SdlJoystickConst.SDL_HAT_DOWN);
						else if ("LEFT".equals(direction)) currentState = (hatState == io.github.libsdl4j.api.joystick.SdlJoystickConst.SDL_HAT_LEFT);
						else if ("RIGHT".equals(direction)) currentState = (hatState == io.github.libsdl4j.api.joystick.SdlJoystickConst.SDL_HAT_RIGHT);
						// System.out.println("DEBUG_GP:  Hat " + index + " state: " + hatState + ", Checking for: " + direction); // Optional
					}
				} else if ("AXIS".equals(type)) { // GP0_AXIS_0_POS
					if (parts.length == 4) {
						short axisValue = io.github.libsdl4j.api.joystick.SdlJoystick.SDL_JoystickGetAxis(joystick, index);
						String direction = parts[3];
						if ("POS".equals(direction)) currentState = (axisValue > AXIS_DEADZONE_THRESHOLD);
						else if ("NEG".equals(direction)) currentState = (axisValue < -AXIS_DEADZONE_THRESHOLD);
						// System.out.println("DEBUG_GP:  Axis " + index + " value: " + axisValue + ", Checking for: " + direction); // Optional
					}
				}
			} catch (NumberFormatException e) {
				System.err.println("DEBUG_GP: Error parsing index for binding: " + gamepadBindingString + " - " + e.getMessage());
				continue;
			} catch (Throwable t) { // Catch any other unexpected errors from SDL calls for this binding
				System.err.println("DEBUG_GP: Unexpected error processing binding " + gamepadBindingString + ": " + t.getMessage());
				t.printStackTrace();
				continue;
			}

			boolean prevState = previousGamepadInputStates.getOrDefault(gamepadBindingString, false);

			if (currentState != prevState) {
				System.out.println("DEBUG_GP: State change for " + actionKeyBase + " (" + gamepadBindingString + "): " + (currentState ? "PRESSED" : "RELEASED"));
				previousGamepadInputStates.put(gamepadBindingString, currentState);

				Integer keyboardKeyCode = actionToKeyboardKeyCodeMap.get("input_" + actionKeyBase); // ensure using "input_" prefix as in initActionToKeyboardKeyCodeMap
				if (keyboardKeyCode != null) {
					int actionIndex = -1;
					for (int i = 0; i < AWTGUI.this.inputKeycodes.length; i++) {
						if (AWTGUI.this.inputKeycodes[i] == keyboardKeyCode) { // Use object equality for Integer if it was Integer object, but VK_ codes are primitive ints.
							actionIndex = i;
							break;
						}
					}

					if (actionIndex != -1) {
						int canvasKeycode = Mobile.convertAWTKeycode(actionIndex);

						if (canvasKeycode != 0 && canvasKeycode != Integer.MIN_VALUE) {
							if (currentState) {
								Mobile.getPlatform().keyPressed(canvasKeycode);
							} else {
								Mobile.getPlatform().keyReleased(canvasKeycode);
							}
							System.out.println("DEBUG_GP: Dispatching KeyEvent for " + actionKeyBase + " (Canvas Key: " + canvasKeycode + "): Type=" + (currentState ? "PRESS" : "RELEASE"));
						} else {
							System.err.println("DEBUG_GP: Invalid Canvas keycode for action " + actionKeyBase + " (AWT VK: " + keyboardKeyCode + ", ActionIndex: " + actionIndex + ")");
						}
					} else {
						System.err.println("DEBUG_GP: Could not find actionIndex for AWT VK: " + keyboardKeyCode + " for action " + actionKeyBase);
					}
				} else {
					System.err.println("DEBUG_GP: No keyboard key code mapping found for action: " + actionKeyBase);
				}
			}
		}
	}
}
