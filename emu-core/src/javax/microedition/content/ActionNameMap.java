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

public final class ActionNameMap 
{

    String[] actions, actionnames;
    String locale; 
    public ActionNameMap (String[] actions, String[] actionnames, String locale) 
    { 
        if(actions == null || actionnames == null || locale == null) { throw new NullPointerException("ActionNameMap received null argument"); }
    
        this.actions = actions;
        this.actionnames = actionnames;
        this.locale = locale;
    }

    public String getAction(int index) 
    { 
        return actions[index];
    }

    public String getAction(String actionname) 
    { 
        for(int i = 0; i < actionnames.length; i ++) 
        {
            if(actionnames[i].equals(actionname)) { return actions[i]; }
        }

        return null;
    }

    public  String getActionName(int index) 
    { 
        return actionnames[index];
    }

    public String getActionName(String action) 
    { 
        for(int i = 0; i < actions.length; i ++) 
        {
            if(actions[i].equals(action)) { return actionnames[i]; }
        }
        return null;
    }

    public String getLocale() { return locale; }

    public int size() { return actions.length; }
}