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
package com.jblend.media;

public abstract class MediaPlayer 
{

	public static final int NO_DATA = 0;
	public static final int READY = 1;
	public static final int PLAYING = 2;
	public static final int PAUSED = 3;
	public static final int ERROR = 0x10000;

	protected static final int REAL_WIDTH = 0;
	protected static final int REAL_HEIGHT = 0;

	public abstract void setData(MediaData data);

	public abstract void play();

	public abstract void play(boolean isRepeat);

	public abstract void play(int count);

	public abstract void stop();

	public abstract void pause();

	public abstract void resume();

	public abstract int getState();

	public abstract void addMediaPlayerListener(MediaPlayerListener l);

	public abstract void removeMediaPlayerListener(MediaPlayerListener l);

}