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
package com.siemens.mp.media;

public interface PlayerListener
{ 

	public static final String BUFFERING_STARTED = "BUFFERING_STARTED";
	public static final String BUFFERING_STOPPED = "BUFFERING_STOPPED";
	public static final String CLOSED = "CLOSED";
	public static final String DEVICE_AVAILABLE = "DEVICE_AVAILABLE";
	public static final String DEVICE_UNAVAILABLE = "DEVICE_UNAVAILABLE";
	public static final String DURATION_UPDATED = "DURATION_UPDATED";
	public static final String END_OF_MEDIA = "END_OF_MEDIA";
	public static final String ERROR = "ERROR";
	public static final String RECORD_ERROR = "RECORD_ERROR";
	public static final String RECORD_STARTED = "RECORD_STARTED";
	public static final String RECORD_STOPPED = "RECORD_STOPPED";
	public static final String SIZE_CHANGED = "SIZE_CHANGED";
	public static final String STARTED = "STARTED";
	public static final String STOPPED = "STOPPED";
	public static final String STOPPED_AT_TIME = "STOPPED_AT_TIME";
	public static final String VOLUME_CHANGED = "VOLUME_CHANGED";

	public void playerUpdate(com.siemens.mp.media.Player player, String event, Object eventData);
}