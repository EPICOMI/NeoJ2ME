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
package com.kddi.camera;

public class BarcodeContent
{

    public static final int BINARY = 0;
    public static final int STRING = 1;

    private final int type;
    private final byte[] binaryData;
    private final String stringData;

    public BarcodeContent(int type, byte[] data) 
    {
        this.type = type;
        this.binaryData = data;
        this.stringData = null;
    }

    public BarcodeContent(int type, String data) 
    {
        this.type = type;
        this.stringData = data;
        this.binaryData = null;
    }

    public byte[] getBytes() 
    {
        return (type == BINARY) ? binaryData : null;
    }

    public String getString() 
    {
        return (type == STRING) ? stringData : null;
    }

    public int getDataType() { return type; }

    public int getCodeType() { return type; }
}