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
package mmpp.media;

import org.recompile.mobile.Mobile;

// seems similar to Moto's FunLights... I'll assume it works the same way
public final class LED extends com.motorola.funlight.FunLight
{

    public LED() { }

    public static int getColor(int LEDnumber) { return deviceRegions[LEDnumber+1].getColor(); }

    public static int getColorDepth() { return 24; }

    public static int getNumber() { Mobile.funLightsEnabled = true; return deviceRegions.length-1; }

    public static void setColor(int LEDnumber, int RGBcolor) 
    { 
        deviceRegions[LEDnumber+1].setColor(RGBcolor);
    }
}