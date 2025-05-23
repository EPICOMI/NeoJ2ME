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
package com.vodafone.v10.system.media;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import java.io.IOException;

public class MediaPlayer extends Canvas 
{
	public static final int ERROR = 65536;
	public static final int NO_DATA = 0;
	public static final int READY = 1;
	public static final int PLAYING = 2;
	public static final int PAUSED = 3;


	public MediaPlayer(byte[] data) { }

	public MediaPlayer(String url) throws IOException { }

	public int getHeight() { return 0; }

	public int getMediaHeight() { return 0; }

	public int getMediaWidth() { return 0; }

	public int getState() { return 0; }

	public int getWidth() { return 0; }

	public void hideNotify() { }

	protected void paint(Graphics g) { }

	public void pause() { }

	public void play() { }

	public void play(boolean isRepeat) { }

	public void resume() { }

	public void setContentPos(int x, int y) { }

	public void setMediaData(byte[] data) { }

	public void setMediaData(String url) throws IOException { }

	public void setMediaPlayerListener(MediaPlayerListener listener) { }

	public void showNotify() { }

	public void stop() { }
}