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
package com.jblend.media.smd;

import com.jblend.media.MediaData;
import com.jblend.media.MediaPlayer;
import com.jblend.media.MediaPlayerListener;

public class SmdPlayer extends MediaPlayer 
{

	public SmdPlayer() { }

	public SmdPlayer(byte[] data) { }

	public SmdPlayer(SmdData data) { }

	public void setData(MediaData data) { }

	public void play() { }

	public void play(boolean isRepeat) { }

	public void play(int count) { }

	public void stop() { }

	public void pause() { }

	public void resume() { }

	public int getState() { return 0; }

	public void addMediaPlayerListener(MediaPlayerListener l) { }

	public void removeMediaPlayerListener(MediaPlayerListener l) { }
}