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
package com.jblend.graphics;

import org.recompile.mobile.Mobile;

public abstract class SpriteCanvas extends com.vodafone.v10.graphics.sprite.SpriteCanvas 
{

	public SpriteCanvas(int numPalettes, int numPatterns) { super(numPalettes, numPatterns); }

	public static int getVirtualWidth() { return Mobile.lcdWidth; }

	public static int getVirtualHeight() { return Mobile.lcdHeight; }

	public static short createCharacterCommand(int offset, boolean transparent, int rotation, boolean isUpsideDown, boolean isRightsideLeft, int patternNo) 
    {
		return com.vodafone.v10.graphics.sprite.SpriteCanvas.createCharacterCommand(offset, transparent, rotation, isUpsideDown, isRightsideLeft, patternNo);
	}
}