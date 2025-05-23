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

public final class BackLight 
{

    public BackLight() { }

    public static int getColor() { return Mobile.lcdMaskColors[Mobile.maskIndex]; }

    public static int numColors() { return 16777216; }

    public static void off() { Mobile.getDisplay().flashBacklight(0); }

    public static void on(int timeout) { Mobile.maskIndex = 6; Mobile.getDisplay().flashBacklight(timeout); }

    public static void setColor(int RGB) { Mobile.lcdMaskColors[6] = RGB; }
}