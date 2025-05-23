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

public class Coordinates 
{

    public static final int	DD_MM = 2;
    public static final int	DD_MM_SS = 1;

    protected double latitude, longitude;
    protected float altitude;
    public Coordinates(double latitude, double longitude, float altitude) 
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public float azimuthTo(Coordinates to) 
    {
        return 0.0f;
    }

    public String convert(double coordinate, int outputType) 
    {
        return "";
    }

    public static double convert(String coordinate) 
    {
        return 0.0d;
    }

    public float distance(Coordinates to) 
    {
        return 0.0f;
    }

    public float getAltitude() { return this.altitude; }

    public double getLatitude() { return this.latitude; }

    public double getLongitude() { return this.longitude; }

    public void setAltitude(float altitude) { this.altitude = altitude; }

    public void setLatitude(float latitude) { this.latitude = latitude; }

    public void setLongitude(float longitude) { this.longitude = longitude; }
}