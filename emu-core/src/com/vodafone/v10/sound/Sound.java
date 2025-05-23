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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.recompile.mobile.Mobile;

public class Sound 
{
	private Player player;
    byte[] data;
	String dataLoc;

	public Sound(byte[] data) throws IOException 
    {
		if (data == null) { throw new NullPointerException("sound data cannot be null."); }

        this.data = data;
        // Vodafone's Sound API doesn't stipulate any specific formats, neat.
        try 
        {
            player = Manager.createPlayer(new ByteArrayInputStream(data), "audio/vodafone");
            player.prefetch();
        } 
        catch (MediaException e) { Mobile.log(Mobile.LOG_ERROR, Sound.class.getPackage().getName() + "." + Sound.class.getSimpleName() + ": " + "Failed to create player: " + e.getMessage()); }
	}

	public Sound(String data) throws IOException 
    {
		if (data == null) { throw new NullPointerException("sound data cannot be null."); }

        this.dataLoc = data;
        // Vodafone's Sound API doesn't stipulate any specific formats, neat.
        try 
        {
            player = Manager.createPlayer(Mobile.getMIDletResourceAsStream(dataLoc), "audio/vodafone");
            player.prefetch();
        } 
        catch (MediaException e) { Mobile.log(Mobile.LOG_ERROR, Sound.class.getPackage().getName() + "." + Sound.class.getSimpleName() + ": " + "Failed to create player: " + e.getMessage()); }
	}

    int getSize() { return data.length; }

    int getUseTracks() { return 1; /* TODO: Return how many tracks the player's MIDI sequence is using */}

	Player getPlayer() { return player; }
}