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
package com.j_phone.media;

import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Ticker;

public class MediaPlayer extends Canvas 
{
	public MediaPlayer(byte[] paramArrayOfByte) { }

	public MediaPlayer(String paramString) throws IOException { }

	public void setMediaData(byte[] paramArrayOfByte) { }

	public void setMediaData(String paramString) throws IOException { }

	public int getMediaWidth() { return 0; }

	public int getMediaHeight() { return 0; }

	public int getWidth() { return 0; }

	public int getHeight() { return 0; }

	public void setContentPos(int paramInt1, int paramInt2) { }

	public void play() { }

	public void play(boolean paramBoolean) { }

	public void stop() { }

	public void pause() { }

	public void resume() { }

	public void setMediaPlayerListener(MediaPlayerListener paramMediaPlayerListener) { }

	protected void paint(Graphics paramGraphics) { }

	public final void setFullScreenMode(boolean paramBoolean) { }

	public final Ticker getTicker() { return null; }

	public final String getTitle() { return null; }

	public final void setTicker(Ticker paramTicker) { }

	public final void setTitle(String paramString) { }
}