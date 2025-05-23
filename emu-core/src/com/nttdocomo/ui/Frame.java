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

import org.recompile.mobile.Mobile;
import org.recompile.mobile.PlatformImage;

public abstract class Frame
{
    public static final int SOFT_KEY_1 = 0;
    public static final int SOFT_KEY_2 = 1;

    public PlatformImage platformImage;
	public Graphics graphics = null;
    public boolean labelVisible = false;

	public int width = 0;
	public int height = 0;

    public Frame() 
    { 
        width = Mobile.getPlatform().lcdWidth;
		height = Mobile.getPlatform().lcdHeight;
		platformImage = new PlatformImage(width, height, null);
		graphics = platformImage.getDoJaGraphics();
    }

    public final int getHeight()  { return width; }

    public final int getWidth() { return height; }

    public void setBackground(int color) 
    {
        if (color < 0) { throw new IllegalArgumentException("Invalid color value."); }

        graphics.setColor(color);
    }

    public void setSoftLabel(int key, String label) 
    {
        if (key != SOFT_KEY_1 && key != SOFT_KEY_2) { throw new IllegalArgumentException("Invalid soft key number."); }
        // TODO
    }

    public void setSoftLabelVisible(boolean b) 
    {
        labelVisible = b;
    }

    public boolean isShown() { return Display.getCurrent() == this; }
}