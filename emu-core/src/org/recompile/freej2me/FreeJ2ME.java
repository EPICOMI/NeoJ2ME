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
// package org.recompile.freej2me;

/*
	FreeJ2ME - AWT
*/

/*
import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FreeJ2ME
{

	public static FreeJ2ME app;
	private Frame main;
	private int lcdWidth;
	private int lcdHeight;
	private int scaleFactor = 1;

	private MobilePlatform platform; // Store platform for cleanup
	private boolean isShuttingDown = false; //Prevent reentrant shutdown

	public static Color freeJ2MEBGColor = new Color(0,0,64);
	public static Color freeJ2MEDragColor = new Color(55, 55, 125);

	public static boolean isFullscreen = false;

	private LCD lcd;

	private int xborder;
	private int yborder;

	// AWT GUI
	private AWTGUI awtGUI;

	public static void main(String args[])
	{
		Mobile.clearOldLog();
		FreeJ2ME.app = new FreeJ2ME(args);
	}

	public FreeJ2ME(String args[]) {
		// Setup Device //

		if (args.length >= 3) {
			Mobile.lcdWidth = Integer.parseInt(args[1]);
			Mobile.lcdHeight = Integer.parseInt(args[2]);
		}
		if (args.length >= 4) {
			scaleFactor = Integer.parseInt(args[3]);
		}

		lcdWidth = Mobile.lcdWidth;
		lcdHeight = Mobile.lcdHeight;

		Mobile.setPlatform(new MobilePlatform(lcdWidth, lcdHeight));

		lcd = new LCD();
		lcd.setFocusable(true);

		Mobile.config = new Config();
		Mobile.config.onChange = new Runnable() {
			public void run() {
				settingsChanged();
			}
		};

		awtGUI = new AWTGUI(Mobile.config);

		constructFreeJ2MEGUI();

		if (args.length >= 1) // Only now we can load the jar passed as argument
		{
			awtGUI.loadJarFile(getFormattedLocation(args[0]), true);
		}

	 // Inputs should only be registered if a jar has been loaded, otherwise AWT will throw NullPointerException
		lcd.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (awtGUI.hasLoadedFile()) {
					int keycode = e.getKeyCode();
					int mobikey = getMobileKey(keycode);

					switch (keycode) // Handle emulator control keys
					{
						case KeyEvent.VK_PLUS:
						case KeyEvent.VK_ADD:
							if (!isFullscreen) {
								scaleFactor++;
								main.setSize(lcdWidth * scaleFactor + xborder, lcdHeight * scaleFactor + yborder);
							}
							break;
						case KeyEvent.VK_MINUS:
						case KeyEvent.VK_SUBTRACT:
							if (scaleFactor > 1 && !isFullscreen) {
								scaleFactor--;
								main.setSize(lcdWidth * scaleFactor + xborder, lcdHeight * scaleFactor + yborder);
							}
							break;
						case KeyEvent.VK_F:
							if (e.isControlDown()) {
								toggleFullscreen();
							}
							break;
					}

					if (mobikey == Integer.MIN_VALUE) // Ignore events from keys not mapped to a phone keypad key (AWTGUI does use 0, so this can't mirror libretro)
					{
						return;
					}

					if (MobilePlatform.pressedKeys[mobikey] == false) {
						if (mobikey < 19) // Anything over 19 are special keys (fast-forward, etc)
						{
							MobilePlatform.pressedKeys[mobikey] = true;
							MobilePlatform.keyPressed(Mobile.getMobileKey(mobikey));
						} else {
							if (e.isControlDown()) {
								MobilePlatform.pressedKeys[mobikey] = true;
							}
						}
					} else {
						if (mobikey < 19) {
							MobilePlatform.keyRepeated(Mobile.getMobileKey(mobikey));
						}
					}
				}
			}

			public void keyReleased(KeyEvent e) {
				if (awtGUI.hasLoadedFile()) {
					int mobikey = getMobileKey(e.getKeyCode());

					if (mobikey == Integer.MIN_VALUE) // Ignore events from keys not mapped to a phone keypad key (AWTGUI does use 0, so this can't mirror libretro)
					{
						return;
					}

					MobilePlatform.pressedKeys[mobikey] = false;
					MobilePlatform.keyReleased(Mobile.getMobileKey(mobikey));

					if (mobikey == 20) {
						ScreenShot.takeScreenshot(false);
					} else if (mobikey == 21) {
						MobilePlatform.pauseResumeApp();
					}

					for (int i = 0; i < MobilePlatform.pressedKeys.length; i++) {
						if (MobilePlatform.pressedKeys[i]) {
							MobilePlatform.keyRepeated(Mobile.getMobileKey(i));
						}
					}
				}
			}

			public void keyTyped(KeyEvent e) {
			}

		});

		lcd.addMouseListener(new MouseListener() {

			public void mousePressed(MouseEvent e) {
				if (awtGUI.hasLoadedFile()) {
					int x = (int) ((e.getX() - lcd.cx) * lcd.scalex);
					int y = (int) ((e.getY() - lcd.cy) * lcd.scaley);

					// Adjust the pointer coords if the screen is rotated, same for mouseReleased
					if (Mobile.rotateDisplay) {
						x = (int) ((lcd.ch - (e.getY() - lcd.cy)) * lcd.scaley);
						y = (int) ((e.getX() - lcd.cx) * lcd.scalex);
					}

					MobilePlatform.pointerPressed(x, y);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (awtGUI.hasLoadedFile()) {
					int x = (int) ((e.getX() - lcd.cx) * lcd.scalex);
					int y = (int) ((e.getY() - lcd.cy) * lcd.scaley);

					if (Mobile.rotateDisplay) {
						x = (int) ((lcd.ch - (e.getY() - lcd.cy)) * lcd.scaley);
						y = (int) ((e.getX() - lcd.cx) * lcd.scalex);
					}

					MobilePlatform.pointerReleased(x, y);
				}
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}

		});

		lcd.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (awtGUI.hasLoadedFile()) {
					int x = (int) ((e.getX() - lcd.cx) * lcd.scalex);
					int y = (int) ((e.getY() - lcd.cy) * lcd.scaley);

					if (Mobile.rotateDisplay) {
						x = (int) ((lcd.ch - (e.getY() - lcd.cy)) * lcd.scaley);
						y = (int) ((e.getX() - lcd.cx) * lcd.scalex);
					}

					MobilePlatform.pointerDragged(x, y);
				}
			}
		});

		displayGUI();

		// Set painter right before the jar is loaded
		Mobile.getPlatform().setPainter(new Runnable() {
			public void run() {
				// Set menuBar option states based on loaded config
				if (awtGUI.hasJustLoaded()) {
					awtGUI.updateOptions();
				}

				// Only update mem dialog's stats if it is visible
				if (awtGUI.awtDialogs[2].isVisible()) {
					awtGUI.updateMemStatDialog();
				}

				// Whenever AWT GUI notifies that its menu options were changed, update settings
				if (awtGUI.hasChanged()) {
					settingsChanged();
					awtGUI.clearChanged();
				}

				if (lcd != null) {
					lcd.repaint();
				}
			}
		});


		if(args.length<1)
		{
			while(!awtGUI.hasLoadedFile())
			{
				try{ Thread.sleep(1000); }
				catch (InterruptedException e) { }
			}
		}
		if(Mobile.getPlatform().load(awtGUI.getJarPath()))
		{
			Mobile.config.init();

			// Allows FreeJ2ME to set the width and height passed as cmd arguments.
			if(args.length>=3)
			{
				lcdWidth = Integer.parseInt(args[1]);
				lcdHeight = Integer.parseInt(args[2]);
				Mobile.config.settings.put("width",  ""+lcdWidth);
				Mobile.config.settings.put("height", ""+lcdHeight);
			}

			settingsChanged();

			Mobile.getPlatform().runJar();

		}
		else
		{
			Mobile.log(Mobile.LOG_ERROR, FreeJ2ME.class.getPackage().getName() + "." + FreeJ2ME.class.getSimpleName() + ": " + "Couldn't load jar...");
			shutdown(); // Close if load fails
		}

	}

	private static String getFormattedLocation(String loc)
	{
		if (loc == null || loc.isEmpty()) {
			return ""; // Allow empty path to show GUI
		}
		if (loc.startsWith("file://") || loc.startsWith("http://") || loc.startsWith("https://"))
			return loc;

		File file = new File(loc);
		if(! file.isFile())
		{
			Mobile.log(Mobile.LOG_ERROR, FreeJ2ME.class.getPackage().getName() + "." + FreeJ2ME.class.getSimpleName() + ": " + "File not found...");
			return "";
			//System.exit(0);
		}

		return file.toURI().toString();
	}

	private void settingsChanged()
	{
		boolean hasRotated = Mobile.updateSettings();

		// Create a standard size LCD if not rotated, else invert window's width and height.
		if(Mobile.lcdWidth != lcdWidth || Mobile.lcdHeight != lcdHeight || hasRotated) 
		{
			Mobile.getPlatform().resizeLCD(Mobile.lcdWidth, Mobile.lcdHeight);

			if(!Mobile.rotateDisplay)
			{
				lcdWidth = Mobile.lcdWidth;
				lcdHeight = Mobile.lcdHeight;
			}
			else 
			{
				lcdWidth = Mobile.lcdHeight;
				lcdHeight = Mobile.lcdWidth;
			}
			resize();
			if(!isFullscreen) { main.setSize(lcdWidth*scaleFactor+xborder , lcdHeight*scaleFactor+yborder); }
			lcd.clearScreen();
		}
		
	}

	// Added shutdown() for preventing re-entry calls
	public void shutdown()
	{
		if (isShuttingDown) {
			return; // Prevent reentrant calls
		}
		isShuttingDown = true;
		Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Initiating shutdown...");
		try {
			if (platform != null) {
				Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Stopping game...");
				platform.stopApp();
				Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Resetting platform...");
				platform.reset();
				Mobile.setPlatform(null);
				platform = null;
			}
		} catch (Exception e) {
			Mobile.log(Mobile.LOG_ERROR, "FreeJ2ME: Cleanup failed: " + e.getMessage());
		}
		Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Clearing resources...");
		awtGUI = null;
		lcd = null;
		if (main != null) {
			Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Disposing frame...");
			main.dispose();
			main = null;
		}
		System.gc();
		Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Shutdown complete.");
		isShuttingDown = false;
	}

	private int getMobileKey(int keycode)
	{
		for(int i = 0; i < awtGUI.inputKeycodes.length; i++) 
		{
			if(keycode == awtGUI.inputKeycodes[i]) { return Mobile.convertAWTKeycode(i);}
		}
		return Integer.MIN_VALUE;
	}

	private void resize()
	{
		xborder = main.getInsets().left+main.getInsets().right;
		yborder = main.getInsets().top+main.getInsets().bottom;

		double vw = (main.getWidth()-xborder)*1;
		double vh = (main.getHeight()-yborder)*1;

		double nw = lcdWidth;
		double nh = lcdHeight;

		nw = vw;
		nh = nw*((double)lcdHeight/(double)lcdWidth);

		if(nh>vh)
		{
			nh = vh;
			nw = nh*((double)lcdWidth/(double)lcdHeight);
		}

		lcd.updateScale((int)nw, (int)nh);
	}

	public void toggleFullscreen() 
	{
        isFullscreen = !isFullscreen;
		main.dispose();
		constructFreeJ2MEGUI();
		displayGUI();
    }

	private void constructFreeJ2MEGUI()
	{
		main = new Frame("FreeJ2ME-Plus");

		if (isFullscreen) 
		{
            main.setUndecorated(true);
            main.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        } 
		else 
		{
            main.setSize(350, 450);
            main.setMinimumSize(new Dimension(240, 240));
			main.setLocationRelativeTo(null); // Center window on screen
        }

		main.setBackground(freeJ2MEBGColor);
		
		try
		{
			main.setIconImage(ImageIO.read(main.getClass().getResourceAsStream("/org/recompile/icon.png")));	
		}
		catch (Exception e) { }

		main.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				// System.exit(0);
				//Mobile.getPlatform().stopApp(); // Stop the game
				//main.dispose(); //Dispose instead of exiting
				Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Stopping game...");
				platform.stopApp(); // Stop the game
				//Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Resetting platform...");
				//Mobile.getPlatform().restart(); // Reset platform state
				Mobile.setPlatform(null); // Clear platform reference
				Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Disposing frame...");
				main.dispose(); // Dispose the frame
				shutdown();
			}
		});

		// Add LCD screen to FreeJ2ME's AWT frame
		main.add(lcd);

		awtGUI.setMainFrame(main);
		// Append the awt menu bar into FreeJ2ME's frame
		if(!isFullscreen) { main.setMenuBar(awtGUI.getMenuBar()); }
	}

	private void displayGUI() 
	{
		main.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				resize();
			}
		});

		main.setVisible(true);
		main.pack();
		resize();
		if(!isFullscreen) { main.setSize(lcdWidth*scaleFactor+xborder, lcdHeight*scaleFactor+yborder); }
	}

	private class LCD extends Canvas
	{
		private boolean showDragMessage = false, fileSupported = true;
		public int cx=0;
		public int cy=0;
		public int cw=240;
		public int ch=320;

		public double scalex=1;
		public double scaley=1;

		public LCD() { setDropTarget(); }

		public void updateScale(int vw, int vh)
		{
			cx = (this.getWidth()-vw)/2;
			cy = (this.getHeight()-vh)/2;
			cw = vw;
			ch = vh;
			scalex = (double)lcdWidth/(double)vw;
			scaley = (double)lcdHeight/(double)vh;
		}

		@Override
        public void update(Graphics g) {
            // Use paint method directly to avoid flicker
            paint(g);
        }

		// Used to clear the entire framebuffer when rotated in fullscreen to remove garbage pixels
		public void clearScreen() 
		{
			Graphics2D cgc = (Graphics2D) this.getGraphics();
			cgc.clearRect(0, 0, getWidth(), getHeight());
		}

		public void paint(Graphics g)
		{
			try {
				MobilePlatform p = Mobile.getPlatform();
				if (p != null && p.screenBuffer != null) {
					g.drawImage(p.screenBuffer, 0, 0, this);
				}
			} catch (Exception e) {
				Mobile.log(Mobile.LOG_ERROR, "LCD: Paint failed: " + e.getMessage());
			}

			if(!showDragMessage) 
			{
				if(!Mobile.rotateDisplay) { g.drawImage(Mobile.getPlatform().getLCD(), cx, cy, cw, ch, null); }
				else
				{
					final Graphics2D cgc = (Graphics2D)this.getGraphics();
					// Rotate the FB 90 degrees counterclockwise with an adjusted pivot
					cgc.rotate(Math.toRadians(-90), ch/2, ch/2);
					// Draw the rotated FB with adjusted cy and cx values
					cgc.drawImage(Mobile.getPlatform().getLCD(), 0, cx, ch, cw, null);
				}
				if(MobilePlatform.isPaused) 
				{ 
					g.setColor(new Color(0, 0, 0, 160));
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setFont(new Font("Dialog", Font.BOLD, cw/5));
					g.setColor(Color.WHITE);
					String message = "PAUSED!";
					FontMetrics metrics = g.getFontMetrics();
					int x = (getWidth() - metrics.stringWidth(message)) / 2;
					int y = (getHeight() + metrics.getAscent()) / 2;
					g.drawString(message, x, y);
				}
				else if (MobilePlatform.pressedKeys[19]) // Check if fast-forward is active
				{
					g.setFont(new Font("Dialog", Font.BOLD, cw/2));
					g.setColor(Color.WHITE);
					String fastForwardIndicator = "»";
					FontMetrics ffMetrics = g.getFontMetrics();
					int ffX = (getWidth() - ffMetrics.stringWidth(fastForwardIndicator)) / 2;
					int ffY = (getHeight() + ffMetrics.getAscent()) / 2;
					g.drawString(fastForwardIndicator, ffX, ffY);
				}
			}
			else 
			{
				super.paint(g);
				g.setColor(freeJ2MEDragColor);
				g.fillRect(cx, cy, cw, ch);
				g.setFont(new Font("Dialog", Font.BOLD, 20));
				g.setColor(fileSupported ? Color.WHITE : Color.RED);
				String message = fileSupported ? ">> DROP HERE <<" : "INVALID FILE TYPE!!!";
				FontMetrics metrics = g.getFontMetrics();
				int x = (getWidth() - metrics.stringWidth(message)) / 2;
				int y = (getHeight() / 2);
				g.drawString(message, x, y);
			}
		}

		private void setDropTarget() 
		{
			new DropTarget(this, new DropTargetListener() 
			{
				@Override
				@SuppressWarnings("unchecked")
				public void dragEnter(DropTargetDragEvent dtde) 
				{
					if(!awtGUI.hasLoadedFile()) 
					{
						try 
						{
							if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) 
							{
								// Get the files being dragged
								Transferable transferable = dtde.getTransferable();
								java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
								
								// Check if the file is supported
								boolean supported = files.stream().anyMatch(file -> isSupportedFile(file.getName()));
								if (supported) { fileSupported = true; } 
								else { fileSupported = false; }
							}
						} catch (Exception e) { e.printStackTrace(); }

						showDragMessage = true;
						repaint();
					}
				}
	
				@Override
				public void dragOver(DropTargetDragEvent dtde) { }
	
				@Override
				public void dropActionChanged(DropTargetDragEvent dtde) { }
	
				@Override
				public void dragExit(DropTargetEvent dte) 
				{
					showDragMessage = false;
					repaint();
				}
	
				@Override
				@SuppressWarnings("unchecked")
				public void drop(DropTargetDropEvent dtde) 
				{
					try 
					{
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						Transferable transferable = dtde.getTransferable();
						if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) 
						{
							java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
							if (!files.isEmpty() && !awtGUI.hasLoadedFile() && fileSupported) 
							{
								// Load the dropped file
								awtGUI.loadJarFile(files.get(0).toURI().toString(), true);
							}
						}
					} 
					catch (Exception e) { System.out.println("Exception caught in Drag and Drop:" + e.getMessage()); } 
					finally 
					{
						dtde.dropComplete(true);
						showDragMessage = false;
						repaint();
					}
				}
			});
		}

		private boolean isSupportedFile(String fileName) 
		{
			// Check for supported extensions with drag and drop
			return fileName.toLowerCase().endsWith(".jar") ||
				   fileName.toLowerCase().endsWith(".jad") ||
				   fileName.toLowerCase().endsWith(".kjx");
		}
	}
}
*/

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

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FreeJ2ME
{
	public static FreeJ2ME app;
	private Frame main;
	private int lcdWidth;
	private int lcdHeight;
	private int scaleFactor = 1;

	private MobilePlatform platform;
	private boolean isShuttingDown = false;

	public static Color freeJ2MEBGColor = new Color(0,0,64);
	public static Color freeJ2MEDragColor = new Color(55, 55, 125);

	public static boolean isFullscreen = false;

	private LCD lcd;

	private int xborder;
	private int yborder;

	private AWTGUI awtGUI;

	public static void main(String args[])
	{
		Mobile.clearOldLog();
		FreeJ2ME.app = new FreeJ2ME(args);
	}

	public FreeJ2ME(String args[])
	{
		app = this;
		try {
			if (args.length >= 3) {
				Mobile.lcdWidth = Integer.parseInt(args[1]);
				Mobile.lcdHeight = Integer.parseInt(args[2]);
			}
			if (args.length >= 4) {
				scaleFactor = Integer.parseInt(args[3]);
			}

			lcdWidth = Mobile.lcdWidth;
			lcdHeight = Mobile.lcdHeight;

			platform = new MobilePlatform(lcdWidth, lcdHeight);
			Mobile.setPlatform(platform);

			lcd = new LCD();
			lcd.setFocusable(true);

			Mobile.config = new Config();
			Mobile.config.onChange = new Runnable() {
				public void run() {
					settingsChanged();
				}
			};

			awtGUI = new AWTGUI(Mobile.config);

			constructFreeJ2MEGUI();

			if (args.length >= 1) {
				awtGUI.loadJarFile(getFormattedLocation(args[0]), true);
			}

			lcd.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (awtGUI.hasLoadedFile()) {
						int keycode = e.getKeyCode();
						int mobikey = getMobileKey(keycode);

						switch (keycode)
						{
							case KeyEvent.VK_PLUS:
							case KeyEvent.VK_ADD:
								if (!isFullscreen) {
									scaleFactor++;
									main.setSize(lcdWidth * scaleFactor + xborder, lcdHeight * scaleFactor + yborder);
								}
								break;
							case KeyEvent.VK_MINUS:
							case KeyEvent.VK_SUBTRACT:
								if (scaleFactor > 1 && !isFullscreen) {
									scaleFactor--;
									main.setSize(lcdWidth * scaleFactor + xborder, lcdHeight * scaleFactor + yborder);
								}
								break;
							case KeyEvent.VK_F:
								if (e.isControlDown()) {
									toggleFullscreen();
								}
								break;
						}

						if (mobikey == Integer.MIN_VALUE) {
							return;
						}

						if (MobilePlatform.pressedKeys[mobikey] == false) {
							if (mobikey < 19) {
								MobilePlatform.pressedKeys[mobikey] = true;
								MobilePlatform.keyPressed(Mobile.getMobileKey(mobikey));
							} else {
								if (e.isControlDown()) {
									MobilePlatform.pressedKeys[mobikey] = true;
								}
							}
						} else {
							if (mobikey < 19) {
								MobilePlatform.keyRepeated(Mobile.getMobileKey(mobikey));
							}
						}
					}
				}

				public void keyReleased(KeyEvent e) {
					if (awtGUI.hasLoadedFile()) {
						int mobikey = getMobileKey(e.getKeyCode());

						if (mobikey == Integer.MIN_VALUE) {
							return;
						}

						MobilePlatform.pressedKeys[mobikey] = false;
						MobilePlatform.keyReleased(Mobile.getMobileKey(mobikey));

						if (mobikey == 20) {
							ScreenShot.takeScreenshot(false);
						} else if (mobikey == 21) {
							MobilePlatform.pauseResumeApp();
						}

						for (int i = 0; i < MobilePlatform.pressedKeys.length; i++) {
							if (MobilePlatform.pressedKeys[i]) {
								MobilePlatform.keyRepeated(Mobile.getMobileKey(i));
							}
						}
					}
				}

				public void keyTyped(KeyEvent e) { }
			});

			lcd.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent e) {
					if (awtGUI.hasLoadedFile()) {
						int x = (int) ((e.getX() - lcd.cx) * lcd.scalex);
						int y = (int) ((e.getY() - lcd.cy) * lcd.scaley);

						if (Mobile.rotateDisplay) {
							x = (int) ((lcd.ch - (e.getY() - lcd.cy)) * lcd.scaley);
							y = (int) ((e.getX() - lcd.cx) * lcd.scalex);
						}

						MobilePlatform.pointerPressed(x, y);
					}
				}

				public void mouseReleased(MouseEvent e) {
					if (awtGUI.hasLoadedFile()) {
						int x = (int) ((e.getX() - lcd.cx) * lcd.scalex);
						int y = (int) ((e.getY() - lcd.cy) * lcd.scaley);

						if (Mobile.rotateDisplay) {
							x = (int) ((lcd.ch - (e.getY() - lcd.cy)) * lcd.scaley);
							y = (int) ((e.getX() - lcd.cx) * lcd.scalex);
						}

						MobilePlatform.pointerReleased(x, y);
					}
				}

				public void mouseExited(MouseEvent e) { }
				public void mouseEntered(MouseEvent e) { }
				public void mouseClicked(MouseEvent e) { }
			});

			lcd.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e) {
					if (awtGUI.hasLoadedFile()) {
						int x = (int) ((e.getX() - lcd.cx) * lcd.scalex);
						int y = (int) ((e.getY() - lcd.cy) * lcd.scaley);

						if (Mobile.rotateDisplay) {
							x = (int) ((lcd.ch - (e.getY() - lcd.cy)) * lcd.scaley);
							y = (int) ((e.getX() - lcd.cx) * lcd.scalex);
						}

						MobilePlatform.pointerDragged(x, y);
					}
				}
			});

			displayGUI();

			platform.setPainter(new Runnable() {
				public void run() {
					if (awtGUI.hasJustLoaded()) {
						awtGUI.updateOptions();
					}
					if (awtGUI.awtDialogs[2].isVisible()) {
						awtGUI.updateMemStatDialog();
					}
					if (awtGUI.hasChanged()) {
						settingsChanged();
						awtGUI.clearChanged();
					}
					if (lcd != null) {
						lcd.repaint();
					}
				}
			});

			if(args.length<1)
			{
				while(!awtGUI.hasLoadedFile())
				{
					try{ Thread.sleep(1000); }
					catch (InterruptedException e) { }
				}
			}
			if(platform.load(awtGUI.getJarPath()))
			{
				Mobile.config.init();
				if(args.length>=3)
				{
					lcdWidth = Integer.parseInt(args[1]);
					lcdHeight = Integer.parseInt(args[2]);
					Mobile.config.settings.put("width",  ""+lcdWidth);
					Mobile.config.settings.put("height", ""+lcdHeight);
				}
				settingsChanged();
				platform.runJar();
			}
			else
			{
				Mobile.log(Mobile.LOG_ERROR, "FreeJ2ME: Couldn't load jar...");
				shutdown();
			}
		} catch (Exception e) {
			Mobile.log(Mobile.LOG_ERROR, "FreeJ2ME: Initialization failed: " + e.getMessage());
			shutdown();
		}
	}

	private static String getFormattedLocation(String loc)
	{
		if (loc == null || loc.isEmpty()) {
			return "";
		}
		if (loc.startsWith("file://") || loc.startsWith("http://") || loc.startsWith("https://"))
			return loc;

		File file = new File(loc);
		if(!file.isFile())
		{
			Mobile.log(Mobile.LOG_ERROR, "FreeJ2ME: File not found...");
			return "";
		}
		return file.toURI().toString();
	}

	private void settingsChanged()
	{
		boolean hasRotated = Mobile.updateSettings();

		if(Mobile.lcdWidth != lcdWidth || Mobile.lcdHeight != lcdHeight || hasRotated)
		{
			platform.resizeLCD(Mobile.lcdWidth, Mobile.lcdHeight);
			if(!Mobile.rotateDisplay)
			{
				lcdWidth = Mobile.lcdWidth;
				lcdHeight = Mobile.lcdHeight;
			}
			else
			{
				lcdWidth = Mobile.lcdHeight;
				lcdHeight = Mobile.lcdWidth;
			}
			resize();
			if(!isFullscreen) { main.setSize(lcdWidth*scaleFactor+xborder, lcdHeight*scaleFactor+yborder); }
			lcd.clearScreen();
		}
	}

	public void shutdown()
	{
		if (isShuttingDown) {
			return;
		}
		isShuttingDown = true;
		Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Initiating shutdown...");
		try {
			if (platform != null) {
				Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Stopping game...");
				platform.stopApp();
				Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Resetting platform...");
				platform.reset();
				Mobile.setPlatform(null);
				platform = null;
			}
		} catch (Exception e) {
			Mobile.log(Mobile.LOG_ERROR, "FreeJ2ME: Cleanup failed: " + e.getMessage());
		}
		Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Clearing resources...");
		awtGUI = null;
		lcd = null;
		if (main != null) {
			Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Disposing frame...");
			main.dispose();
			main = null;
		}
		System.gc();
		Mobile.log(Mobile.LOG_INFO, "FreeJ2ME: Shutdown complete.");
		isShuttingDown = false;
	}

	private int getMobileKey(int keycode)
	{
		for(int i = 0; i < awtGUI.inputKeycodes.length; i++)
		{
			if(keycode == awtGUI.inputKeycodes[i]) { return Mobile.convertAWTKeycode(i);}
		}
		return Integer.MIN_VALUE;
	}

	private void resize()
	{
		xborder = main.getInsets().left+main.getInsets().right;
		yborder = main.getInsets().top+main.getInsets().bottom;

		double vw = (main.getWidth()-xborder)*1;
		double vh = (main.getHeight()-yborder)*1;

		double nw = lcdWidth;
		double nh = lcdHeight;

		nw = vw;
		nh = nw*((double)lcdHeight/(double)lcdWidth);

		if(nh>vh)
		{
			nh = vh;
			nw = nh*((double)lcdWidth/(double)lcdHeight);
		}

		lcd.updateScale((int)nw, (int)nh);
	}

	public void toggleFullscreen()
	{
		isFullscreen = !isFullscreen;
		main.dispose();
		constructFreeJ2MEGUI();
		displayGUI();
	}

	private void constructFreeJ2MEGUI()
	{
		main = new Frame("FreeJ2ME-Plus");

		if (isFullscreen)
		{
			main.setUndecorated(true);
			main.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		}
		else
		{
			main.setSize(350, 450);
			main.setMinimumSize(new Dimension(240, 240));
			main.setLocationRelativeTo(null);
		}

		main.setBackground(freeJ2MEBGColor);

		try
		{
			main.setIconImage(ImageIO.read(main.getClass().getResourceAsStream("/org/recompile/icon.png")));
		}
		catch (Exception e) { }

		main.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				shutdown();
			}
		});

		main.add(lcd);
		awtGUI.setMainFrame(main);
		if(!isFullscreen) { main.setMenuBar(awtGUI.getMenuBar()); }
	}

	private void displayGUI()
	{
		main.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				resize();
			}
		});

		main.setVisible(true);
		main.pack();
		resize();
		if(!isFullscreen) { main.setSize(lcdWidth*scaleFactor+xborder, lcdHeight*scaleFactor+yborder); }
	}

	private class LCD extends Canvas
	{
		private boolean showDragMessage = false, fileSupported = true;
		public int cx=0;
		public int cy=0;
		public int cw=240;
		public int ch=320;

		public double scalex=1;
		public double scaley=1;

		public LCD() { setDropTarget(); }

		public void updateScale(int vw, int vh)
		{
			cx = (this.getWidth()-vw)/2;
			cy = (this.getHeight()-vh)/2;
			cw = vw;
			ch = vh;
			scalex = (double)lcdWidth/(double)vw;
			scaley = (double)lcdHeight/(double)vh;
		}

		@Override
		public void update(Graphics g) {
			paint(g);
		}

		public void clearScreen()
		{
			Graphics2D cgc = (Graphics2D) this.getGraphics();
			if (cgc != null) {
				cgc.clearRect(0, 0, getWidth(), getHeight());
				cgc.dispose();
			}
		}

		public void paint(Graphics g)
		{
			if(!showDragMessage)
			{
				if(!Mobile.rotateDisplay) {
					g.drawImage(Mobile.getPlatform().getLCD(), cx, cy, cw, ch, null);
				}
				else
				{
					final Graphics2D cgc = (Graphics2D)g;
					cgc.rotate(Math.toRadians(-90), ch/2, ch/2);
					cgc.drawImage(Mobile.getPlatform().getLCD(), 0, cx, ch, cw, null);
				}
				if(MobilePlatform.isPaused)
				{
					g.setColor(new Color(0, 0, 0, 160));
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setFont(new Font("Dialog", Font.BOLD, cw/5));
					g.setColor(Color.WHITE);
					String message = "PAUSED!";
					FontMetrics metrics = g.getFontMetrics();
					int x = (getWidth() - metrics.stringWidth(message)) / 2;
					int y = (getHeight() + metrics.getAscent()) / 2;
					g.drawString(message, x, y);
				}
				else if (MobilePlatform.pressedKeys[19])
				{
					g.setFont(new Font("Dialog", Font.BOLD, cw/2));
					g.setColor(Color.WHITE);
					String fastForwardIndicator = "»";
					FontMetrics ffMetrics = g.getFontMetrics();
					int ffX = (getWidth() - ffMetrics.stringWidth(fastForwardIndicator)) / 2;
					int ffY = (getHeight() + ffMetrics.getAscent()) / 2;
					g.drawString(fastForwardIndicator, ffX, ffY);
				}
			}
			else
			{
				super.paint(g);
				g.setColor(freeJ2MEDragColor);
				g.fillRect(cx, cy, cw, ch);
				g.setFont(new Font("Dialog", Font.BOLD, 20));
				g.setColor(fileSupported ? Color.WHITE : Color.RED);
				String message = fileSupported ? ">> DROP HERE <<" : "INVALID FILE TYPE!!!";
				FontMetrics metrics = g.getFontMetrics();
				int x = (getWidth() - metrics.stringWidth(message)) / 2;
				int y = (getHeight() / 2);
				g.drawString(message, x, y);
			}
		}

		private void setDropTarget()
		{
			new DropTarget(this, new DropTargetListener()
			{
				@Override
				@SuppressWarnings("unchecked")
				public void dragEnter(DropTargetDragEvent dtde)
				{
					if(!awtGUI.hasLoadedFile())
					{
						try
						{
							if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
							{
								// Get the files being dragged
								Transferable transferable = dtde.getTransferable();
								java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

								// Check if the file is supported
								boolean supported = files.stream().anyMatch(file -> isSupportedFile(file.getName()));
								if (supported) { fileSupported = true; }
								else { fileSupported = false; }
							}
						} catch (Exception e) { e.printStackTrace(); }

						showDragMessage = true;
						repaint();
					}
				}

				@Override
				public void dragOver(DropTargetDragEvent dtde) { }

				@Override
				public void dropActionChanged(DropTargetDragEvent dtde) { }

				@Override
				public void dragExit(DropTargetEvent dte)
				{
					showDragMessage = false;
					repaint();
				}

				@Override
				@SuppressWarnings("unchecked")
				public void drop(DropTargetDropEvent dtde)
				{
					try
					{
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						Transferable transferable = dtde.getTransferable();
						if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
						{
							java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
							if (!files.isEmpty() && !awtGUI.hasLoadedFile() && fileSupported)
							{
								// Load the dropped file
								awtGUI.loadJarFile(files.get(0).toURI().toString(), true);
							}
						}
					}
					catch (Exception e) { System.out.println("Exception caught in Drag and Drop:" + e.getMessage()); }
					finally
					{
						dtde.dropComplete(true);
						showDragMessage = false;
						repaint();
					}
				}
			});
		}

		private boolean isSupportedFile(String fileName)
		{
			// Check for supported extensions with drag and drop
			return fileName.toLowerCase().endsWith(".jar") ||
					fileName.toLowerCase().endsWith(".jad") ||
					fileName.toLowerCase().endsWith(".kjx");
		}
		}
	}

