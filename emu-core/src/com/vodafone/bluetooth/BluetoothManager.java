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
package com.vodafone.bluetooth;

public class BluetoothManager 
{
	private BluetoothManager() { }

	public static final synchronized BluetoothManager getInstance() { return null; }

	public final synchronized String getFriendlyName() { return null; }

	public final int getMaxDevices() { return 0; }

	public final void startDeviceSeek(SeekListener paramSeekListener) { }

	public final synchronized boolean stopDeviceSeek() { return false; }

	public final void registerPushRequest(String paramString1, String paramString2, String paramString3) { }

	public synchronized String[] getPushRequest() { return null; }
}