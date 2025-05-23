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
package com.vodafone.v10.system.device;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

public class DeviceControl 
{
	public static final int BATTERY = 1;
	public static final int FIELD_INTENSITY = 2;
	public static final int KEY_STATE = 3;
	public static final int VIBRATION = 4;
	public static final int BACK_LIGHT = 5;
	public static final int EIGHT_DIRECTIONS = 6;

	private static DeviceControl instance;

	public static synchronized DeviceControl getDefaultDeviceControl()
    {
		if (instance == null) { instance = new DeviceControl(); }
		return instance;
	}

	public static void setMailListener(MailListener listener) { }

	public static void setRingStateListener(RingStateListener listener) { }

	public static void setScheduledAlarmListener(ScheduledAlarmListener listener) { }

	public static void setTelephonyListener(TelephonyListener listener) { }

	public void blink(int lighting, int extinction, int repeat) { Mobile.getDisplay().vodafoneFlashBacklight(lighting, extinction, repeat); }

	public int getDeviceState(int deviceNo)
    {
		switch (deviceNo)
        {
			case BATTERY:
			case FIELD_INTENSITY:
				return 100;
			case KEY_STATE:
				return MobilePlatform.vodafoneKeyState;
			default:
				throw new IllegalStateException("Invalid device No for getDeviceState");
		}
	}

	public boolean getKeyRepeatState(int key) { return false; }

	public boolean isDeviceActive(int deviceNo) { return false; }

	public boolean setDeviceActive(int deviceNo, boolean active)
    {
		switch (deviceNo)
        {
			case BACK_LIGHT:
			case EIGHT_DIRECTIONS:
				break;
			case VIBRATION:
				int duration = active ? 2000 : 0;
				Mobile.getDisplay().vibrate(duration);
				break;
			default:
				throw new IllegalStateException("Invalid device No for setDeviceActive");
		}
		return true;
	}

	public boolean setKeyRepeatState(int key, boolean state) { return false; }
}