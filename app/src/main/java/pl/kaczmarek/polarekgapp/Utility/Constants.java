package pl.kaczmarek.polarekgapp.Utility;

import android.Manifest;

public class Constants {
    public static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    public static final String[] BLUETOOTH_PERMISSIONS = new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT};
    public static final String[] FINE_LOCATION_PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] COARSE_LOCATION_PERMISSIONS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String DEVICE_ID = "deviceID";
    public static final String BATTERY_VALUE = "batteryVal";
    public static final String MEASUREMENT_TYPE = "measurementType";
    public static final String DATA_TO_SAVE = "dataToSave";
    public static final String LOADED_DATA = "loadedData";
    public static final String FILE_NAME = "fileName";
    public static final String ECG_EXTENSION = "ecg";
    public static final String PPG_EXTENSION = "ppg";
    public static final int MAX_VISIBLE_ENTRIES = 200;
    public static final String NO_BLUETOOTH_ERROR_MESSAGE = "For proper use of application, please allow Bluetooth access.";
    public static final String UNDEFINED_STREAMING_ERROR_MESSAGE = "Undefined streaming error. Please try again.";
    public static final String PPG_COMPLETE_MESSAGE = "PPG Stream Complete";
    public static final String ECG_COMPLETE_MESSAGE = "ECG Stream Complete";
    public static final String DEVICE_CONNECTION_ERROR_MESSAGE = "Couldn't connect to device %s!";
    public static final String DEVICE_CONNECTION_LOST_MESSAGE = "Lost connection with device %s!";
    public static final String SAVING_SUCCESS_MESSAGE = "Successfully saved data to %s!";
    public static final String SAVING_FAILED_MESSAGE = "Failed to save data!";
    public static final String SAVING_NO_FILES_MESSAGE = "No saved files found!";
    public static final String DELETING_SUCCESS_MESSAGE = "Deleted file %s!";
    public static final String DELETING_FAILED_MESSAGE = "Unable to delete file %s!";
    public static final String LOADING_FAILED_MESSAGE = "Unable to load file %s!";
    public static final String LOADING_INCORRECT_EXTENSION_MESSAGE = "Incorrect file extension!";
}
