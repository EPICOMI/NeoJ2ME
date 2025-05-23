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

public class Orientation 
{
	private float azimuth;
	private float pitch;
	private float roll;
	private boolean isMagnetic;

	public Orientation(float azimuth, boolean isMagnetic, float pitch, float roll) 
    {
		this.azimuth = azimuth;
		this.isMagnetic = isMagnetic;
		this.pitch = pitch;
		this.roll = roll;
	}

	public float getCompassAzimuth() { return this.azimuth; }

	public boolean isOrientationMagnetic() { return this.isMagnetic; }

	public float getPitch() { return this.pitch; }

	public float getRoll() { return this.roll; }

	public static Orientation getOrientation() throws LocationException { return null; }
}