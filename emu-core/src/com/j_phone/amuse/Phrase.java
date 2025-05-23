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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.recompile.mobile.Mobile;

// Those Phrase* classes look a lot like vodafone v10's Sound* classes... probably work the same way too.
public class Phrase 
{
    private Player player;
    byte[] data;
	String dataLoc;

	public Phrase(byte[] data) throws IOException 
    {
		if (data == null) { throw new NullPointerException("sound data cannot be null."); }

        this.data = data;
        // Vodafone's Sound API doesn't stipulate any specific formats, neat.
        try 
        {
            player = Manager.createPlayer(new ByteArrayInputStream(data), "audio/vodafone");
            player.realize();
        } 
        catch (MediaException e) { Mobile.log(Mobile.LOG_ERROR, Phrase.class.getPackage().getName() + "." + Phrase.class.getSimpleName() + ": " + "Failed to create player: " + e.getMessage()); }
	}

	public Phrase(String data) throws IOException 
    {
		if (data == null) { throw new NullPointerException("sound data cannot be null."); }

        this.dataLoc = data;
        // Vodafone's Sound API doesn't stipulate any specific formats, neat.
        try 
        {
            player = Manager.createPlayer(Mobile.getMIDletResourceAsStream(dataLoc), "audio/vodafone");
            player.realize();
        } 
        catch (MediaException e) { Mobile.log(Mobile.LOG_ERROR, Phrase.class.getPackage().getName() + "." + Phrase.class.getSimpleName() + ": " + "Failed to create player: " + e.getMessage()); }
	}

    int getSize() { return data.length; }

    int getUseTracks() { return 1; /* TODO: Return how many tracks the player's MIDI sequence is using */}

	Player getPlayer() { return player; }
}