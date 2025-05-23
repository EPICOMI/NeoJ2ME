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

public abstract interface MailTransportListener 
{
	public static final int MAIL_SUCCEEDED = 0;
	public static final int MAIL_FAILED = -1;
	public static final int MAIL_STOP = -2;
	public static final int MAIL_PART_FAILED = -3;
	public static final int MAIL_UNKNOWN = -4;

	public abstract void mailSent(int paramInt);

	public abstract void messageReceived(int paramInt);
}