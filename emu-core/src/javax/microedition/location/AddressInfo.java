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
package javax.microedition.location;

public class AddressInfo 
{
    public static final int	BUILDING_FLOOR = 11;
    public static final int BUILDING_NAME = 10;
    public static final int BUILDING_ROOM = 12;
    public static final int BUILDING_ZONE = 13;
    public static final int CITY = 4;
    public static final int COUNTRY = 7;
    public static final int COUNTRY_CODE = 8;
    public static final int COUNTY = 5;
    public static final int CROSSING1 = 14;
    public static final int CROSSING2 = 15;
    public static final int DISTRICT = 9;
    public static final int EXTENSION = 1;
    public static final int PHONE_NUMBER = 17;
    public static final int POSTAL_CODE = 3;
    public static final int STATE = 6;
    public static final int STREET = 2;
    public static final int URL = 16;

    private String[] values;
    public AddressInfo() 
    { 
        values = new String[17];
    }

    // constants go from 1 to 17, so here we go from 0 to 16
    public String getField(int field) 
    {
        if(field < 1 || field > 17) { throw new IllegalArgumentException("Invalid field value"); }
        return values[field-1]; 
    }

    public void setField(int field, String value) 
    {
        if(field < 1 || field > 17) { throw new IllegalArgumentException("Invalid field value"); }
        values[field-1] = value;
    }
}