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

public class TextMessageImpl implements TextMessage 
{
	protected String address;
	protected String messageData;
	protected long timestamp;

	public TextMessageImpl(final String address, final String messageData) 
	{
		this.address = address;
		this.messageData = messageData;
		this.timestamp = System.currentTimeMillis();
	}

	public String getPayloadText() { return this.messageData; }

	public void setPayloadText(final String data) { this.messageData = data; }

	public String getAddress() { return this.address; }

	public void setAddress(final String address) { this.address = address; }

	public Date getTimestamp() { return new Date(this.timestamp); }

}
