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
package com.vodafone.v10.graphics.sprite;

import java.util.ArrayList;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.recompile.mobile.Mobile;

public abstract class SpriteCanvas extends Canvas 
{
	private static ArrayList<CharacterCommand> commands = new ArrayList<>();
	private Image spriteImage;
	private Graphics spriteGraphics;
	private int[] palettes;
	private byte[] patternData;
	private int[] pixels;

	public SpriteCanvas(int numPalettes, int numPatterns) 
    {
		super();
		this.palettes = new int[numPalettes];
		this.patternData = new byte[numPatterns * 64];
		this.pixels = new int[64];
	}

	public void createFrameBuffer(int fw, int fh) 
    {
		spriteImage = Image.createImage(fw, fh, 0);
		spriteGraphics = (Graphics) spriteImage.getGraphics();
	}

	public void disposeFrameBuffer() { spriteGraphics = null; spriteImage = null; }

    // TODO: This might be incorrect, it has to copy from the screen to the framebuffer
	public void copyArea(int sx, int sy, int fw, int fh, int tx, int ty) 
    { 
        if(Mobile.getDisplay().getCurrent() != this) { return; }
        spriteGraphics.copyArea(sx, sy, fw, fh, tx, ty, 0);
    }

    // TODO: Might also be incorrect for the same reason
    public void copyFullScreen(int tx, int ty) 
    {
        if(Mobile.getDisplay().getCurrent() != this) { return; }
        spriteGraphics.copyArea(0, 0, getVirtualWidth()-tx, getVirtualHeight()-ty, tx, ty, 0);
    }

	public void drawFrameBuffer(int tx, int ty) 
    {
        if(Mobile.getDisplay().getCurrent() != this) { return; }
		Mobile.getPlatform().flushGraphics(spriteImage, tx, ty, getVirtualWidth()-tx, getVirtualHeight()-ty);
	}

	public void setPalette(int index, int palette) { this.palettes[index] = palette | 0xFF000000; }

	public void setPattern(int index, byte[] data) { System.arraycopy(data, 0, patternData, index * 64, data.length); }

	public static short createCharacterCommand(int offset, boolean transparent, int rotation, boolean isUpsideDown, boolean isRightsideLeft, int patternNo)
    {
		int cmd = (offset & 0x7) << 13;

		if (transparent) { cmd |= 0x1000; }
		cmd |= (rotation & 0x3) << 10;
		if (isUpsideDown) { cmd |= 0x200; }
		if (isRightsideLeft) { cmd |= 0x100; }
		cmd |= patternNo & 0xFF;

		return (short) cmd;
	}

    public void drawBackground(short command, short x, short y) 
    {
        if(Mobile.getDisplay().getCurrent() != this) { return; }

        command = (short) (command & 0xFFFF);
		int offset = (command >> 13) & 0x7;
		int rotation = (command >> 10) & 0x3;
		boolean transparent = (command & 0x1000) != 0;
		boolean isUpsideDown = (command & 0x200) != 0;
		boolean isRightsideLeft = (command & 0x100) != 0;
		int patternNo = command & 0xff;

		for (int x1 = 0; x1 < 8; x1++) 
        {
			for (int y1 = 0; y1 < 8; y1++) 
            {
				int cmd = (isUpsideDown ? 7 - y1 : y1) * 8 + (isRightsideLeft ? 7 - x1 : x1);
				int colorId = patternData[patternNo * 64 + cmd] & 0xFF;

				if (rotation == 1) { cmd = (7 - y1) + x1 * 8; } // 90 degrees
                else if (rotation == 2) { cmd = (7 - y1) * 8 + (7 - x1); } // 180 degrees
                else if (rotation == 3) { cmd = y1 + (7 - x1) * 8; } // 270 degrees
                else { cmd = y1 * 8 + x1; } // No rotation
				pixels[cmd] = transparent && colorId == 0 ? 0xFF000000
						: (palettes[(colorId + (offset * 32)) & 0xFF]);
			}
		}
		spriteGraphics.drawRGB(pixels, 0, 8, x, y, 8, 8, true);
    }

	public void drawSpriteChar(short command, short x, short y)
    {
        if(Mobile.getDisplay().getCurrent() != this) { return; }

		command = (short) (command & 0xFFFF);
		int offset = (command >> 13) & 0x7;
		int rotation = (command >> 10) & 0x3;
		boolean transparent = (command & 0x1000) != 0;
		boolean isUpsideDown = (command & 0x200) != 0;
		boolean isRightsideLeft = (command & 0x100) != 0;
		int patternNo = command & 0xff;

		for (int x1 = 0; x1 < 8; x1++) 
        {
			for (int y1 = 0; y1 < 8; y1++) 
            {
				int cmd = (isUpsideDown ? 7 - y1 : y1) * 8 + (isRightsideLeft ? 7 - x1 : x1);
				int colorId = patternData[patternNo * 64 + cmd] & 0xFF;

				if (rotation == 1) { cmd = (7 - y1) + x1 * 8; } // 90 degrees
                else if (rotation == 2) { cmd = (7 - y1) * 8 + (7 - x1); } // 180 degrees
                else if (rotation == 3) { cmd = y1 + (7 - x1) * 8; } // 270 degrees
                else { cmd = y1 * 8 + x1; } // No rotation
				pixels[cmd] = transparent && colorId == 0 ? 0x00000000
						: (palettes[(colorId + (offset * 32)) & 0xFF]);
			}
		}
		spriteGraphics.drawRGB(pixels, 0, 8, x, y, 8, 8, true);
	}

    public static int getVirtualHeight() { return Mobile.lcdHeight; }

    public static int getVirtualWidth() { return Mobile.lcdWidth; }

	private static class CharacterCommand
    {
		int offset;
		boolean transparent;
		int rotation;
		boolean isUpsideDown;
		boolean isRightsideLeft;
		int patternNo;
	}
}