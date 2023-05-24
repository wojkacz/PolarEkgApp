package pl.kaczmarek.polarekgapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.model.PolarDeviceInfo;

import java.util.function.Consumer;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    LayoutInflater layoutInflater;
    LinearLayout devicesList;
    Disposable deviceSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForPermissions();
        PolarBleApi api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.cleanup();

        devicesList = findViewById(R.id.devicesList);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Button scanForNearbyButton = findViewById(R.id.scanForNearbyButton);
        scanForNearbyButton.setOnClickListener(view -> {
                clearScreen();
                deviceSearch = api.searchForDevice().subscribe(this::onDeviceFound);
        });

        Button savedMeasurements = findViewById(R.id.savedMeasurements);
        savedMeasurements.setOnClickListener(view -> {
            clearScreen();
            Intent savedMeasurementsScreen = new Intent(this, SavedFilesActivity.class);
            startActivity(savedMeasurementsScreen);
        });

        EditText deviceIdInput = (EditText) findViewById(R.id.deviceIdInput);
        Button connectByDeviceIDButton = findViewById(R.id.connectByDeviceIDButton);
        connectByDeviceIDButton.setOnClickListener(view -> connectToDeviceByUserProvidedID(deviceIdInput.getText().toString()));
    }

    private void clearScreen() {
        devicesList.removeAllViewsInLayout();
        if(deviceSearch != null) {
            deviceSearch.dispose();
        }
    }

    public void connectToDeviceByUserProvidedID(String deviceID){
        connectToDevice(deviceID);
    }

    public void connectToDevice(String deviceID){
        clearScreen();
        Intent connectedDeviceScreen = new Intent(this, ConnectedDeviceActivity.class);
        connectedDeviceScreen.putExtra(Constants.DEVICE_ID, deviceID);
        startActivity(connectedDeviceScreen);
    }

    public void onDeviceFound(PolarDeviceInfo device) {
        View deviceFoundComponent = layoutInflater.inflate(R.layout.found_device, devicesList, false);

        Button connectToDeviceButton = deviceFoundComponent.findViewById(R.id.connectButton);
        connectToDeviceButton.setOnClickListener(view2 -> connectToDevice(device.getDeviceId()));

        TextView deviceIDText = deviceFoundComponent.findViewById(R.id.deviceIDText);
        deviceIDText.setText(device.getDeviceId());

        devicesList.addView(deviceFoundComponent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.BLUETOOTH_PERMISSION_REQUEST_CODE){
            for(int grantResult : grantResults){
                if(grantResult == PackageManager.PERMISSION_DENIED) {
                    return;
                }
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show();
    }

    public void checkForPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(Constants.BLUETOOTH_PERMISSIONS, Constants.BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
                requestPermissions(Constants.FINE_LOCATION_PERMISSIONS, Constants.BLUETOOTH_PERMISSION_REQUEST_CODE);
            }
        } else {
            requestPermissions(Constants.COARSE_LOCATION_PERMISSIONS, Constants.BLUETOOTH_PERMISSION_REQUEST_CODE);
        }
    }

}