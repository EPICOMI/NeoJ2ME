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
package com.j_phone.system;

import java.io.IOException;

public class MotionDetectiveSensor 
{
	public static final int POSTURE_INFO = 1;
	public static final int KEY_COMPATIBLE = 2;
	public static final int KEY_SENSOR = 3;
	public static final int CYCLE_20 = 1;
	public static final int CYCLE_40 = 2;
	public static final int CYCLE_60 = 3;
	public static final int CYCLE_80 = 4;
	public static final int CYCLE_100 = 5;

	public static final MotionDetectiveSensor getDefaultMotionDetectiveSensor() throws IOException 
    {
		return null;
	}

	public void startSensor(int paramInt1, int paramInt2) { }

	public void stopSensor() { }

	public PostureInfo getPostureInfoLatest() throws IOException { return null; }

	public PostureInfo getPostureInfoStack(int paramInt) throws IOException { return null; }

	public int getStackCount() { return 0; }

	public int getState() { return 0; }

	public void setNeutralPosition() throws IOException { }

	public void setDefaultNeutralPosition() throws IOException { }
}