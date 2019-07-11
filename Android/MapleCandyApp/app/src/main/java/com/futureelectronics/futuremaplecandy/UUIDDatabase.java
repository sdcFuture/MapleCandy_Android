package com.futureelectronics.futuremaplecandy;

/**
 * Created by Kyle.Harman on 1/15/2015.
 */
import java.util.UUID;

/**
 * This class will store the UUID of the GATT services and characteristics
 */
public class UUIDDatabase {
	/**
	 * Generic UUIDs
	 */
	public final static UUID UUID_GENERIC_ACCESS_SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_GENERIC_ATTRIBUTE_SERVICE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");

	/**
	 * Descriptors
	 */
	public final static UUID UUID_USER_DESCRIPTION_DESC = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CHARACTERISTIC_EXTENDED_PROPERTIES = UUID.fromString("00002900-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_SERVER_CHARACTERISTIC_CONFIG = UUID.fromString("00002903-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CHARACTERISTIC_PRESENTATION_FORMAT = UUID.fromString("00002904-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CHARACTERISTIC_AGGREGATE_FORMAT = UUID.fromString("00002905-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CHARACTERISTIC_VALID_RANGE = UUID.fromString("00002906-0000-1000-8000-00805f9b34fb");

	/**
	 * Heart rate related UUID
	 */
	public final static UUID UUID_HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BODY_SENSOR_LOCATION = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HEART_RATE_CONTROL_POINT = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");

	/**
	 * Device information related UUID
	 */
	public final static UUID UUID_DEVICE_INFORMATION_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_DEVICE_NAME_STRING = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_APPEARANCE_STRING = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_PERIPHERAL_PRIVACY_FLAG = UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_RECONNECTION_ADDRESS = UUID.fromString("00002a03-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_PREFERRED_PARAMETERS = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_SYSTEM_ID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_MANUFATURE_NAME_STRING = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_MODEL_NUMBER_STRING = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_SERIAL_NUMBER_STRING = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_HARDWARE_REVISION_STRING = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_SOFTWARE_REVISION_STRING = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_FIRMWARE_REVISION_STRING = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_PNP_ID = UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_IEEE = UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_SERVICE_CHANGED = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb");

	/**
	 * Health thermometer related UUID
	 */
	public final static UUID UUID_HEALTH_THERMO_SERVICE = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HEALTH_THERMOMETER = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb");

	/**
	 * Battery Level related uuid
	 */
	public final static UUID UUID_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

	/**
	 * Find me related uuid
	 */
	public static final UUID UUID_LINK_LOSS_SERVICE = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_TRANSMISSION_POWER_SERVICE = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_ALERT_LEVEL = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_TRANSMISSION_POWER_LEVEL = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");

	/**
	 * Time UUIDs
	 */
	public final static UUID UUID_CURRENT_TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_REFERENCE_TIME_UPDATE_SERVICE = UUID.fromString("00001806-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_NEXT_DST_CHANGE_SERVICE = UUID.fromString("00001807-0000-1000-8000-00805f9b34fb");

	/**
	 * Location and Navigation
	 */
	public final static UUID UUID_LOCATION_AND_NAVIGATION_SERVICE = UUID.fromString("00001819-0000-1000-8000-00805f9b34fb");

	/**
	 * LED Strip related uuid
	 */
	public final static UUID UUID_LED_STRIP_SERVICE = UUID.fromString("0000cccc-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_LED_STRIP_MODE = UUID.fromString("0000ccc2-0000-1000-8000-00805f9b34fb");

	/**
	 * GlucoseService related uuid
	 */
	public final static UUID UUID_GLUCOSE_SERVICE = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_GLUCOSE = UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_GLUCOSE_MESUREMENT_CONTEXT = UUID.fromString("00002a34-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_GLUCOSE_FEATURE = UUID.fromString("00002a51-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_RECORD_ACCESS_CONTROL_POINT = UUID.fromString("00002a52-0000-1000-8000-00805f9b34fb");

	/**
	 * Continuous Glucose Monitoring (CGM) related uuid
	 */
	public final static UUID UUID_CGM_SERVICE = UUID.fromString("0000181f-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CGM_MEASUREMENT = UUID.fromString("00002aa7-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CGM_FEATURE = UUID.fromString("00002aa8-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CGM_STATUS = UUID.fromString("00002aa9-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CGM_SESSION_START_TIME = UUID.fromString("00002aaa-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CGM_SESSION_RUN_TIME = UUID.fromString("00002aab-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CGM_SPEC_OPS_CTRL_POINT = UUID.fromString("00002aac-0000-1000-8000-00805f9b34fb");

	/**
	 * Blood pressure related uuid
	 */
	public final static UUID UUID_BLOOD_PRESSURE_SERVICE = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BLOOD_PRESSURE_MEASUREMENT = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BLOOD_INTERMEDIATE_CUFF_PRESSURE = UUID.fromString("00002a36-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BLOOD_PRESSURE_FEATURE = UUID.fromString("00002a49-0000-1000-8000-00805f9b34fb");

	/**
	 * HID related uuid
	 */
	public final static UUID UUID_HID_SERVICE = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HID_PROTOCOL_MODE = UUID.fromString("00002a4e-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HID_REPORT = UUID.fromString("00002a4d-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HID_REPORT_MAP = UUID.fromString("00002a4b-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BOOT_KEYBD_INPUT_REPORT = UUID.fromString("00002a22-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BOOT_KEYBD_OUTPUT_REPORT = UUID.fromString("00002a32-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_BOOT_MOUSE_INPUT_REPORT = UUID.fromString("00002a33-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HID_INFORMATION = UUID.fromString("00002a4a-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_HID_CTRL_POINT = UUID.fromString("00002a4c-0000-1000-8000-00805f9b34fb");

	/**
	 * Running Speed & Cadence related uuid
	 */
	public final static UUID UUID_RSC_SERVICE = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_RSC_MEASURE = UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_RSC_FEATURE = UUID.fromString("00002a54-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_SC_SENSOR_LOCATION = UUID.fromString("00002a5d-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_SC_CONTROL_POINT = UUID.fromString("00002a55-0000-1000-8000-00805f9b34fb");

	/**
	 * Internet Protocol Support related uuid
	 */
	public final static UUID UUID_INTERNET_PROTOCOL_SUPPORT_SERVICE = UUID.fromString("00001820-0000-1000-8000-00805f9b34fb");

	/**
	 * Cycling Speed & Cadence related uuid
	 */
	public final static UUID UUID_CSC_SERVICE = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CSC_MEASURE = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CSC_FEATURE = UUID.fromString("00002a5c-0000-1000-8000-00805f9b34fb");

	/**
	 * Barometer related uuid
	 */
	public final static UUID UUID_BAROMETER_SERVICE = UUID.fromString("00040001-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_BAROMETER_DIGITAL_SENSOR = UUID.fromString("00040002-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_BAROMETER_SENSOR_SCAN_INTERVAL = UUID.fromString("00040004-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_BAROMETER_DATA_ACCUMULATION = UUID.fromString("00040007-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_BAROMETER_READING = UUID.fromString("00040009-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_BAROMETER_THRESHOLD_FOR_INDICATION = UUID.fromString("0004000d-0000-1000-8000-00805f9b0131");

	/**
	 * Accelerometer related uuid
	 */
	public final static UUID UUID_ACCELEROMETER_SERVICE = UUID.fromString("00040020-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_ACCELEROMETER_ANALOG_SENSOR = UUID.fromString("00040021-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_ACCELEROMETER_SENSOR_SCAN_INTERVAL = UUID.fromString("00040023-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_ACCELEROMETER_DATA_ACCUMULATION = UUID.fromString("00040026-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_ACCELEROMETER_READING_X = UUID.fromString("00040028-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_ACCELEROMETER_READING_Y = UUID.fromString("0004002b-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_ACCELEROMETER_READING_Z = UUID.fromString("0004002d-0000-1000-8000-00805f9b0131");

	/**
	 * Analog temperature  related uuid
	 */
	public final static UUID UUID_ANALOG_TEMPERATURE_SERVICE = UUID.fromString("00040030-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_TEMPERATURE_ANALOG_SENSOR = UUID.fromString("00040031-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_TEMPERATURE_READING = UUID.fromString("00040033-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_TEMPERATURE_SENSOR_SCAN_INTERVAL = UUID.fromString("00040032-0000-1000-8000-00805f9b0131");
	public final static UUID UUID_TEMPERATURE = UUID.fromString("00002a6e-0000-1000-8000-00805f9b34fb");

	/**
	 * Serial Port related UUID
	 */
	public final static UUID UUID_SERIAL_PORT_SERVICE_DIALOG = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
	public final static UUID UUID_SERIAL_PORT_SERVICE_CSR = UUID.fromString("00005500-D102-11E1-9B23-00025B00A5A5");
	public final static UUID UUID_SERIAL_PORT_SERVICE_LAIRD = UUID.fromString("569a1101-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID UUID_SERIAL_PORT_SERVICE_TOSHIBA = UUID.fromString("e079c6a0-aa8b-11e3-a903-0002a5d5c51b");
	public final static UUID UUID_SERIAL_PORT_TX_DIALOG = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
	public final static UUID UUID_SERIAL_PORT_RX_DIALOG = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
	public final static UUID UUID_SERIAL_PORT_FLOWCONTROL_DIALOG = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb9");
	public final static UUID UUID_SERIAL_PORT_RX_TX_CSR = UUID.fromString("00005501-d102-11e1-9b23-00025b00a5a5");
	public final static UUID UUID_SERIAL_PORT_TX_LAIRD = UUID.fromString("569a2000-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID UUID_SERIAL_PORT_RX_LAIRD = UUID.fromString("569a2001-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID UUID_SERIAL_PORT_RX_TX_TOSHIBA = UUID.fromString("b38312c0-aa89-11e3-9cef-0002a5d5c51b");

	/**
	 * Future Electronics Maple Candy VUART UUIDs
	 */
	public final static UUID UUID_MAPLECANDY_CUSTOM_SERVICE = UUID.fromString("D68C0001-A21B-11E5-8CB8-0002A5D5C51B");
	public final static UUID UUID_MAPLECANDY_VUART_CHAR = UUID.fromString("D68C0002-A21B-11E5-8CB8-0002A5D5C51B");
	public final static UUID UUID_MAPLECANDY_WRITE_CHAR = UUID.fromString("D68C0003-A21B-11E5-8CB8-0002A5D5C51B");
}

