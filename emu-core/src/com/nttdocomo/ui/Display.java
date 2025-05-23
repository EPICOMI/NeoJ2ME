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
import org.recompile.mobile.MobilePlatform;

public class Display
{
    public static final int KEY_0 = 0x00;
    public static final int KEY_1 = 0x01;
    public static final int KEY_2 = 0x02;
    public static final int KEY_3 = 0x03;
    public static final int KEY_4 = 0x04;
    public static final int KEY_5 = 0x05;
    public static final int KEY_6 = 0x06;
    public static final int KEY_7 = 0x07;
    public static final int KEY_8 = 0x08;
    public static final int KEY_9 = 0x09;
    public static final int KEY_ASTERISK = 0x0a;
    public static final int KEY_POUND = 0x0b;
    public static final int KEY_UP = 0x11;
    public static final int KEY_DOWN = 0x13;
    public static final int KEY_LEFT = 0x10;
    public static final int KEY_RIGHT = 0x12;
    public static final int KEY_SELECT = 0x14;
    public static final int KEY_SOFT1 = 0x15;
    public static final int KEY_SOFT2 = 0x16;

    public static final int KEY_PRESSED_EVENT = 0;
    public static final int KEY_RELEASED_EVENT = 1;
    public static final int MEDIA_EVENT = 8;
    public static final int RESUME_VM_EVENT = 4;
    public static final int RESET_VM_EVENT = 5;
    public static final int UPDATE_VM_EVENT = 6;
    public static final int TIMER_EXPIRED_EVENT = 7;

    protected static Frame current = null;

    protected Display() { }

    public static Frame getCurrent() { return current; }

    public static int getHeight() { return MobilePlatform.lcdHeight; }

    public static int getWidth() { return MobilePlatform.lcdWidth; }

    public static boolean isColor() { return true; }

    public static int numColors() { return 167772162; }

    public static void setCurrent(Frame frame) 
    {
        
        if (frame == null) { throw new NullPointerException("Frame cannot be null."); }
        if (frame instanceof Dialog) { throw new IllegalArgumentException("Cannot set a dialog as the current frame."); }
        
        if(frame == current) { return; }

        current = frame;
    }
}