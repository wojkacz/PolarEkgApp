package pl.kaczmarek.polarekgapp;

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
    public static final int PEAK_TRESHOLD = 500;
}
