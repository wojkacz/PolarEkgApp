package pl.kaczmarek.polarekgapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.model.PolarHrData;
import com.polar.sdk.api.model.PolarOhrData;
import com.polar.sdk.api.model.PolarSensorSetting;
import org.reactivestreams.Publisher;
import java.util.Locale;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import pl.kaczmarek.polarekgapp.Controller.EcgActivityController;
import pl.kaczmarek.polarekgapp.Controller.PpgActivityController;
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.ChartSetter;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.DataFormatter;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class PpgActivity extends AppCompatActivity {

    private String deviceId;
    private Integer batteryLevel;

    private PolarBleApi api;
    private PpgActivityController controller;

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
        setContentView(R.layout.ppg_activity);
        deviceId = getIntent().getStringExtra(Constants.DEVICE_ID);
        batteryLevel = getIntent().getIntExtra(Constants.BATTERY_VALUE, -1);

        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);

        lineChart = findViewById(R.id.ppgLineChart);
        bpmText = findViewById(R.id.ppgBpmText);
        startMeasuringButton = findViewById(R.id.ppgMeasureButton);
        clearButton = findViewById(R.id.ppgClearButton);
        saveButton = findViewById(R.id.ppgSaveMeasurementButton);
        disconnectButton = findViewById(R.id.ppgDisconnectButton);
        batteryValue = findViewById(R.id.ppgBatteryValue);
        deviceIdText = findViewById(R.id.ppgDeviceID);
        waitingText = findViewById(R.id.ppgWaitingText);

        disableSaveAndClearButtons();
        ChartSetter.setPpgChart(lineChart);
        controller = new PpgActivityController(this, api, lineChart, deviceId);

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

    public void changeStartMeasuringButtonText() {
        if(startMeasuringButton.getText().equals(getString(R.string.start_measurement))) {
            startMeasuringButton.setText(getString(R.string.pause_measurement));
            return;
        }
        startMeasuringButton.setText(getString(R.string.start_measurement));
    }

    public void disableDisconnectButton() {
        disconnectButton.setEnabled(false);
    }

    public void enableDisconnectButton() {
        disconnectButton.setEnabled(true);
    }

    public void setWaitingTextVisibility() {
        if(waitingText.getVisibility() == View.GONE) {
            waitingText.setText(View.VISIBLE);
            return;
        }
        waitingText.setVisibility(View.GONE);
    }
}
