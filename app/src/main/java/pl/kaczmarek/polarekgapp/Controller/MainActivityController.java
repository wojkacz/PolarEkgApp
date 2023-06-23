package pl.kaczmarek.polarekgapp.Controller;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.model.PolarDeviceInfo;
import io.reactivex.rxjava3.disposables.Disposable;
import pl.kaczmarek.polarekgapp.Activity.ConnectedDeviceActivity;
import pl.kaczmarek.polarekgapp.Activity.MainActivity;
import pl.kaczmarek.polarekgapp.Activity.SavedFilesActivity;
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;

public class MainActivityController {

    private MainActivity activity;
    private PolarBleApi api;
    private LinearLayout devicesList;
    private LayoutInflater layoutInflater;
    private Disposable deviceSearch;

    public MainActivityController(MainActivity activity, PolarBleApi api, LinearLayout devicesList, LayoutInflater layoutInflater) {
        this.activity = activity;
        this.api = api;
        this.devicesList = devicesList;
        this.layoutInflater = layoutInflater;
    }

    public void scanForNearbyDevices() {
        clearScreen();
        deviceSearch = api.searchForDevice().subscribe(this::onDeviceFound);
    }

    public void openSavedMeasurementsScreen() {
        clearScreen();
        Intent savedMeasurementsScreen = new Intent(devicesList.getContext(), SavedFilesActivity.class);
        devicesList.getContext().startActivity(savedMeasurementsScreen);
    }

    public void connectToDeviceByUserProvidedID(String deviceID) {
        if(deviceID == null || deviceID.isEmpty()) {
            return;
        }
        connectToDevice(deviceID);
    }

    public void connectToDevice(String deviceID) {
        clearScreen();
        Intent connectedDeviceScreen = new Intent(devicesList.getContext(), ConnectedDeviceActivity.class);
        connectedDeviceScreen.putExtra(Constants.DEVICE_ID, deviceID);
        activity.startActivity(connectedDeviceScreen);
    }

    public void onDeviceFound(PolarDeviceInfo device) {
        View deviceFoundComponent = layoutInflater.inflate(R.layout.found_device, devicesList, false);

        TextView deviceIDText = deviceFoundComponent.findViewById(R.id.deviceIDText);
        deviceIDText.setText(device.getDeviceId());

        Button connectToDeviceButton = deviceFoundComponent.findViewById(R.id.connectButton);
        connectToDeviceButton.setOnClickListener(view2 -> connectToDevice(device.getDeviceId()));

        devicesList.addView(deviceFoundComponent);
    }

    private void clearScreen() {
        devicesList.removeAllViewsInLayout();
        if (deviceSearch != null) {
            deviceSearch.dispose();
        }
    }
}