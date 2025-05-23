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
package com.j_phone.amuse;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.recompile.mobile.PlatformPlayer;

public class PhraseTrack 
{
	public static final int DEFAULT_VOLUME = 100;

    public static final int NO_DATA = 0;
	public static final int PAUSED = 3;
	public static final int PLAYING = 2;
	public static final int READY = 1;
	private static final int MAX_VOLUME = 127;
	private Phrase phrase;
	private Player player;
    private int playerState = NO_DATA;
	private PhraseTrackListener listener;

    PhraseTrack() { }

    public int getID() { return 0; } /* TODO: Flesh this out */

	public Phrase getPhrase() { return phrase; }

    public int getState() { return playerState; }

	public void setPhrase(Phrase p) 
    {
		this.phrase = p;
		this.player = p.getPlayer();
	}

	public boolean isPlaying() { return getState() == PLAYING; }

    public boolean isMute() 
    { 
        if(player == null) { return true; }
        else { return ((PlatformPlayer.volumeControl)player.getControl("VolumeControl")).isMuted(); }
    }

	public void setVolume(int value) 
    {
        if(player == null) { return; }

		if (value < 0) { value = 0; }
		if (value > MAX_VOLUME) { value = MAX_VOLUME; }
        ((PlatformPlayer.volumeControl)player.getControl("VolumeControl")).setLevel(value);
	}

    public void mute(boolean mute) 
    {
        if(player == null) { return; }

        ((PlatformPlayer.volumeControl)player.getControl("VolumeControl")).setMute(mute);
	}

	public void stop() 
    { 
        if(player != null && getState() == PLAYING) 
        { 
            player.stop(); 
            player.setMediaTime(0);
            playerState = READY;
        } 
    }

    // For play() we'll assume it rewinds back to the start, otherwise resume() below is useless
    public void play() 
    {
        if (player != null && getState() != PLAYING) 
        {
            player.setMediaTime(0);
            player.start();
            playerState = PLAYING;
        }
	}

	public void play(int loop) 
    {
        if (player != null && getState() != PLAYING) 
        {
            if (loop == 0) { loop = -1; }
            player.setLoopCount(loop);
            player.setMediaTime(0);
            player.start();
            playerState = PLAYING;
        }
	}

    public void pause() 
    {
        if (player != null && getState() == PLAYING) 
        {
            player.stop();
            playerState = PAUSED;
        }
	}

    public void resume() 
    {
        if (player != null && getState() == PAUSED) 
        {
            player.start();
            playerState = PLAYING;
        }
    }

	public void removePhrase() { this.phrase = null; }

    public PhraseTrack getSyncMaster() 
    {
        /* TODO: Flesh this out */
        return null;
    }

    public void setSubjectTo(PhraseTrack phraseTrack) 
    {
        /* TODO: Flesh this out */
    }

    // TODO: Make PlatformPlayer also report events to vodafone listeners
	public void setEventListener(PhraseTrackListener l) { this.listener = l; }
}