package pl.kaczmarek.polarekgapp.Controller;

import android.content.Intent;
import com.polar.sdk.api.PolarBleApi;
import java.util.Set;
import pl.kaczmarek.polarekgapp.Activity.ConnectedDeviceActivity;
import pl.kaczmarek.polarekgapp.Activity.EcgActivity;
import pl.kaczmarek.polarekgapp.Activity.PpgActivity;
import pl.kaczmarek.polarekgapp.Utility.Constants;

public class ConnectedDeviceActivityController {

    private ConnectedDeviceActivity activity;
    private PolarBleApi api;
    private String deviceId;

    public ConnectedDeviceActivityController(ConnectedDeviceActivity activity, PolarBleApi api, String deviceId) {
        this.activity = activity;
        this.api = api;
        this.deviceId = deviceId;
    }

    public void onConnectToEcgButtonClick() {
        Intent ecgActivityScreen = new Intent(activity, EcgActivity.class);
        ecgActivityScreen.putExtra(Constants.DEVICE_ID, deviceId);
        ecgActivityScreen.putExtra(Constants.BATTERY_VALUE, activity.getBatteryLevel());
        activity.startActivity(ecgActivityScreen);
    }

    public void onConnectToPpgButtonClick() {
        Intent ppgActivityScreen = new Intent(activity, PpgActivity.class);
        ppgActivityScreen.putExtra(Constants.DEVICE_ID, deviceId);
        ppgActivityScreen.putExtra(Constants.BATTERY_VALUE, activity.getBatteryLevel());
        activity.startActivity(ppgActivityScreen);
    }

    public void onStreamingFeaturesReady(Set<? extends PolarBleApi.DeviceStreamingFeature> features) {
        for (PolarBleApi.DeviceStreamingFeature feature : features) {
            if (feature == PolarBleApi.DeviceStreamingFeature.ECG) {
                activity.enableEcgButton();
            } else if (feature == PolarBleApi.DeviceStreamingFeature.PPG) {
                activity.enablePpgButton();
            }
        }
    }
}