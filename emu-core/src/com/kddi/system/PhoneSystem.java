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
package com.kddi.system;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

public class PhoneSystem 
{

    public static final int ATTR_COLOR = 0;
    public static final int ATTR_ONOFF = 1;
    public static final int ATTR_PATTERN = 2;
    public static final int CHARGING = 3;
    public static final int COLOR_BLUE = 4;
    public static final int COLOR_DEFAULT = 5;
    public static final int COLOR_GREEN = 6;
    public static final int COLOR_RED = 7;
    public static final int DEVICE_OFF = 8;
    public static final int DEVICE_ON = 9;
    public static final int HIGH = 10;
    public static final int LOW = 11;
    public static final int MAX = 12;
    public static final int MAX_TIMEOUT = 60000;
    public static final int MIDDLE = 13;
    public static final int NO_SUPPORT = 14;
    public static final int NONE = 15;
    public static final int PATTERN1 = 16;
    public static final int PATTERN2 = 17;
    public static final int PATTERN3 = 18;
    public static final int PATTERN4 = 19;
    public static final int PATTERN5 = 20;
    public static final int PATTERN6 = 21;
    public static final int PATTERN7 = 22;
    public static final int PATTERN8 = 23;

    public static String getID() { return "00AA"; }

    public static int getIntensity() { return MAX; }

    public static int getPowerSupply() { return MAX; }

    public static int getIncallLedStatus(int attr) { return DEVICE_OFF; }

    public static int getVibrationStatus(int attr) { return DEVICE_OFF; }

    public static int getDisplayBacklightStatus() { return DEVICE_OFF; }

    public static void onIncallLed(int color, int timeout) { Mobile.getDisplay().flashBacklight(timeout); }

    public static void onIncallLed(int color, int pattern, int timeout) { Mobile.getDisplay().flashBacklight(timeout); }

    public static void offIncallLed() { Mobile.getDisplay().flashBacklight(0);  }

    public static void onVibration(int timeout) { Mobile.getDisplay().vibrate(timeout); }

    public static void onVibration(int pattern, int timeout) { Mobile.getDisplay().vibrate(timeout); }

    public static void offVibration() { Mobile.getDisplay().vibrate(0); }

    public static void onDisplayBacklight() { Mobile.getDisplay().flashBacklight(Integer.MAX_VALUE); }

    public static void offDisplayBacklight() { Mobile.getDisplay().flashBacklight(0); }

    // Kddi seems to use the same key layout as vodafone, looking at the documentation
    public static int getKeyState(boolean eightDirections) 
    {
        return MobilePlatform.vodafoneKeyState;
    }

    public static boolean isSupportedEightDirections() { return false; }

    public static int getColorDepth(int color) { return 8; /* 8 bits per color */ }
}