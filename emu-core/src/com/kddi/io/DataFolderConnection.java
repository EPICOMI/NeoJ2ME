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
package com.kddi.io;

public interface DataFolderConnection extends javax.microedition.io.ContentConnection 
{

    int FILE_TYPE_ANIME = 0;
    int FILE_TYPE_ETC = 1;
    int FILE_TYPE_GRAPHIC = 2;
    int FILE_TYPE_KARAOKE = 3;
    int FILE_TYPE_MELODY = 4;
    int FILE_TYPE_MOVIE = 5;
    int FILE_TYPE_PHOTO = 6;

    String[] getList() throws java.io.IOException;

    String[] getList(int type) throws java.io.IOException;

    String getName() throws java.io.IOException;

    String getType();

    boolean isCopyrighted() throws java.io.IOException;
}