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
package com.nokia.payment;

import org.recompile.mobile.Mobile;
import javax.microedition.midlet.MIDlet;

public class NPayManager 
{

    public NPayManager(MIDlet midlet) { Mobile.log(Mobile.LOG_WARNING, NPayManager.class.getPackage().getName() + "." + NPayManager.class.getSimpleName() + ": " + "NPayManager init requested."); }

    public void getProductData(String[] productIdList)  { }

    public boolean isNPayAvailable() { return true; }

    public void launchNPaySetup() { }

    public void purchaseProduct(String productId) { }

    public void purchaseProduct(String contentId, String productId) { }

    public void setNPayListener(NPayListener listener) { }
}