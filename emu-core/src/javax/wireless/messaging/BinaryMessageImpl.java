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
package javax.wireless.messaging;

import java.util.Date;

public class BinaryMessageImpl implements BinaryMessage 
{
	protected String address;
	protected byte[] messageData;
	protected long timestamp;

	public BinaryMessageImpl(String address, byte[] data) 
	{
		super();
		this.messageData = data;
		this.address = address;
		this.timestamp = System.currentTimeMillis();
	}

	public byte[] getPayloadData() { return this.messageData; }

	public void setPayloadData(byte[] data) { this.messageData = data; }

	public String getAddress() { return this.address; }

	public void setAddress(String address) { this.address = address; }

	public Date getTimestamp() { return new Date(this.timestamp); }

}