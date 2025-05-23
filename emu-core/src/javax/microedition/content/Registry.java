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

import java.util.ArrayList;
import java.util.List;

// TODO: Finish implementing this, if needed
public class Registry 
{

    private static List<Registry> registries = new ArrayList<>();
    String registryName;
    String ID;
    String[] actions, suffixes, types;
    ActionNameMap[] actionNames;
    ContentHandlerServer server;
    ResponseListener listener;
    Invocation invocation;

    public Registry(String classname) 
    { 
        this.registryName = classname;
        registries.add(this);
    }

    public void cancelGetResponse() { }

    public ContentHandler[] findHandler(Invocation invocation) { return null; }

    public ContentHandler[] forAction(String action) { return null; }

    public ContentHandler forID(String ID, boolean exact) { return null; }

    public ContentHandler[] forSuffix(String suffix) { return null; }

    public ContentHandler[] forType(String type) { return null; }

    public String[] getActions() { return actions; }

    public String getID() { return ID; }

    public String[] getIDs() { return null; }

    public static Registry getRegistry(String classname) 
    {
        for(Registry registry : registries) 
        {
            if(registry.registryName.equals(classname)) { return registry; }
        }
        return null; 
    }

    public Invocation getResponse(boolean wait) { return null; }

    public static ContentHandlerServer getServer(String classname) 
    { 
        return null; 
    }

    public String[] getSuffixes() { return suffixes; }

    public String[] getTypes() { return types; }

    public boolean invoke(Invocation invocation) 
    {
        invocation.invokerID = "";
        invocation.invokerAuth = "";
        invocation.invokerName = "";
        this.invocation = invocation;
        return false; 
    }

    public boolean invoke(Invocation invocation, Invocation previous) 
    {
        invocation.invokerID = "";
        invocation.invokerAuth = "";
        invocation.invokerName = "";
        invocation.setPrevious(previous);
        this.invocation = invocation;
        return false; 
    }

    public ContentHandlerServer register(String classname, String[] types, String[] suffixes, String[] actions, ActionNameMap[] actionnames, String ID, String[] accessAllowed) 
    {
        for(int i = 0; i < registries.size(); i++) 
        {
            if(registries.get(i).registryName == classname) { registries.remove(i); }
        }

        this.registryName = classname;

        this.ID = ID;
        this.actions = actions;
        this.suffixes = suffixes;
        this.types = types;
        this.actionNames = actionnames;

        registries.add(this);

        return null;
    }

    public boolean reinvoke(Invocation invocation) { return false; }

    public void setListener(ResponseListener listener) { this.listener = listener; }

    public boolean unregister(String classname) { return false; }
}
