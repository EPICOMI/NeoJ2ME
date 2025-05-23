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

public class PhrasePlayer 
{
    private PhraseTrack[] tracks = new PhraseTrack[] // 16 tracks 
	{
		new PhraseTrack(), new PhraseTrack(), new PhraseTrack(), new PhraseTrack(),
		new PhraseTrack(), new PhraseTrack(), new PhraseTrack(), new PhraseTrack(), 
		new PhraseTrack(), new PhraseTrack(), new PhraseTrack(), new PhraseTrack(),
		new PhraseTrack(), new PhraseTrack(), new PhraseTrack(), new PhraseTrack()
	};

	public static PhrasePlayer getPlayer() { return new PhrasePlayer(); }

	public PhraseTrack getTrack() 
    {
		for (int i = 0; i < tracks.length; i++) 
        {
			if (tracks[i] == null) 
            {
				tracks[i] = new PhraseTrack();
				return tracks[i];
			}
		}
        return null;
	}

    public PhraseTrack getTrack(int track)
    {
        if(track < 0 || track > tracks.length-1) { throw new IllegalArgumentException("invalid track index"); }
        return tracks[track];
	}

    public int getTrackCount() { return tracks.length; }

    public void disposeTrack(PhraseTrack t) 
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

    public PhraseTrack getTrackPair() { return null; }

	public PhraseTrack getTrackPair(int paramInt) { return null; }

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