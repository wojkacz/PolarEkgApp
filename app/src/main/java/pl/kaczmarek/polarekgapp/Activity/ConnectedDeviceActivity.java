package pl.kaczmarek.polarekgapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.PolarDeviceInfo;
import java.util.Locale;
import java.util.Set;

import pl.kaczmarek.polarekgapp.Controller.ConnectedDeviceActivityController;
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;


public class ConnectedDeviceActivity extends AppCompatActivity {

    private String deviceId;
    private int batteryLevel;

    private PolarBleApi api;
    private ConnectedDeviceActivityController controller;

    private Button connectToEcgButton;
    private Button connectToPpgButton;
    private Button disconnectButton;
    private TextView batteryValue;
    private TextView deviceIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_device);

        deviceId = getIntent().getStringExtra(Constants.DEVICE_ID);
        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        controller = new ConnectedDeviceActivityController(this, api, deviceId);

        connectToEcgButton = findViewById(R.id.conConnectToEcgButton);
        connectToPpgButton = findViewById(R.id.conConnectToPpgButton);
        disconnectButton = findViewById(R.id.conDisconnectButton);
        deviceIdText = findViewById(R.id.conDeviceID);
        batteryValue = findViewById(R.id.conBatteryValue);

        connectToEcgButton.setEnabled(false);
        connectToPpgButton.setEnabled(false);

        connectToEcgButton.setOnClickListener(view -> controller.onConnectToEcgButtonClick());
        connectToPpgButton.setOnClickListener(view -> controller.onConnectToPpgButtonClick());
        disconnectButton.setOnClickListener(view -> finish());

        api.setApiCallback(getApiCallback(this));

        try {
            api.connectToDevice(deviceId);
        } catch (PolarInvalidArgument e) {
            e.printStackTrace();
            ToastShower.show(this, String.format("Couldn't connect to device %s!", deviceId));
            finish();
        }
    }

    private PolarBleApiCallback getApiCallback(Context context) {
        return new PolarBleApiCallback() {
            @Override
            public void batteryLevelReceived(@NonNull String identifier, int level) {
                super.batteryLevelReceived(identifier, level);
                batteryLevel = level;
                batteryValue.setText(String.format(Locale.ENGLISH, "%d%%", level));
            }

            @Override
            public void deviceConnected(@NonNull PolarDeviceInfo polarDeviceInfo) {
                super.deviceConnected(polarDeviceInfo);
                deviceIdText.setText(deviceId);
            }

            @Override
            public void deviceDisconnected(@NonNull final PolarDeviceInfo polarDeviceInfo) {
                super.deviceDisconnected(polarDeviceInfo);
                ToastShower.show(context, String.format("Lost connection with device %s!", deviceId));
                finish();
            }

            @Override
            public void blePowerStateChanged(boolean powered) {
                super.blePowerStateChanged(powered);
                if (!powered) {
                    ToastShower.show(context, String.format("Lost connection with device %s!", deviceId));
                    finish();
                }
            }

            @Override
            public void streamingFeaturesReady(@NonNull String identifier, @NonNull Set<? extends PolarBleApi.DeviceStreamingFeature> features) {
                super.streamingFeaturesReady(identifier, features);
                controller.onStreamingFeaturesReady(features);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            api.disconnectFromDevice(deviceId);
        } catch (PolarInvalidArgument e) {
            e.printStackTrace();
        }
    }

    public void enableEcgButton() {
        connectToEcgButton.setEnabled(true);
    }

    public void enablePpgButton() {
        connectToPpgButton.setEnabled(true);
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }
}
