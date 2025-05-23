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

public class Location 
{

	public static final int MTE_SATELLITE = 1;
	public static final int MTE_TIMEDIFFERENCE = 2;
	public static final int MTE_TIMEOFARRIVAL = 4;
	public static final int MTE_CELLID = 8;
	public static final int MTE_SHORTRANGE = 16;
	public static final int MTE_ANGLEOFARRIVAL = 32;
	public static final int MTY_TERMINALBASED = 65536;
	public static final int MTY_NETWORKBASED = 131072;
	public static final int MTA_ASSISTED = 262144;
	public static final int MTA_UNASSISTED = 524288;
    
	private QualifiedCoordinates coords;
	private int locationMethod;
	private String nmea;

	protected Location(QualifiedCoordinates coords, int method, String nmea) 
    {
		this.coords = coords;
		this.locationMethod = method;
		this.nmea = nmea;
	}

	public boolean isValid() { return coords != null; }

	public long getTimestamp() { return System.currentTimeMillis(); }

	public QualifiedCoordinates getQualifiedCoordinates() 
    {
		if (coords == null) { return null; }

		return new QualifiedCoordinates(coords.getLatitude(), coords.getLongitude(), coords.getAltitude(), coords.getHorizontalAccuracy(), coords.getVerticalAccuracy());
	}

	public float getSpeed() 
    {
		return 0.0f;
	}

	public float getCourse() 
    {
		return 0.0f;
	}

	public int getLocationMethod() { return locationMethod; }

	public AddressInfo getAddressInfo() { return null; }

	public String getExtraInfo(String mimetype) 
    {
		if ("application/X-jsr179-location-nmea".equals(mimetype)) { return nmea; }
		return null;
	}
}