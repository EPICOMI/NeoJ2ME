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

public class PhoneSystem 
{

    public static final int ATTR_BACKLIGHT_OFF = 0;
    public static final int ATTR_BACKLIGHT_ON = 1;
    public static final int ATTR_VIBRATOR_OFF = 0;
    public static final int ATTR_VIBRATOR_ON = 1;
    public static final int DEV_BACKLIGHT = 0;
    public static final int DEV_VIBRATOR = 1;
    public static final int SOUND_INFO = 0;
    public static final int SOUND_WARNING = 1;
    public static final int SOUND_ERROR = 2;
    public static final int SOUND_ALARM = 3;
    public static final int SOUND_CONFIRM = 4;

    private PhoneSystem() {}

    public static void setAttribute(int attr, int value) 
    {
        if (!isValidAttribute(attr, value)) { throw new IllegalArgumentException("Invalid attribute or value."); }
    }

    public static void playSound(int type) 
    {
        if (type < SOUND_INFO || type > SOUND_CONFIRM) { throw new IllegalArgumentException("Invalid sound type."); }
    }

    private static boolean isValidAttribute(int attr, int value) 
    {
        if (attr == DEV_BACKLIGHT) 
        {
            return value == ATTR_BACKLIGHT_OFF || value == ATTR_BACKLIGHT_ON;
        } 
        else if (attr == DEV_VIBRATOR) 
        {
            return value == ATTR_VIBRATOR_OFF || value == ATTR_VIBRATOR_ON;
        }
        return false; 
    }
}