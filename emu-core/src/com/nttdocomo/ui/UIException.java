
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
package com.nttdocomo.ui;

public class UIException extends RuntimeException 
{
    public static final int UNDEFINED = 0;
    public static final int ILLEGAL_STATE = 1;
    public static final int NO_RESOURCES = 2;
    public static final int BUSY_RESOURCE = 3;
    public static final int UNSUPPORTED_FORMAT = 4;

    protected static final int STATUS_FIRST = 0;
    protected static final int STATUS_LAST = 63;

    private final int status;

    public UIException() 
    {
        super();
        this.status = UNDEFINED;
    }

    public UIException(int status) 
    {
        super();
        this.status = status;
    }

    public UIException(int status, String msg) 
    {
        super(msg);
        this.status = status;
    }

    public int getStatus() { return status; }
}