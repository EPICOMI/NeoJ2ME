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
package com.vodafone.system;

import javax.microedition.midlet.MIDlet;

import org.recompile.mobile.MobilePlatform;

public class DeviceControl 
{
	public static final int BATTERY = 1;
	public static final int FIELD_INTENSITY = 2;
	public static final int FLIP_STATE = 7;
	public static final int FLIP_OPENED = 0;
	public static final int FLIP_CLOSED = 1;

	private static DeviceControl instance;

	public static final DeviceControl getDefaultDeviceControl() 
    {
		if (instance == null) { instance = new DeviceControl(); }
		return instance;
	}

	public int getDeviceState(int deviceNo) 
    {
		switch (deviceNo) 
        {
			case BATTERY:
			case FIELD_INTENSITY:
				return 100;
			case com.vodafone.v10.system.device.DeviceControl.KEY_STATE:
				return MobilePlatform.vodafoneKeyState;
			default:
				return 0;
		}
	}

	public String getWakeupParam(MIDlet paramMIDlet, String paramString) { return ""; }

	public static void setBodyOpenListener(BodyOpenListener paramBodyOpenListener) { }
}