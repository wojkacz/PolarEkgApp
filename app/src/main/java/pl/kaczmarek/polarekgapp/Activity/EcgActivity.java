package pl.kaczmarek.polarekgapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.model.PolarHrData;
import java.util.Locale;
import com.polar.sdk.api.PolarBleApi;
import pl.kaczmarek.polarekgapp.Controller.EcgActivityController;
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.ChartSetter;
import pl.kaczmarek.polarekgapp.Utility.Constants;


public class EcgActivity extends AppCompatActivity {

    private String deviceId;
    private Integer batteryLevel;

    private PolarBleApi api;
    private EcgActivityController controller;

    private Button startMeasuringButton;
    private Button saveButton;
    private Button disconnectButton;
    private Button clearButton;
    private TextView batteryValue;
    private TextView deviceIdText;
    private TextView bpmText;
    private LineChart lineChart;
    private TextView waitingText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_activity);
        deviceId = getIntent().getStringExtra(Constants.DEVICE_ID);
        batteryLevel = getIntent().getIntExtra(Constants.BATTERY_VALUE, -1);

        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);

        lineChart = findViewById(R.id.ecgLineChart);
        startMeasuringButton = findViewById(R.id.ecgMeasureButton);
        clearButton = findViewById(R.id.ecgClearButton);
        saveButton = findViewById(R.id.ecgSaveMeasurementButton);
        disconnectButton = findViewById(R.id.ecgDisconnectButton);
        batteryValue = findViewById(R.id.ecgBatteryValue);
        deviceIdText = findViewById(R.id.ecgDeviceID);
        bpmText = findViewById(R.id.bpmText);
        waitingText = findViewById(R.id.waitingText);

        disableSaveAndClearButtons();
        ChartSetter.setEcgChart(lineChart);
        controller = new EcgActivityController(this, api, lineChart, deviceId);

        deviceIdText.setText(deviceId);
        batteryValue.setText(batteryLevel != -1 ? String.format(Locale.ENGLISH, "%d%%", batteryLevel) : "Not found");

        clearButton.setOnClickListener(onClick -> controller.onClearButtonClick());
        saveButton.setOnClickListener(onClick -> controller.onSaveButtonClick());
        disconnectButton.setOnClickListener(onClick -> controller.onDisconnectButtonClick());
        startMeasuringButton.setOnClickListener(onClick -> controller.onStartMeasurementButtonClick());

        api.setApiCallback(new PolarBleApiCallback(){
            @Override
            public void hrNotificationReceived(@NonNull String identifier, @NonNull PolarHrData data) {
                super.hrNotificationReceived(identifier, data);
                if(!controller.isPaused()) {
                    bpmText.setText(String.format(Locale.ENGLISH, "%d BPM", data.getHr()));
                }
            }
        });
    }

    public void enableSaveAndClearButtonsIfPossible() {
        if(lineChart.getData().getEntryCount() > 0) {
            saveButton.setEnabled(true);
            clearButton.setEnabled(true);
        }
    }

    public void disableSaveAndClearButtons() {
        saveButton.setEnabled(false);
        clearButton.setEnabled(false);
    }

    public void disableDisconnectButton() {
        disconnectButton.setEnabled(false);
    }

    public void enableDisconnectButton() {
        disconnectButton.setEnabled(true);
    }

    public void setWaitingTextVisibility() {
        if(waitingText.getVisibility() == View.GONE) {
            waitingText.setVisibility(View.VISIBLE);
            return;
        }
        waitingText.setVisibility(View.GONE);
    }

    public void changeStartMeasuringButtonText() {
        if(startMeasuringButton.getText().equals(getString(R.string.start_measurement))) {
            startMeasuringButton.setText(getString(R.string.pause_measurement));
            return;
        }
        startMeasuringButton.setText(getString(R.string.start_measurement));
    }
}