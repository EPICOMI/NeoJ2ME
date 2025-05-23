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

import javax.microedition.lcdui.Canvas;
import com.jblend.media.MediaPlayer;

public class MediaPlayerBox extends Canvas implements MediaPlayerInterface 
{
    protected MediaEventListener _listener;
    public static final int BACKGROUND = 0;
    public static final int FOREGROUND = 1;
    public static final int PAUSE = 2;
    public static final int PLAY = 3;
    public static final int RESOURCE_DISPOSED = 4;
    public static final int RESUME = 5;
    public static final int STOP = 6;

    public MediaPlayerBox() {}

    public MediaPlayerBox(int flag) {}

    public MediaPlayerBox(MediaResource resource, int flag) {}

    public void addMediaEventListener(MediaEventListener l) {}

    public int getAttribute(int attr) { return 0; }

    protected int getMode() { return 0; }

    public int getPitch() { return 0; }

    protected MediaPlayer getPlayer() { return null; }

    public MediaResource getResource() { return null; }

    public int getTempo() { return 0; }

    public int getVolume() { return 0; }

    public void hide() {}

    protected MediaPlayer instantiatePlayer(MediaResource resource) { return null; }

    protected void paint(javax.microedition.lcdui.Graphics g) {}

    public void pause() {}

    public void play() {}

    public void play(int count) {}

    public void removeMediaEventListener(MediaEventListener l) {}

    public void resume() {}

    public void setAttribute(int attr, int value) {}

    public void setPitch(int pitch) {}

    protected void setPlayerAttributes() {}

    public void setResource(MediaResource resource) {}

    public void setTempo(int tempo) {}

    public void setVolume(int volume) {}

    public void show() {}

    public void stop() {}

    public void unsetResource(MediaResource resource) {}
}