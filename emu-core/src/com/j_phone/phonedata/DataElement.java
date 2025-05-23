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
import java.util.Date;

public abstract interface DataElement 
{
	public static final int STRING = 1;
	public static final int INT = 2;
	public static final int DATE = 3;
	public static final int BOOLEAN = 4;

	public abstract String getType();

	public abstract int getElementCount(int paramInt) throws IOException;

	public abstract int getDataType(int paramInt);

	public abstract String getString(int paramInt1, int paramInt2) throws IOException;

	public abstract Integer getInt(int paramInt1, int paramInt2) throws IOException;

	public abstract Date getDate(int paramInt1, int paramInt2) throws IOException;

	public abstract Boolean getBoolean(int paramInt1, int paramInt2) throws IOException;

	public abstract void setString(int paramInt1, int paramInt2, String paramString) throws IOException;

	public abstract void setInt(int paramInt1, int paramInt2, Integer paramInteger) throws IOException;

	public abstract void setBoolean(int paramInt1, int paramInt2, Boolean paramBoolean) throws IOException;

	public abstract boolean isListElement();

	public abstract DataElement createClone() throws IOException;
}