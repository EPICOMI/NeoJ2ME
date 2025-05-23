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
package javax.microedition.location;

public abstract class LocationProvider 
{

	public static final int AVAILABLE = 1;
	public static final int TEMPORARILY_UNAVAILABLE = 2;
	public static final int OUT_OF_SERVICE = 3;

    protected LocationProvider() { /* TODO */ }

	public static LocationProvider getInstance(Criteria criteria) throws LocationException 
    {
		// TODO (Idea: Maybe implement a spoofer through LCDUI?)
		return null;
	}

	public abstract Location getLocation(int timeout) throws LocationException, InterruptedException;

	public abstract void setLocationListener(LocationListener listener, int interval, int timeout, int maxAge);

	public static Location getLastKnownLocation() 
    {
		// TODO (Idea: Get the last set location of said spoofer)
		return null;
	}

	public abstract int getState();

	public abstract void reset();

	public static void addProximityListener(ProximityListener listener, Coordinates coordinates, float proximityRadius) throws LocationException { throw new LocationException(); }

	public static void removeProximityListener(ProximityListener listener) { }
}