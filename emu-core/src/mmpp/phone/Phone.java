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
package mmpp.phone;

public final class Phone 
{
    
    public Phone() { }

    public static int getBASEID() { return 0; }

    public static int getBASELAT() { return 0; }

    public static int getBASELONG() { return 0; }

    public static int getNID() { return 0; }

    public static String getProperty(String key) { return null; }

    public static int getSID() { return 0;  }

    public static void invokeWAPBrowser(String url) { }

    public static void placeCall(String phoneNumber) { }
}