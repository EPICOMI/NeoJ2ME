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
package com.jblend.media.smaf.phrase;

abstract class PhraseTrackBase 
{
	public static final int NO_DATA = 1;
	public static final int READY = 2;
	public static final int PLAYING = 3;
	public static final int PAUSED = 5;
	public static final int DEFAULT_VOLUME = 100;
	public static final int DEFAULT_PANPOT = 64;
	private int state;
	private boolean muted;

	PhraseTrackBase(int id) { state = READY; }

	public void removePhrase() { }

	public void play() { state = PLAYING; }

	public void play(int loop) { state = PLAYING; }

	public void stop() { state = PAUSED; }

	public void pause() { state = PAUSED; }

	public void resume() { state = PLAYING; }

	public int getState() { return state; }

	public void setVolume(int value) { }

	public int getVolume() { return 0; }

	public void setPanpot(int value) { }

	public int getPanpot() { return 0; }

	public void mute(boolean mute) { muted = mute; }

	public boolean isMute() { return muted; }

	public int getID() { return 0; }

	public void setEventListener(PhraseTrackListener l) { }
}