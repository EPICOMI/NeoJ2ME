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
package mmpp.media;

import java.io.ByteArrayInputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.PlatformPlayer;

public class MediaPlayer 
{
    private static final byte STOPPED = 0;
    private static final byte STARTED = 1;

    private byte state = 0;
    private Player player;
    private boolean isLooping = false;

    public MediaPlayer() { }

    public String getVolumeLevel() 
    {
        if(player != null ) { return "" + ((PlatformPlayer.volumeControl)player.getControl("VolumeControl")).getLevel(); }
        return "0";
    }

    public boolean isPlayBackLoop() { return isLooping; }

    public void pause() { stop(); }

    public void resume() { if(player != null) { player.start(); } }

    public void setMediaLocation(String location) throws java.io.IOException , MediaException
    { 
        player = Manager.createPlayer(Mobile.getMIDletResourceAsStream(location), "");
        player.prefetch();
    }

    public void setMediaSource(byte[] buffer) throws java.io.IOException , MediaException
    {
        player = Manager.createPlayer(new ByteArrayInputStream(buffer), "");
        player.prefetch();
    }

    public void setMediaSource(byte[] buffer, int offset, int length) throws java.io.IOException , MediaException
    {
        player = Manager.createPlayer(new ByteArrayInputStream(buffer, offset, length), "");
		player.prefetch();
    }

    public void setPlayBackLoop(boolean val) 
    {
        if(state == STARTED) { player.stop(); }
        if(val) { player.setLoopCount(Integer.MAX_VALUE); isLooping = true; }
        else { player.setLoopCount(0); isLooping = false; }

        if(state == STARTED) { player.start();}
    }

    public void setVolumeLevel(String volumeString) { if(player != null ) { ((PlatformPlayer.volumeControl)player.getControl("VolumeControl")).setLevel(Integer.parseInt(volumeString)); } }

    public void start() 
    {
        if(player != null) 
        {
            player.setMediaTime(0);
            player.start();
        }
        state = STARTED;
    }

    public void stop() 
    {
        if(player != null) { player.stop(); }
        state = STOPPED;
    }
}