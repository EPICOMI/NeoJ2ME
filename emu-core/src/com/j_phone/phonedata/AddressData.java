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

public abstract interface AddressData extends DataElement 
{
	public static final int MEMORYDIAL_NO_INFO = 1;
	public static final int NAME_INFO = 2;
	public static final int KANA_INFO = 3;
	public static final int PHONE_NUMBER_INFO = 4;
	public static final int EMAIL_INFO = 5;
	public static final int GROUP_NO_INFO = 6;
	public static final int SECRET_INFO = 7;
	public static final int PHOTO_INFO = 8;
	public static final int GEO_ACCURACY = 9;
	public static final int GEO_GEODETIC_DATUM = 10;
	public static final int GEO_LATITUDE = 11;
	public static final int GEO_LONGITUDE = 12;
}