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
package com.j_phone.phonedata;

import java.io.IOException;

public abstract interface AddressBook extends PhoneData 
{
	public static final int GROUP_SEARCH = 1;
	public static final int KANA_SEARCH = 2;
	public static final int NUMBER_SEARCH = 3;
	public static final int MAIL_ADDRESS_SEARCH = 4;

	public abstract int[] getGroupNoList() throws IOException;

	public abstract String getGroupName(int paramInt) throws IOException;

	public abstract int getPhoneNumberMaxCount() throws IOException;

	public abstract int getMailAddressMaxCount() throws IOException;

	public abstract DataEnumeration elements(int paramInt1, String paramString, int paramInt2, int paramInt3) throws IOException;
}