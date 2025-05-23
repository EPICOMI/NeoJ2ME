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

public abstract interface PhoneData 
{
	public static final int SORT_ASCENDING = 0;
	public static final int SORT_DESCENDING = 1;

	public abstract void close();

	public abstract String getListType();

	public abstract DataEnumeration elements(int paramInt1, int paramInt2, int paramInt3) throws IOException;

	public abstract void createElement(DataElement paramDataElement) throws IOException;

	public abstract void delete(DataElement paramDataElement) throws IOException;

	public abstract void importElementRawData(byte[] paramArrayOfByte) throws IOException;

	public abstract byte[] exportElementRawData(DataElement paramDataElement) throws IOException;

	public abstract int getListMaxCount() throws IOException;
}