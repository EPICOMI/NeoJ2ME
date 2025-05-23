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
package com.vodafone.v10.sound;

public class SoundPlayer 
{
	private SoundTrack[] tracks = new SoundTrack[] // 16 tracks 
	{
		new SoundTrack(), new SoundTrack(), new SoundTrack(), new SoundTrack(),
		new SoundTrack(), new SoundTrack(), new SoundTrack(), new SoundTrack(), 
		new SoundTrack(), new SoundTrack(), new SoundTrack(), new SoundTrack(),
		new SoundTrack(), new SoundTrack(), new SoundTrack(), new SoundTrack()
	};

	public static SoundPlayer getPlayer() { return new SoundPlayer(); }

	public SoundTrack getTrack() 
    {
		for (int i = 0; i < tracks.length; i++) 
        {
			if (tracks[i] == null) 
            {
				tracks[i] = new SoundTrack();
				return tracks[i];
			}
		}
        return null;
	}

    public SoundTrack getTrack(int track)
    {
        if(track < 0 || track > tracks.length-1) { throw new IllegalArgumentException("invalid track index"); }
        return tracks[track];
	}

    public int getTrackCount() { return tracks.length; }

    public void disposeTrack(SoundTrack t) 
    { 
        if(t == null) { throw new NullPointerException("disposeTrack received a null argument"); }
        for (int i = 0; i < tracks.length; i++) 
        {
			if (tracks[i] == t) 
            {
				tracks[i] = null;
                return;
			}
		}
    }

    public void kill() 
	{ 
		for (int i = 0; i < tracks.length; i++) 
        {
			if (tracks[i] != null) 
            {
				tracks[i].stop();
			}
		}
	}

    public void pause() 
	{ 
		for (int i = 0; i < tracks.length; i++) 
        {
			if (tracks[i] != null) 
            {
				tracks[i].pause();
			}
		}
	}

    public void resume() 
	{ 
		for (int i = 0; i < tracks.length; i++) 
        {
			if (tracks[i] != null) 
            {
				tracks[i].resume();
			}
		}
	}
}