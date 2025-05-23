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
package com.vodafone.io;

import org.recompile.mobile.Mobile;
public class RemoteControl 
{

    public static void send(int numOfData, RemoteControlData[] data) 
    {
        // Placeholder for actual implementation
        if (data == null) { throw new NullPointerException("Data array cannot be null."); }
        if (numOfData <= 0) { throw new IllegalArgumentException("Number of data must be greater than zero."); }
        if (numOfData > data.length) { throw new ArrayIndexOutOfBoundsException("numOfData exceeds the length of the data array."); }
        for (int i = 0; i < numOfData; i++) 
        {
            if (data[i] == null) { throw new NullPointerException("Data at index " + i + " is null."); }
        }
        
        Mobile.log(Mobile.LOG_INFO, RemoteControl.class.getPackage().getName() + "." + RemoteControl.class.getSimpleName() + ": " + "RemoteControlData send requested!");
    }
}