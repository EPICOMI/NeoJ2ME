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

public interface ContentHandler 
{
    public static final String ACTION_DELETE       = "delete";
    public static final String ACTION_EDIT         = "edit";
    public static final String ACTION_EXECUTE      = "execute";
    public static final String ACTION_INSTALL      = "install";
    public static final String ACTION_INSTALL_ONLY = "install_only";
    public static final String ACTION_NEW          = "new";
    public static final String ACTION_OPEN         = "open";
    public static final String ACTION_PRINT        = "print";
    public static final String ACTION_REMOVE       = "remove";
    public static final String ACTION_SAVE         = "save";
    public static final String ACTION_SELECT       = "select";
    public static final String ACTION_SEND         = "send";
    public static final String ACTION_STOP         = "stop";
    public static final String UNIVERSAL_TYPE      = "*";

    public String getAction();

    public int getActionCount();

    public ActionNameMap getActionNameMap();

    public ActionNameMap getActionNameMap(int index);

    public ActionNameMap getActionNameMap(String locale);

    public int getActionNameMapCount();

    public String getAppName();

    public String getAuthority();

    public String getID();

    public String getSuffix(int index);

    public int getSuffixCount();

    public String getType(int index);

    public int getTypeCount();

    public String getVersion();

    public boolean hasAction(String action);

    public boolean hasSuffix(String suffix);

    public boolean hasType(String type);
}