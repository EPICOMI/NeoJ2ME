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
package com.kddi.media;

public class QcelpPlayer extends MediaPlayer 
{

    protected class QcelpEventWatcher { }

    protected QcelpPlayer(MediaResource resource, MediaPlayerBox box) 
    { 
        super(resource, box);
    }

    public static boolean canPlay(String dataType) { return false; }

    protected void dispose() { }

    protected boolean disposePlayer() { return false; }

    public int getPitch() { return 0; }

    public int getTempo() { return 100; }

    public int getVolume() { return 100; }

    public void pause() { }

    public void play() { }

    public void play(int count) { }

    public void resume() { }

    public void setPitch(int pitch) { }

    public void setTempo(int tempo) { }

    public void setVolume(int volume) { }

    public void stop() { }
}