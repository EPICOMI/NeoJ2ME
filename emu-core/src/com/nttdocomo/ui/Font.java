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
import org.recompile.mobile.PlatformFont;

public class Font
{

	public static final int FACE_MONOSPACE = 0x72000000;
    public static final int FACE_PROPORTIONAL = 0x73000000;
    public static final int FACE_SYSTEM = 0x71000000;

    public static final int SIZE_LARGE = 0x70000300;
    public static final int SIZE_MEDIUM = 0x70000200;
    public static final int SIZE_SMALL = 0x70000100;

    public static final int STYLE_BOLD = 0x70110000;
    public static final int STYLE_BOLDITALIC = 0x70130000;
    public static final int STYLE_ITALIC = 0x70120000;
    public static final int STYLE_PLAIN = 0x70100000;

    public static final int TYPE_DEFAULT = 0x00000000;
    public static final int TYPE_HEADING = 0x00000001;

    protected static final int[] fontSizes = 
	{
		 8, 10, 12, // < 128 minimum px dimension
		12, 14, 16, // < 176 minimum px dimension
		14, 15, 17, // < 220 minimum px dimension
		16, 18, 20, // >= 220 minimum px dimension
	};

	// Helps LCDUI to better adjust for different screen sizes.
	public static final int[] fontPadding =
	{
		1, // < 128 minimum px dimension
		2, // < 176 minimum px dimension
		2, // < 220 minimum px dimension
		3 // >= 220 minimum px dimension
	};

	public static int screenType = -4;
	protected int face;
	protected int style;
	protected int size;

	protected static Font defaultDoJaFont = null;

	public PlatformFont platformFont;

    protected Font(int face, int style, int size)
	{
		if(face != FACE_SYSTEM && face != FACE_PROPORTIONAL && face != FACE_MONOSPACE
			&& style != STYLE_PLAIN && style != STYLE_ITALIC && style != STYLE_BOLD && style != STYLE_BOLDITALIC
			&& size != SIZE_SMALL && size != SIZE_MEDIUM && size != SIZE_LARGE) 
		{
			throw new IllegalArgumentException("Cannot create a font with invalid face, style or size. style " + style + " face " + face + " size " + size);
		}

		this.face = face;
		this.style = style;
		this.size = size;
		platformFont = new PlatformFont(this);
	}

	public static void setScreenSize(int width, int height)
	{
		final int minSize = Math.min(width, height);
		if (minSize < 128)      { screenType = 0; }
		else if (minSize < 176) { screenType = 1; }
		else if (minSize < 220) { screenType = 2; }
		else                    { screenType = 3; }
		
		defaultDoJaFont = new Font(FACE_SYSTEM, STYLE_PLAIN, convertSize(SIZE_MEDIUM));   
	}

    public int getAscent() { return platformFont.getAscent();  }

    public int getBBoxHeight(String str) { return 0; }

    public int getBBoxWidth(String str) { return 0; }

    public static Font getFont(int type) { return getDefaultFont(); }

    public static Font getDefaultFont() 
    {
        if (defaultDoJaFont == null) 
		{
			defaultDoJaFont = new Font(FACE_SYSTEM, STYLE_PLAIN, convertSize(SIZE_MEDIUM)); 
		}
		return defaultDoJaFont;
    }

    public int getDescent() { return 0; }

    public int getHeight() { return platformFont.getHeight(); }

    public int getLineBreak(String str, int off, int len, int width) { return off + len; }

    public int stringWidth(String str) 
	{
		if(str == null) { throw new NullPointerException("Cannot get stringWidth from a null String"); }

		return platformFont.stringWidth(str); 
	}

    public int getFace() { return face; }

    public int getSize() { return size; }

	public int getPointSize() { return convertSize(size); }

	public int getStyle() { return style; }

    public int getLCDUIStyle() 
    { 
        if(style == STYLE_PLAIN) { return javax.microedition.lcdui.Font.STYLE_PLAIN; }
        else if(style == STYLE_ITALIC) { return javax.microedition.lcdui.Font.STYLE_ITALIC; }
        else if(style == STYLE_BOLD) { return javax.microedition.lcdui.Font.STYLE_BOLD; }
        else if(style == STYLE_BOLDITALIC) { return javax.microedition.lcdui.Font.STYLE_UNDERLINED; }
        else { return style; } 
    }

    private static int convertSize(int size)
	{
		switch(size)
		{
			case SIZE_LARGE  : return fontSizes[3*screenType + 2]+Mobile.fontSizeOffset;
			case SIZE_MEDIUM : return fontSizes[3*screenType + 1]+Mobile.fontSizeOffset;
			case SIZE_SMALL  :
			default          : return fontSizes[3*screenType]+Mobile.fontSizeOffset;
		}
	}
}