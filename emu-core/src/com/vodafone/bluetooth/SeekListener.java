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

public abstract interface SeekListener 
{
	public static final int COMPLETED = 0;
	public static final int ERROR = 1;
	public static final int CANCELLED = 2;
	public static final int SERVICE_NOT_FOUND = 3;
	public static final int PUSH_SERVER_FOUND = 4;
	public static final int MASK_SERVICE_CLASSES = 16719872;
	public static final int MASK_MAJOR_DEVICE_CLASS = 7936;
	public static final int MASK_MINOR_DEVICE_CLASS = 252;

	public abstract void foundDevice(Device paramDevice, int paramInt);

	public abstract void terminatedDeviceSeek(int paramInt);

	public abstract void foundService(RemoteService[] paramArrayOfRemoteService);

	public abstract void terminatedServiceSeek(Device paramDevice, int paramInt);
}