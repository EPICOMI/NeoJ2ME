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

public class QualifiedCoordinates extends Coordinates 
{
	float horizontalAccuracy = Float.NaN;
	float verticalAccuracy = Float.NaN;

	public QualifiedCoordinates(double latitude, double longitude, float altitude, float horizontalAccuracy, float verticalAccuracy) 
    {
		super(latitude, longitude, altitude);
		setHorizontalAccuracy(horizontalAccuracy);
		setVerticalAccuracy(verticalAccuracy);
	}

	QualifiedCoordinates() { this(0.0D, 0.0D, Float.NaN, Float.NaN, Float.NaN); }

	QualifiedCoordinates(QualifiedCoordinates other) 
    {
		this(other.latitude, other.longitude, other.altitude, other.horizontalAccuracy, other.verticalAccuracy);
	}

	public float getHorizontalAccuracy() { return this.horizontalAccuracy; }

	public float getVerticalAccuracy() { return this.verticalAccuracy; }

	public void setHorizontalAccuracy(float horizontalAccuracy) 
    {
		if ((Float.isNaN(horizontalAccuracy)) || (horizontalAccuracy >= 0.0F)) { this.horizontalAccuracy = horizontalAccuracy; } 
        else { throw new IllegalArgumentException("invalid horizontal accuracy"); }
	}

	public void setVerticalAccuracy(float verticalAccuracy) 
    {
		if ((Float.isNaN(verticalAccuracy)) || (verticalAccuracy >= 0.0F)) { this.verticalAccuracy = verticalAccuracy; } 
        else { throw new IllegalArgumentException("Invalid vertical accuracy"); }
	}

	public boolean equals(Object other) 
    {
		if (other == this) { return true; }
		if (!super.equals(other)) { return false; }
		if (!(other instanceof QualifiedCoordinates)) { return false; }

		QualifiedCoordinates o = (QualifiedCoordinates) other;

		if (Float.floatToIntBits(getHorizontalAccuracy()) != Float.floatToIntBits(o.getHorizontalAccuracy())) { return false; }
		if (Float.floatToIntBits(getVerticalAccuracy()) != Float.floatToIntBits(o.getVerticalAccuracy())) { return false; }
		return true;
	}

	public int hashCode() 
    {
		int result = 17;
		result = 37 * result + Float.floatToIntBits(getHorizontalAccuracy());
		result = 37 * result + Float.floatToIntBits(getVerticalAccuracy());
		result = 37 * result + super.hashCode();
		return result;
	}
    
}