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
package com.nttdocomo.io;

public class ConnectionException extends java.io.IOException 
{

    public static final int UNDEFINED = 0;
    public static final int ILLEGAL_STATE = 1;
    public static final int NO_RESOURCE = 2;
    public static final int RESOURCE_BUSY = 3;
    public static final int NO_USE = 4;
    public static final int OUT_OF_SERVICE = 5;
    public static final int IMODE_LOCKED = 6;
    public static final int TIMEOUT = 7;
    public static final int USER_ABORT = 8;
    public static final int SYSTEM_ABORT = 9;
    public static final int HTTP_ERROR = 10;
    public static final int SCRATCHPAD_OVERSIZE = 11;
    public static final int SELF_MODE = 13;
    public static final int SSL_ERROR = 14;

    private final int status;

    public ConnectionException() 
    { 
        super(); 
        this.status = UNDEFINED; 
    }

    public ConnectionException(int status) 
    { 
        super(); 
        this.status = status; 
    }

    public ConnectionException(int status, String msg) 
    {
        super(msg);
        this.status = status;
    }

    public int getStatus() { return status; }
}