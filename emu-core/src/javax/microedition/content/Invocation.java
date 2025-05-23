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
package javax.microedition.content;

// TODO: Not ideal, it's here just so FreeJ2ME builds without modifying the Connection stubs below
import com.siemens.mp.io.Connection;

public final class Invocation 
{

    public static final int	ACTIVE = 2;
    public static final int CANCELLED = 6;
    public static final int ERROR = 7;
    public static final int HOLD = 4;
    public static final int INIT = 1;
    public static final int INITIATED = 8;
    public static final int OK = 5;
    public static final int WAITING = 3;

    boolean responseRequired;
    String url, type, ID, action, username, invokerName, invokerID, invokerAuth;
    String[] args;
    char[] password;
    byte[] data;
    int status;
    Invocation prevInv;

    public Invocation() { }

    public Invocation(String url) { this.url = url; }

    public Invocation(String url, String type) 
    { 
        this.url = url;
        this.type = type;
    }

    public Invocation(String url, String type, String ID) 
    {
        this.url = url;
        this.type = type;
        this.ID = ID;
    }

    public Invocation(String url, String type, String ID, boolean responseRequired, String action) 
    {
        this.url = url;
        this.type = type;
        this.ID = ID;
        this.responseRequired = responseRequired;
        this.action = action;
    }

    public String findType() { return type; }

    public String getAction() { return action; }

    public String[] getArgs() { return args; }

    public byte[] getData() { return data; }

    public String getID() { return ID; }

    public String getInvokingAppName() { return invokerName; }

    public String getInvokingAuthority() { return invokerAuth; }

    public String getInvokingID() { return invokerID; }

    public Invocation getPrevious() { return prevInv; }

    public boolean getResponseRequired() { return responseRequired; }

    public int getStatus() { return status; }

    public String getType() { return type; }

    public String getURL() { return url; }

    public Connection open(boolean timeouts) { return new Connection(url); }

    public void setAction(String action) { this.action = action; }

    public void setArgs(String[] args) { this.args = args; }

    public void setCredentials(String username, char[] password) 
    {
        this.username = username;
        this.password = password;
    }

    public void setData(byte[] data) { this.data = data; }

    public void setID(String ID) { this.ID = ID; }

    public void setResponseRequired(boolean responseRequired) { this.responseRequired = responseRequired; }

    public void setType(String type) { this.type = type; }

    public void setURL(String url) { this.url = url; }


    public void setPrevious(Invocation prevInv) { this.prevInv = prevInv; }
}