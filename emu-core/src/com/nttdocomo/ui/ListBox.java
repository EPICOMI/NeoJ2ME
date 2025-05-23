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

public final class ListBox extends Component implements Interactable 
{
    public static final int SINGLE_SELECT = 0;
    public static final int RADIO_BUTTON = 1;
    public static final int CHECK_BOX = 2;
    public static final int NUMBERED_LIST = 3;
    public static final int MULTIPLE_SELECT = 4;
    public static final int CHOICE = 5;

    private String[] items;
    private boolean[] selected;
    private int type;
    private int itemCount;

    public ListBox(int type) { this(type, 1); }

    public ListBox(int type, int rows) 
    {
        if (type < 0 || type > 5) {  throw new IllegalArgumentException("Illegal list type"); }
        this.type = type;
        this.items = new String[0];
        this.selected = new boolean[0];
        this.itemCount = 0;
    }

    public void setEnabled(boolean b) { }

    public void requestFocus() { }

    public void setItems(String[] items) 
    {
        if (items == null) { throw new NullPointerException("Items cannot be null"); }
        this.items = items;
        this.itemCount = items.length;
        this.selected = new boolean[itemCount];
    }

    public void append(String item) 
    {
        if (item == null) { throw new NullPointerException("Item cannot be null"); }
        String[] newItems = new String[itemCount + 1];
        System.arraycopy(items, 0, newItems, 0, itemCount);
        newItems[itemCount] = item;
        items = newItems;
        selected = new boolean[items.length];
        itemCount++;
    }

    public void removeAll() 
    {
        items = new String[0];
        selected = new boolean[0];
        itemCount = 0;
    }

    public int getItemCount() { return itemCount; }

    public String getItem(int index) 
    {
        if (index < 0 || index >= itemCount) { throw new ArrayIndexOutOfBoundsException("Invalid index"); }
        return items[index];
    }

    public void select(int index) 
    {
        if (index < 0 || index >= itemCount) {
            throw new ArrayIndexOutOfBoundsException("Invalid index");
        }
        selected[index] = true;
    }

    public void deselect(int index) {
        if (type == CHOICE) { throw new UIException(1, "Cannot deselect in CHOICE type"); }
        if (index < 0 || index >= itemCount) { throw new ArrayIndexOutOfBoundsException("Invalid index"); }
        selected[index] = false;
    }

    public boolean isIndexSelected(int index) 
    {
        if (index < 0 || index >= itemCount) { throw new ArrayIndexOutOfBoundsException("Invalid index"); }
        return selected[index];
    }

    public int getSelectedIndex() 
    {
        for (int i = 0; i < itemCount; i++) 
        {
            if (selected[i]) { return i; }
        }
        return -1;
    }
}