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

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

import javax.microedition.midlet.MIDlet;

public class DeviceControl 
{
	public static final int BATTERY = 1;
	public static final int FIELD_INTENSITY = 2;
	public static final int KEY_STATE = 3;
	public static final int VIBRATION = 4;
	public static final int BACK_LIGHT = 5;
	public static final int EIGHT_DIRECTIONS = 6;
	public static final int FLIP_STATE = 7;
	public static final int MEMORY_CARD = 8;
	public static final int SPEAKER_STATE = 9;
	public static final int ENHANCED_KEY_STATE = 10;
	public static final int FLIP_OPENED = 0;
	public static final int FLIP_CLOSED = 1;
	public static final int MEMORY_CARD_OFF = 0;
	public static final int MEMORY_CARD_WRITABLE = 1;
	public static final int MEMORY_CARD_WRITE_PROTECTED = 2;
	public static final int MEMORY_CARD_READ_ONLY = 3;
	public static final int NEW_ARRIVAL_STATE_CALL = 1;
	public static final int NEW_ARRIVAL_STATE_MAIL = 2;
	public static final int SPEAKER_INTERNAL = 1;
	public static final int SPEAKER_EXTERNAL = 2;
	public static final int RAB_GPRS = 1;
	public static final int RAB_R99 = 2;
	public static final int RAB_HSDPA_C6 = 3;
	public static final int RAB_HSDPA_C7 = 4;
	public static final int RAB_NULL = 0;
	public static final int RAB_MEASUREMENT = -1;
	public static final int RAB_INDEFINITE = -99;
	public static final int STYLE_PORTRAIT = 0;
	public static final int STYLE_LANDSCAPE = 1;

	private static final DeviceControl deviceControl = new DeviceControl();

	public static DeviceControl getDefaultDeviceControl() { return deviceControl; }

	public int getDeviceState(int device) 
	{ 
		if (device != 3) { return 0; }
		else { return MobilePlatform.vodafoneKeyState; }
	}

	public boolean isDeviceActive(int device) { return false; }

	public boolean setDeviceActive(int device, boolean active) 
    {
		if (device == VIBRATION) 
        {
			if (active) { Mobile.getDisplay().vibrate(3000); } 
            else { Mobile.getDisplay().vibrate(0); }
			return true;
		}
		return true;
	}

	public void blink(int a, int b, int c) { }

	public boolean setKeyRepeatState(int key, boolean enable) { return false; }

	public boolean getKeyRepeatState(int key) { return false; }

	public int getLatitude() { return 0; }

	public int getLongitude() { return 0; }

	public String getPlaceName() { return null; }

	public void updateLocationInfo() throws RuntimeException { }

	public int getNewArrivalState() { return 0; }

	public String getWakeupParam(MIDlet midlet, String name) { return null; }

	public String getMyTelNumber() { return "123456789"; }

	public String getIMEI() 
    {
		return System.getProperty("device.imei");
	}

	public int getTransmissionRate() { return 0; }

	public int getStyle() { return 0; }

	public static void setMailListener(MailListener mailListener) { }

	public static void setScheduledAlarmListener(ScheduledAlarmListener scheduledAlarmListener) { }

	public static void setTelephonyListener(TelephonyListener telephonyListener) { }

	public static void setRingStateListener(RingStateListener ringStateListener) { }

	public static void setBodyOpenListener(BodyOpenListener bodyOpenListener) { }

	public static void setLocationUpdateListener(LocationUpdateListener locationUpdateListener) { }

	public static void setPhoneStateListener(PhoneStateListener phoneStateListener) { }

	public static void setMemoryCardListener(MemoryCardListener memoryCardListener) { }

	public static void setSpeakerStateListener(SpeakerStateListener speakerStateListener) { }

	public static void setStyleChangedListener(StyleChangedListener styleChangedListener) { }
}