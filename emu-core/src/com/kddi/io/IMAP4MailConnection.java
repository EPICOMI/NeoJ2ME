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

import com.kddi.system.PersonalInfo;

public interface IMAP4MailConnection extends javax.microedition.io.Connection 
{

    void addBcc(PersonalInfo person) throws java.io.IOException, java.lang.IllegalArgumentException;

    void addBcc(String address) throws java.io.IOException, java.lang.IllegalArgumentException;

    void addCc(PersonalInfo person) throws java.io.IOException, java.lang.IllegalArgumentException;

    void addCc(String address) throws java.io.IOException, java.lang.IllegalArgumentException;

    void addTo(PersonalInfo person) throws java.io.IOException, java.lang.IllegalArgumentException;

    void addTo(String address) throws java.io.IOException, java.lang.IllegalArgumentException;

    String[] getBcc() throws java.io.IOException;

    String[] getCc() throws java.io.IOException;

    String getContent() throws java.io.IOException;

    long getLength() throws java.io.IOException;

    String getSubject() throws java.io.IOException;

    String[] getTo() throws java.io.IOException;

    void send() throws java.io.IOException;

    void setContent(String content) throws java.io.IOException, java.lang.IllegalArgumentException;

    void setSubject(String subject) throws java.io.IOException, java.lang.IllegalArgumentException;
}