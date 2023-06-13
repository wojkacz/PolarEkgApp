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

import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;


public class ConnectedDeviceActivity extends AppCompatActivity {

    private String deviceId;
    private int batteryLevel;

    PolarBleApi api;

    // Objects
    Button connectToEcgButton;
    Button connectToPpgButton;
    Button disconnectButton;
    TextView batteryValue;
    TextView deviceIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_device);

        deviceId = getIntent().getStringExtra(Constants.DEVICE_ID);
        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);

        setObjects();
        api.setApiCallback(getApiCallback(this));

        try {
            api.connectToDevice(deviceId);
        } catch (PolarInvalidArgument e) {
            e.printStackTrace();
            ToastShower.show(this, String.format("Couldn't connect to device %s!", deviceId));
            finish();
        }
    }

    private void setObjects() {
        connectToEcgButton = findViewById(R.id.conConnectToEcgButton);
        connectToEcgButton.setEnabled(false);
        connectToEcgButton.setOnClickListener(onClick -> {
            Intent ecgActivityScreen = new Intent(this, EcgActivity.class);
            ecgActivityScreen.putExtra(Constants.DEVICE_ID, deviceId);
            ecgActivityScreen.putExtra(Constants.BATTERY_VALUE, batteryLevel);
            startActivity(ecgActivityScreen);
        });

        connectToPpgButton = findViewById(R.id.conConnectToPpgButton);
        connectToPpgButton.setEnabled(false);
        connectToPpgButton.setOnClickListener(onClick -> {
            Intent ppgActivityScreen = new Intent(this, PpgActivity.class);
            ppgActivityScreen.putExtra(Constants.DEVICE_ID, deviceId);
            startActivity(ppgActivityScreen);
        });

        disconnectButton = findViewById(R.id.conDisconnectButton);
        disconnectButton.setOnClickListener(onClick -> finish());

        deviceIdText = findViewById(R.id.conDeviceID);
        batteryValue = findViewById(R.id.conBatteryValue);
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
                if(!powered) {
                    ToastShower.show(context, String.format("Lost connection with device %s!", deviceId));
                    finish();
                }
            }

            @Override
            public void streamingFeaturesReady(@NonNull String identifier, @NonNull Set<? extends PolarBleApi.DeviceStreamingFeature> features) {
                super.streamingFeaturesReady(identifier, features);
                for(PolarBleApi.DeviceStreamingFeature feature : features) {
                    if(feature == PolarBleApi.DeviceStreamingFeature.ECG) {
                        connectToEcgButton.setEnabled(true);
                    }
                    else if(feature == PolarBleApi.DeviceStreamingFeature.PPG) {
                        connectToPpgButton.setEnabled(true);
                    }
                }
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
    
    
}
