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

public interface CMailConnection extends javax.microedition.io.DatagramConnection 
{

    int CONNECT_FAILURE = -1;
    int CONNECT_NO_SUPPORT = -2;
    int CONNECT_SUCCESS = 0;
    int SINGLE_MODE = 1;

    String getMessage() throws java.io.IOException;

    int getMode() throws java.io.IOException;

    String getNickName() throws java.io.IOException;

    String getTelNo() throws java.io.IOException;

    void sendTo() throws java.io.IOException;

    void setMessage(String message);

    void setNickName(String nickname);

    void setSingleMode();
}