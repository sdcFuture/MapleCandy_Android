package com.futureelectronics.futuremaplecandy;

/**
 * Created by Kyle.Harman on 2/15/2016.
 */
public class GattError {
	public static final int SUCCESS = 0;

	public static final int CONN_TIMEOUT = -1;
	public static final int DISCOVER_SERVICES_TIMEOUT = -2;
	public static final int DISCOVER_SERVICES_FAILED = -3;
	public static final int MTU_CHANGE_FAILED = -4;
	public static final int GATT_READ_NOT_PERMITTED = -5;
	public static final int GATT_READ_FAILED = -6;
	public static final int GATT_WRITE_NOT_PERMITTED = -7;
	public static final int GATT_WRITE_FAILED = -8;
	public static final int READ_RSSI_FAILED = -9;

}
