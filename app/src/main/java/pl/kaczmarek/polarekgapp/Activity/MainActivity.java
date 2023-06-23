package pl.kaczmarek.polarekgapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import pl.kaczmarek.polarekgapp.Controller.MainActivityController;
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class MainActivity extends AppCompatActivity {

    private MainActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForPermissions();
        PolarBleApi api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.cleanup();

        LinearLayout devicesList = findViewById(R.id.devicesList);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Button scanForNearbyButton = findViewById(R.id.scanForNearbyButton);
        Button savedMeasurements = findViewById(R.id.savedMeasurements);
        EditText deviceIdInput = (EditText) findViewById(R.id.deviceIdInput);
        Button connectByDeviceIDButton = findViewById(R.id.connectByDeviceIDButton);

        controller = new MainActivityController(this, api, devicesList, layoutInflater);

        scanForNearbyButton.setOnClickListener(view -> controller.scanForNearbyDevices());
        savedMeasurements.setOnClickListener(view -> controller.openSavedMeasurementsScreen());
        connectByDeviceIDButton.setOnClickListener(view -> controller.connectToDeviceByUserProvidedID(deviceIdInput.getText().toString()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.BLUETOOTH_PERMISSION_REQUEST_CODE){
            for(int grantResult : grantResults){
                if(grantResult == PackageManager.PERMISSION_DENIED) {
                    ToastShower.show(this, Constants.NO_BLUETOOTH_ERROR_MESSAGE);
                }
            }
        }
    }

    private void checkForPermissions(){
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