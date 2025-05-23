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
package com.nttdocomo.ui;

public class Palette 
{
    private int[] entries;

    public Palette(int n) 
    {
        if (n <= 0) { throw new IllegalArgumentException("Number of entries must be greater than zero"); }

        entries = new int[Math.min(n, 256)];
        for (int i = 0; i < entries.length; i++) { entries[i] = 0xFF000000; } // Spec dictates it should initialize to black, so go with fully opaque
    }

    public Palette(int[] colors) 
    {
        if (colors == null) { throw new NullPointerException("Colors array cannot be null"); }
        if (colors.length == 0) { throw new IllegalArgumentException("Colors array must have at least one entry"); }
        if (colors.length > 256) { throw new IllegalArgumentException("Palette can have a maximum of 256 colors"); }

        entries = new int[colors.length];
        System.arraycopy(colors, 0, entries, 0, colors.length);
    }

    public int getEntry(int index) 
    {
        if (index < 0 || index >= entries.length) { throw new ArrayIndexOutOfBoundsException("Index out of bounds"); }

        return entries[index];
    }

    public int getEntryCount() { return entries.length; }

    public void setEntry(int index, int color) 
    {
        if (index < 0 || index >= entries.length) { throw new ArrayIndexOutOfBoundsException("Index out of bounds"); }
        // TODO: Validate the color value (do we really need to?)
        entries[index] = color;
    }
}