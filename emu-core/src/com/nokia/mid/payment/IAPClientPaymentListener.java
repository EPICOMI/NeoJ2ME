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
package com.nokia.mid.payment;

public interface IAPClientPaymentListener 
{

	public static final int OK = 1;
	public static final int BAD_REQUEST = -1;
	public static final int AUTH_FAILED = -2;
	public static final int FORBIDDEN = -3;
	public static final int NOT_FOUND = -4;
	public static final int SERVER_ERROR = -5;
	public static final int SERVICE_UNAVAILABLE = -6;
	public static final int UNKNOWN_SERVICE = -7;
	public static final int INVALID_PRODUCT_ID = -8;
	public static final int PRODUCT_INFO_FAILED = -9;
	public static final int INVALID_PRICE = -10;
	public static final int CUST_INFO_FAILED = -11;
	public static final int PMT_INSTR_FAILED = -12;
	public static final int NO_PMT_METHODS = -13;
	public static final int PURCHASE_SESSION_FAILED = -14;
	public static final int UNKNOWN_PRODUCT = -15;
	public static final int INVALID_PRODUCT_DATA = -16;
	public static final int DELIVERY_LIMIT_EXCEEDED = -17;
	public static final int RESTORABLE = -18;
	public static final int RESTORATION_NOT_ALLOWED = -19;
	public static final int RESTORATION_LMT_EXCEEDED = -20;
	public static final int RESTORATION_DEVICE_LMT_EXCEEDED = -21;
	public static final int GENERAL_PRODUCT_ERROR = -22;
	public static final int DRM_SERVER_ERROR = -23;
	public static final int LICENSE_ACTIVATION_ERROR = -24;
	public static final int SILENT_OPER_FAILED = -25;
	public static final int OVI_SIGN_IN_FAILED = -26;
	public static final int SMS_PMT_FAILED = -27;
	public static final int OPERATOR_BILLING_FAILED = -28;
	public static final int PMT_INSTR_UNAUTHORIZED = -29;
	public static final int UNKNOWN_TRANSACTION_ID = -30;
	public static final int TIMEOUT = -31;
	public static final int TIMEOUT_DELIVERED = -32;
	public static final int GENERAL_PURCHASE_ERROR = -33;
	public static final int OPERATION_FAILED = -34;
	public static final int GENERAL_HTTP_ERROR = -35;
	public static final int USER_AND_DEVICE_INFO_FAILED = -36;
	public static final int RESTORATION_FAILED = -37;

	void productDataReceived(int status, IAPClientProductData pd);

	void productDataListReceived(int status, IAPClientProductData[] productDataList);

	void purchaseCompleted(int status, String purchaseTicket);

	void restorationCompleted(int status, String purchaseTicket);

	void restorableProductsReceived(int status, IAPClientProductData[] productDataList);

	void userAndDeviceDataReceived(int status, IAPClientUserAndDeviceData ud);
}