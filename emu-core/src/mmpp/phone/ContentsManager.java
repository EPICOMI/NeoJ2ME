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
package mmpp.phone;

public class ContentsManager 
{

    public static int SUCCESS = 1;
    public static int CONTENTS_FULL = 2;
    public static int FAILURE = 3;

    public ContentsManager() { }

    public static String[] getListOfDesktop() { return new String[0]; }

    public static String[] getListOfMelody() { return new String[0]; }

    public static int getNumberOfDesktop() { return 0; }

    public static int getNumberOfMelody() { return 0; }

    public static int setCurrentDesktop(String name, byte[] contents, int offset, int len) throws IllegalArgumentException { return SUCCESS; }

    public static int setCurrentMelody(String name, byte[] contents, int offset, int len) throws IllegalArgumentException { return SUCCESS; }
}