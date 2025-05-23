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
package com.j_phone.system;

import javax.microedition.lcdui.Image;

public class SubDisplay 
{
	private static SubDisplay instance;

	public static SubDisplay getInstance() throws RuntimeException 
    {
		if (instance == null) { return instance = new SubDisplay(0); }
		return instance;
	}

	public int getWidth() { return 0; }

	public int getHeight() { return 0; }

	public int getFullWidth() { return 0; }

	public int getFullHeight() { return 0; }

	public boolean isColor() { return false; }

	public int numColors() { return 1; }

	public void setViewPort(int paramInt1, int paramInt2) throws IllegalArgumentException, RuntimeException { }

	public void releaseViewPort() throws RuntimeException { }

	public void setWallPaperImage(Image paramImage) throws RuntimeException { }

	private SubDisplay(int paramInt) throws RuntimeException { }
}