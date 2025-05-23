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
package com.nttdocomo.ui;

public final class Dialog extends Frame 
{
    public static final int BUTTON_OK = 0x0001;
    public static final int BUTTON_CANCEL = 0x0002;
    public static final int BUTTON_YES = 0x0004;
    public static final int BUTTON_NO = 0x0008;
    
    public static final int DIALOG_INFO = 0;
    public static final int DIALOG_WARNING = 1;
    public static final int DIALOG_ERROR = 2;
    public static final int DIALOG_YESNO = 3;
    public static final int DIALOG_YESNOCANCEL = 4;

    private String title;
    private String message;
    private int dialogType;

    public Dialog(int type, String title) 
    {
        super();
        if (type < DIALOG_INFO || type > DIALOG_YESNOCANCEL) { throw new IllegalArgumentException("Illegal dialog type"); }
        
        this.dialogType = type;
        this.title = (title != null) ? title : " ";
    }

    public void setBackground(int color) { super.setBackground(color); }

    public void setText(String msg) 
    {
        this.message = (msg != null) ? msg : " ";
    }

    public int show() throws UIException { return BUTTON_OK; }

    public void setSoftLabel(int key, String label) { }
}