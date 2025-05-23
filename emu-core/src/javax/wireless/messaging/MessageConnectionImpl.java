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

import org.recompile.mobile.Mobile;

public class MessageConnectionImpl implements MessageConnection 
{

	private String address;
	private MessageListener listener;
	boolean timeouts;

	public MessageConnectionImpl(String address) 
	{ 
		this.address = address; 
		Mobile.log(Mobile.LOG_WARNING, MessageConnection.class.getPackage().getName() + "." + MessageConnection.class.getSimpleName() + ": " + "New SMS Message to: "+ this.address);
	}

	public void close() 
	{
		this.address = null;
		this.listener = null;
	}

	public Message newMessage(String type) 
	{
		return this.newMessage(type, address);
	}

	public Message newMessage(String type, String address) 
	{
		if (type.equals("text")) { return new TextMessageImpl(address, null); }
		else if (type.equals("binary")) { return new BinaryMessageImpl(address, null); }
		return null;
	}

	public void send(Message message)
	{
		Mobile.log(Mobile.LOG_WARNING, MessageConnection.class.getPackage().getName() + "." + MessageConnection.class.getSimpleName() + ": " + "Message send requested to: "+ this.address);
	}

	public Message receive()
	{
		Mobile.log(Mobile.LOG_WARNING, MessageConnection.class.getPackage().getName() + "." + MessageConnection.class.getSimpleName() + ": " + "Message receive requested");
        return null;
    }

	public void setMessageListener(MessageListener listener)
	{
		this.listener = listener;
	}

	public int numberOfSegments(Message message) 
	{
		if (message instanceof TextMessageImpl) { return ((TextMessageImpl) message).messageData.length() / 160 + 1; }
		return 1;
	}

}

