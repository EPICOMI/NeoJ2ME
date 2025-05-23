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
package com.pantech.titan;

import com.samsung.util.AudioClip;

// Seems to be just a Samsung AudioClip copy
public class PantechAudio 
{
	private AudioClip samPlayer;

	public PantechAudio(int type, String filename) 
    { 
        samPlayer = new AudioClip(type, filename); 
    }

    public PantechAudio(int clipType, byte[] audioData, int audioOffset, int audioLength)
	{
		samPlayer = new AudioClip(clipType, audioData, audioOffset, audioLength);
	}

	public void start(int loops, int volume) 
    {
		if (samPlayer != null) { samPlayer.play(loops, volume); }
	}

    public void play(int loops, int volume) 
    {
		if (samPlayer != null) { samPlayer.play(loops, volume); }
	}

    public void pause() { if (samPlayer != null) { samPlayer.stop(); } }

    public void resume() { if (samPlayer != null) { samPlayer.resume(); } }

	public void stop() 
    {
		if (samPlayer != null) samPlayer.stop();
	}

    public static boolean isSupported() { return true; }	
}