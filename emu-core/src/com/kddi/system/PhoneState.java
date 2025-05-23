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
package com.kddi.system;

public class PhoneState 
{

    public static final int FLIP_CLOSE = 1;
    public static final int FLIP_OPEN = 0;
    public static final int NORMAL_MODE = 0;
    public static final int SLEEP_MODE = 1;

    private static PhoneState instance;

    public static PhoneState getInstance() 
    {
        if (instance == null) { instance = new PhoneState(); }

        return instance;
    }

    public int getFlipState() { return FLIP_OPEN; }

    public int getOperationMode() { return NORMAL_MODE; }

    public void setOperationMode(int mode) 
    {
        if (mode != NORMAL_MODE && mode != SLEEP_MODE) 
        {
            throw new IllegalArgumentException("Invalid mode");
        }
    }

    public void setOperationModeListener(OperationModeListener listener) { }

    public void setFlipStateListener(FlipStateListener listener) { }
}