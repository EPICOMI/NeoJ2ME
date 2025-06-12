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

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;
import net.java.games.input.Controller;
import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;


public final class AWTGUI 
{
	final String VERSION = "1.45";
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
	private List<Controller> detectedGamepads;
	private Controller selectedGamepadController;
	private volatile boolean isCapturingInput = false;
	private Button currentMappingButton = null;
	private String actionToMap = null;
	private Map<String, String> currentGamepadMappings = new HashMap<>(); // J2MEActionName -> JInputComponentID


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
		new Dialog(main , "Set LCD Resolution", true), // 0
		new Dialog(main , "About FreeJ2ME", true),     // 1
		new Dialog(main, "FreeJ2ME MemStat", false),  // 2
		new Dialog(main, "Restart Required", true),   // 3
		new Dialog(main, "Key Mapping", true),        // 4
	};
	
	final Button[] awtButtons = 
	{
		new Button("Close"),          // 0 About
		new Button("Apply"),          // 1 Res
		new Button("Cancel"),         // 2 Res
		new Button("Close FreeJ2ME"), // 3 Restart
		new Button("Restart later"),  // 4 Restart
		new Button("Apply Inputs"),   // 5 Mapping
		new Button("Cancel")          // 6 Mapping
	};
	
	// Gamepad UI Elements
	private Choice gamepadSelectionChoice;
	private Map<String, Button> gamepadMappingButtons = new HashMap<>();
	private String[] gamepadActionsToMap = {
		"UP", "DOWN", "LEFT", "RIGHT", "FIRE",
		"GAME_A", "GAME_B", "GAME_C", "GAME_D",
		"SOFT_LEFT", "SOFT_RIGHT", "STAR", "POUND"
		// "NUM0", "NUM1", ... "NUM9" // Optional: if direct numkey mapping is desired
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
	int inputKeycodes[] = new int[] { 
		KeyEvent.VK_Q, KeyEvent.VK_W, 
		KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_ENTER, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, 
		KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9, 
		KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, 
		KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3, 
		KeyEvent.VK_E, KeyEvent.VK_NUMPAD0, KeyEvent.VK_R, KeyEvent.VK_SPACE, KeyEvent.VK_C, KeyEvent.VK_X
	};

	private final int newInputKeycodes[] = Arrays.copyOf(inputKeycodes, inputKeycodes.length);

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


	public AWTGUI(Config config, List<Controller> gamepads)
	{
		this.config = config;
		this.detectedGamepads = gamepads != null ? gamepads : new ArrayList<>();

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
		awtDialogs[4].setLayout(new java.awt.BorderLayout()); // Main layout for the dialog
		awtDialogs[4].setSize(500, 700); // Adjusted size
		awtDialogs[4].setLocationRelativeTo(main);
		awtDialogs[4].setResizable(true);
		

		// Panel for Keyboard Mappings (Top)
		java.awt.Panel keyboardPanel = new java.awt.Panel(new GridLayout(0, 3, 5, 5)); // rows, cols, hgap, vgap
		keyboardPanel.setBackground(FreeJ2ME.freeJ2MEBGColor);

		// Setup input button colors
		awtButtons[5].setBackground(FreeJ2ME.freeJ2MEDragColor); // Apply Inputs (now for both)
		awtButtons[5].setForeground(Color.GREEN);

		awtButtons[6].setBackground(FreeJ2ME.freeJ2MEDragColor);
		awtButtons[6].setForeground(Color.RED);

		for(int i = 0; i < inputButtons.length; i++) 
		{ 
			inputButtons[i].setBackground(FreeJ2ME.freeJ2MEDragColor);
			inputButtons[i].setForeground(Color.ORANGE);
		}

		awtDialogs[4].add(new Label("Map keys by"));
		awtDialogs[4].add(new Label("clicking each"));
		awtDialogs[4].add(new Label("button below"));

		awtDialogs[4].add(awtButtons[5]);
		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(awtButtons[6]);

		awtDialogs[4].add(new Label("-----------------------"));
		awtDialogs[4].add(new Label("-----------------------"));
		awtDialogs[4].add(new Label("-----------------------"));

		awtDialogs[4].add(inputButtons[0]);
		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(inputButtons[1]);

		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(inputButtons[2]);
		awtDialogs[4].add(new Label(""));

		awtDialogs[4].add(inputButtons[3]);
		awtDialogs[4].add(inputButtons[4]);
		awtDialogs[4].add(inputButtons[5]);

		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(inputButtons[6]);
		awtDialogs[4].add(new Label(""));

		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(new Label(""));
		
		awtDialogs[4].add(inputButtons[7]);
		awtDialogs[4].add(inputButtons[8]);
		awtDialogs[4].add(inputButtons[9]);
		
		awtDialogs[4].add(inputButtons[10]);
		awtDialogs[4].add(inputButtons[11]);
		awtDialogs[4].add(inputButtons[12]);

		awtDialogs[4].add(inputButtons[13]);
		awtDialogs[4].add(inputButtons[14]);
		awtDialogs[4].add(inputButtons[15]);

		awtDialogs[4].add(inputButtons[16]);
		awtDialogs[4].add(inputButtons[17]);
		awtDialogs[4].add(inputButtons[18]);

		awtDialogs[4].add(new Label("-----------------------"));
		awtDialogs[4].add(new Label("-----------------------"));
		awtDialogs[4].add(new Label("-----------------------"));

		awtDialogs[4].add(new Label("Hotkeys"));
		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(new Label("(Ctrl + *)"));

		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(new Label(""));
		
		awtDialogs[4].add(new Label("Fast-Forward"));
		awtDialogs[4].add(new Label("Screenshot"));
		awtDialogs[4].add(new Label("Pause/Resume"));

		awtDialogs[4].add(inputButtons[19]);
		awtDialogs[4].add(inputButtons[20]);
		awtDialogs[4].add(inputButtons[21]);

		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(new Label(""));
		awtDialogs[4].add(new Label(""));
		
		awtDialogs[4].add(new Label("Slowdown"));
		keyboardPanel.add(new Label("Map keys by"));
		keyboardPanel.add(new Label("clicking each"));
		keyboardPanel.add(new Label("button below"));

		// keyboardPanel.add(awtButtons[5]); // Apply and Cancel buttons will be at the bottom of the main dialog
		// keyboardPanel.add(new Label(""));
		// keyboardPanel.add(awtButtons[6]);

		keyboardPanel.add(new Label("-----------------------"));
		keyboardPanel.add(new Label("--- Keyboard ---"));
		keyboardPanel.add(new Label("-----------------------"));

		// Adding existing keyboard inputButtons to keyboardPanel
		keyboardPanel.add(inputButtons[0]); keyboardPanel.add(new Label("")); keyboardPanel.add(inputButtons[1]);
		keyboardPanel.add(new Label("")); keyboardPanel.add(inputButtons[2]); keyboardPanel.add(new Label(""));
		keyboardPanel.add(inputButtons[3]); keyboardPanel.add(inputButtons[4]); keyboardPanel.add(inputButtons[5]);
		keyboardPanel.add(new Label("")); keyboardPanel.add(inputButtons[6]); keyboardPanel.add(new Label(""));
		keyboardPanel.add(new Label("")); keyboardPanel.add(new Label("")); keyboardPanel.add(new Label(""));
		keyboardPanel.add(inputButtons[7]); keyboardPanel.add(inputButtons[8]); keyboardPanel.add(inputButtons[9]);
		keyboardPanel.add(inputButtons[10]); keyboardPanel.add(inputButtons[11]); keyboardPanel.add(inputButtons[12]);
		keyboardPanel.add(inputButtons[13]); keyboardPanel.add(inputButtons[14]); keyboardPanel.add(inputButtons[15]);
		keyboardPanel.add(inputButtons[16]); keyboardPanel.add(inputButtons[17]); keyboardPanel.add(inputButtons[18]);
		keyboardPanel.add(new Label("-----------------------"));
		keyboardPanel.add(new Label("--- Hotkeys ---"));
		keyboardPanel.add(new Label("-----------------------"));
		keyboardPanel.add(new Label("Fast-Forward")); keyboardPanel.add(new Label("Screenshot")); keyboardPanel.add(new Label("Pause/Resume"));
		keyboardPanel.add(inputButtons[19]); keyboardPanel.add(inputButtons[20]); keyboardPanel.add(inputButtons[21]);
		// keyboardPanel.add(new Label("")); keyboardPanel.add(new Label("")); keyboardPanel.add(new Label(""));
		// keyboardPanel.add(new Label("Slowdown")); keyboardPanel.add(new Label("TODO")); keyboardPanel.add(new Label("TODO"));
		// keyboardPanel.add(new Label("TODO")); keyboardPanel.add(new Label("")); keyboardPanel.add(new Label(""));

		awtDialogs[4].add(keyboardPanel, java.awt.BorderLayout.NORTH);

		// Panel for Gamepad Mappings (Center)
		java.awt.Panel gamepadPanel = new java.awt.Panel(new java.awt.BorderLayout(5,5));
		gamepadPanel.setBackground(FreeJ2ME.freeJ2MEBGColor);

		java.awt.Panel gamepadSelectionPanel = new java.awt.Panel(new FlowLayout(FlowLayout.LEFT));
		gamepadSelectionPanel.setBackground(FreeJ2ME.freeJ2MEBGColor);
		gamepadSelectionChoice = new Choice();
		gamepadSelectionChoice.add("None (Keyboard Only)");
		if (this.detectedGamepads != null) {
			for (Controller controller : this.detectedGamepads) {
				gamepadSelectionChoice.add(controller.getName());
			}
		}
		gamepadSelectionChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String selectedName = gamepadSelectionChoice.getSelectedItem();
					if ("None (Keyboard Only)".equals(selectedName)) {
						selectedGamepadController = null;
						for (Button btn : gamepadMappingButtons.values()) {
							btn.setLabel("N/A");
							btn.setEnabled(false);
						}
					} else {
						for (Controller c : detectedGamepads) {
							if (c.getName().equals(selectedName)) {
								selectedGamepadController = c;
								for (Button btn : gamepadMappingButtons.values()) {
									btn.setEnabled(true);
								}
								loadGamepadMappingsForSelectedController();
								break;
							}
						}
					}
				}
			}
		});
		gamepadSelectionPanel.add(new Label("Select Gamepad:"));
		gamepadSelectionPanel.add(gamepadSelectionChoice);
		gamepadPanel.add(gamepadSelectionPanel, java.awt.BorderLayout.NORTH);

		java.awt.Panel gamepadButtonGrid = new java.awt.Panel(new GridLayout(0, 3, 5, 5)); // Grid for mapping buttons
		gamepadButtonGrid.setBackground(FreeJ2ME.freeJ2MEBGColor);
		for (String action : gamepadActionsToMap) {
			gamepadButtonGrid.add(new Label(action + ":"));
			Button mapBtn = new Button("Press to Map");
			mapBtn.setActionCommand("MapGamepad_" + action);
			mapBtn.addActionListener(menuItemListener);
			gamepadMappingButtons.put(action, mapBtn);
			gamepadButtonGrid.add(mapBtn);
			// gamepadButtonGrid.add(new Label("")); // Optional: if a 3rd column for status is desired
		}
		gamepadPanel.add(gamepadButtonGrid, java.awt.BorderLayout.CENTER);

		boolean enableGamepadUI = (selectedGamepadController != null);
		if (detectedGamepads.isEmpty()) enableGamepadUI = false; // No gamepads, disable UI

		for (Button btn : gamepadMappingButtons.values()) {
			btn.setEnabled(enableGamepadUI);
			if (!enableGamepadUI) btn.setLabel("N/A");
		}
		if (detectedGamepads.isEmpty()) {
		    gamepadSelectionChoice.select("None (Keyboard Only)");
		    gamepadSelectionChoice.setEnabled(false);
		}


		awtDialogs[4].add(gamepadPanel, java.awt.BorderLayout.CENTER);

		// Panel for Apply/Cancel buttons (South)
		java.awt.Panel bottomButtonPanel = new java.awt.Panel(new FlowLayout(FlowLayout.CENTER));
		bottomButtonPanel.setBackground(FreeJ2ME.freeJ2MEBGColor);
		bottomButtonPanel.add(awtButtons[5]); // Apply Inputs
		bottomButtonPanel.add(awtButtons[6]); // Cancel Inputs
		awtDialogs[4].add(bottomButtonPanel, java.awt.BorderLayout.SOUTH);


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
		awtButtons[5].setActionCommand("ApplyInputs"); // Apply (Keyboard & Gamepad)
		awtButtons[6].setActionCommand("CancelInputs"); // Cancel (Keyboard & Gamepad)

		showPlayer.setActionCommand("ShowPlayer");
		
		// Add action listeners for new gamepad mapping buttons via UIListener
		// The action commands are set like "MapGamepad_UP", "MapGamepad_FIRE" etc.

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
			inputKeycodes = config.inputKeycodes;
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

			else if(a.getActionCommand() == "Exit") { Mobile.getPlatform().stopApp(); main.dispose(); }

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
			else if(a.getActionCommand() == "CloseFreeJ2ME") { Mobile.getPlatform().stopApp(); main.dispose(); }

			else if(a.getActionCommand() == "RestartLater") { awtDialogs[3].setVisible(false); }

			else if(a.getActionCommand() == "MapInputs") { awtDialogs[4].setVisible(true); }

			else if(a.getActionCommand() == "ApplyInputs") 
			{
				// Save Keyboard Mappings
				System.arraycopy(newInputKeycodes, 0, inputKeycodes, 0, inputKeycodes.length);
				config.updateAWTInputs(); // This saves keyboard inputKeycodes to config.settings

				// Save Gamepad Mappings
				if (selectedGamepadController != null && currentGamepadMappings != null) {
					config.setGamepadMappings(selectedGamepadController.getName(), currentGamepadMappings);
					// config.save() is called within setGamepadMappings
					Mobile.log(Mobile.LOG_INFO, "Gamepad mappings for " + selectedGamepadController.getName() + " applied and saved.");
				} else if (selectedGamepadController == null && currentGamepadMappings != null && !currentGamepadMappings.isEmpty()) {
					// This case should ideally not happen if UI logic is correct (no controller selected, but mappings changed)
					Mobile.log(Mobile.LOG_WARNING, "Attempted to save gamepad mappings but no gamepad was selected. Mappings not saved.");
				}
				awtDialogs[4].setVisible(false); 
			}

			else if(a.getActionCommand() == "CancelInputs")
			{
				// Reset any temporarily changed mappings to what's in config by reloading them
				if (selectedGamepadController != null) {
					loadGamepadMappingsForSelectedController(); // This will reload from config, discarding changes in currentGamepadMappings
				} else {
					// If no gamepad is selected, clear any transient mappings
					currentGamepadMappings.clear();
					for (String action : gamepadActionsToMap) {
						Button btn = gamepadMappingButtons.get(action);
						if (btn != null) {
							btn.setLabel("N/A");
							btn.setEnabled(false);
						}
					}
				}
				awtDialogs[4].setVisible(false);
			}
			else if (a.getActionCommand() != null && a.getActionCommand().startsWith("MapGamepad_"))
			{
				if (selectedGamepadController == null) {
					Mobile.log(Mobile.LOG_WARNING, "No gamepad selected for mapping.");
					return;
				}
				actionToMap = a.getActionCommand().substring("MapGamepad_".length());
				currentMappingButton = (Button)a.getSource();
				currentMappingButton.setLabel("Waiting...");
				isCapturingInput = true;
				startCapturingGamepadInput(); // Needs implementation
			}
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

	private void loadGamepadMappingsForSelectedController() {
		if (selectedGamepadController == null) {
			// No controller selected, clear/disable mapping buttons
			this.currentGamepadMappings.clear();
			for (String action : gamepadActionsToMap) {
				Button btn = gamepadMappingButtons.get(action);
				if (btn != null) {
					btn.setLabel("N/A");
					btn.setEnabled(false);
				}
			}
			return;
		}

		String gamepadName = selectedGamepadController.getName();
		Map<String, String> mappings = config.getGamepadMappings(gamepadName); // J2MEActionName -> JInputComponentID

		Mobile.log(Mobile.LOG_INFO, "Loading mappings for: " + gamepadName);

		for (String action : gamepadActionsToMap) {
			Button btn = gamepadMappingButtons.get(action);
			if (btn != null) {
				String mappedComponentId = mappings.getOrDefault(action, "Press to Map");
				btn.setLabel(mappedComponentId);
				btn.setEnabled(true); // Ensure button is enabled when a controller is selected
			}
		}
		this.currentGamepadMappings = new HashMap<>(mappings); // Keep a working copy for edits
	}

	private void startCapturingGamepadInput() {
		if (selectedGamepadController == null || actionToMap == null || currentMappingButton == null) {
			isCapturingInput = false;
			return;
		}
		Mobile.log(Mobile.LOG_INFO, "AWTGUI: Waiting for input for " + actionToMap + " on " + selectedGamepadController.getName() + "...");

		// Disable other mapping buttons
		for (Button btn : gamepadMappingButtons.values()) {
			if (btn != currentMappingButton) {
				btn.setEnabled(false);
			}
		}
		// Also disable keyboard mapping buttons
		for (Button btn : inputButtons) {
			btn.setEnabled(false);
		}


		new Thread(() -> {
			long startTime = System.currentTimeMillis();
			final long TIMEOUT_MS = 5000; // 5 seconds timeout
			final float AXIS_THRESHOLD = 0.2f; // Minimum deflection for an axis to be considered "active"

			boolean inputCaptured = false;

			while (isCapturingInput && (System.currentTimeMillis() - startTime) < TIMEOUT_MS) {
				selectedGamepadController.poll();
				EventQueue queue = selectedGamepadController.getEventQueue();
				Event event = new Event();

				while (queue.getNextEvent(event)) {
					Component comp = event.getComponent();
					float value = event.getValue();
					Component.Identifier id = comp.getIdentifier();
					String compIdName = id.getName(); // This is what we store (e.g., "0", "x", "pov")
					String compDisplayName = comp.getName(); // Human-readable (e.g., "Button 0", "X Axis")

					// Determine if this event is significant enough for mapping
					boolean significantEvent = false;
					if (id instanceof Component.Identifier.Button) {
						if (value == 1.0f) { // Only map button presses, not releases
							significantEvent = true;
						}
					} else if (id == Component.Identifier.Axis.POV) {
						if (value != POV.OFF && value != POV.CENTER) { // Any POV direction change from center
							significantEvent = true;
							// For POV, we map the "pov" component itself, not its specific value here.
							// The direction is interpreted later during event processing in MobilePlatform.
						}
					} else if (comp.isAnalog()) { // Other analog axes
						if (Math.abs(value) > AXIS_THRESHOLD) {
							significantEvent = true;
							// For axes, we map the axis component itself (e.g., "x", "y").
							// The direction/threshold is interpreted later in MobilePlatform.
						}
					} else if (id instanceof Component.Identifier.Key) { // Gamepad may have actual keys
					    significantEvent = true; // Treat as a button press
					}


					if (significantEvent) {
						final String finalCompIdName = compIdName;
						final String finalCompDisplayName = compDisplayName;
						final String actionBeingMapped = actionToMap;

						java.awt.EventQueue.invokeLater(() -> {
							if (currentMappingButton != null && actionBeingMapped != null) {
								currentMappingButton.setLabel(finalCompDisplayName + " (" + finalCompIdName + ")");
								currentGamepadMappings.put(actionBeingMapped, finalCompIdName); // Store the JInput component ID name
								Mobile.log(Mobile.LOG_INFO, "AWTGUI: Mapped " + actionBeingMapped + " to " + finalCompDisplayName + " (ID: " + finalCompIdName + ")");
							}
						});
						inputCaptured = true;
						break; // Exit event loop
					}
				} // end while(queue.getNextEvent)

				if (inputCaptured) {
					break; // Exit polling loop
				}

				try {
					Thread.sleep(50); // Poll moderately frequently
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			} // end while(isCapturingInput && !timeout)

			// Re-enable buttons on EDT
			java.awt.EventQueue.invokeLater(() -> {
				if (!inputCaptured && currentMappingButton != null) { // Timeout or cancelled
					currentMappingButton.setLabel(currentGamepadMappings.getOrDefault(actionToMap, "Press to Map")); // Revert to previous or default
					Mobile.log(Mobile.LOG_INFO, "AWTGUI: Input capture for " + actionToMap + " timed out or was cancelled.");
				}
				isCapturingInput = false; // Crucial to reset state
				// currentMappingButton = null; // Don't nullify, it's needed if user clicks again
				// actionToMap = null;

				for (Button btn : gamepadMappingButtons.values()) {
					if (selectedGamepadController != null) { // Only enable if a gamepad is still selected
						btn.setEnabled(true);
					} else {
						btn.setLabel("N/A");
						btn.setEnabled(false);
					}
				}
				for (Button btn : inputButtons) {
					btn.setEnabled(true); // Re-enable keyboard mapping buttons
				}
				// If no gamepad is selected after all this, ensure labels are N/A
				if (selectedGamepadController == null) {
				    loadGamepadMappingsForSelectedController(); // This will set labels to N/A and disable
				}
			});

		}).start();
	}
}
