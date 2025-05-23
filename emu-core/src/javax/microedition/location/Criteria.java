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

public class Criteria 
{

    public static final int	NO_REQUIREMENT = 0;
    public static final int	POWER_USAGE_HIGH = 3;
    public static final int	POWER_USAGE_LOW = 1;
    public static final int	POWER_USAGE_MEDIUM = 2;

    private int horizontalAccuracy = 0;
	private int verticalAccuracy = 0;
	private int maxResponseTime = 0;
	private int powerConsumption = 0;
	private boolean costAllowed = true;
	private boolean speedRequired = false;
	private boolean altitudeRequired = false;
	private boolean addressInfoRequired = false;

    public Criteria() { }

	public int getPreferredPowerConsumption() { return this.powerConsumption; }

	public boolean isAllowedToCost() { return this.costAllowed; }

	public int getVerticalAccuracy() { return this.verticalAccuracy; }

	public int getHorizontalAccuracy() { return this.horizontalAccuracy; }

	public int getPreferredResponseTime() { return this.maxResponseTime; }

	public boolean isSpeedAndCourseRequired() { return this.speedRequired; }

	public boolean isAltitudeRequired() { return this.altitudeRequired; }

	public boolean isAddressInfoRequired() { return this.addressInfoRequired; }

	public void setHorizontalAccuracy(int accuracy) { this.horizontalAccuracy = accuracy; }

	public void setVerticalAccuracy(int accuracy) { this.verticalAccuracy = accuracy; }

	public void setPreferredResponseTime(int time) { this.maxResponseTime = time; }

	public void setPreferredPowerConsumption(int level) { this.powerConsumption = level; }

	public void setCostAllowed(boolean costAllowed) { this.costAllowed = costAllowed; }

	public void setSpeedAndCourseRequired(boolean speedAndCourseRequired) { this.speedRequired = speedAndCourseRequired; }

	public void setAltitudeRequired(boolean altitudeRequired) { this.altitudeRequired = altitudeRequired; }

	public void setAddressInfoRequired(boolean addressInfoRequired) { this.addressInfoRequired = addressInfoRequired; }
}