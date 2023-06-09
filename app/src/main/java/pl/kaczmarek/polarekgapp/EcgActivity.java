package pl.kaczmarek.polarekgapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.model.PolarEcgData;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Consumer;

import com.polar.sdk.api.model.PolarHrData;
import com.polar.sdk.api.model.PolarSensorSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.polar.sdk.api.PolarBleApi;
import io.reactivex.rxjava3.disposables.Disposable;
import org.reactivestreams.Publisher;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.data.Entry;

public class EcgActivity extends AppCompatActivity {

    private String deviceId;
    private Integer batteryLevel;

    PolarBleApi api;

    Disposable ecgDisposable;
    Button startMeasuringButton;
    Button saveButton;
    Button disconnectButton;
    Button clearButton;
    TextView batteryValue;
    TextView deviceIdText;
    TextView bpmText;
    LineChart lineChart;

    boolean readyToMeasure = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_activity);
        deviceId = getIntent().getStringExtra(Constants.DEVICE_ID);
        batteryLevel = getIntent().getIntExtra(Constants.BATTERY_VALUE, -1);

        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        setObjects();
        api.setApiCallback(new PolarBleApiCallback(){
            @Override
            public void hrNotificationReceived(@NonNull String identifier, @NonNull PolarHrData data) {
                super.hrNotificationReceived(identifier, data);
                if(readyToMeasure) {
                    bpmText.setText(String.format(Locale.ENGLISH, "%d BPM", data.getHr()));
                }
            }
        });
    }

    private void setObjects() {
        lineChart = findViewById(R.id.ecgLineChart);
        ChartSetter.setEcgChart(lineChart);

        bpmText = findViewById(R.id.bpmText);

        startMeasuringButton = findViewById(R.id.ecgMeasureButton);
        startMeasuringButton.setOnClickListener(onClick -> {
            if(ecgDisposable == null) {
                startMeasuringButton.setText(getString(R.string.pause_measurement));
                saveButton.setEnabled(false);
                disconnectButton.setEnabled(false);
                clearButton.setEnabled(false);
                ecgDisposable = api
                        .requestStreamSettings(deviceId, PolarBleApi.DeviceStreamingFeature.ECG)
                        .toFlowable()
                        .flatMap((Function<PolarSensorSetting, Publisher<?>>) sensorSetting -> api.startEcgStreaming(deviceId, sensorSetting.maxSettings()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (Consumer<Object>) polarEcgData -> {
                                    if (polarEcgData instanceof PolarEcgData) {
                                        PolarEcgData ecgData = (PolarEcgData) polarEcgData;
                                        LineData lineData = lineChart.getData();
                                        ILineDataSet dataSet = lineData.getDataSetByIndex(0);
                                        for (PolarEcgData.PolarEcgDataSample data : ecgData.getSamples()) {
                                            if(!readyToMeasure && data.component2() <= 0) {
                                                readyToMeasure = true;
                                                findViewById(R.id.waitingText).setVisibility(View.GONE);
                                            } else if(readyToMeasure) {
                                                float index = dataSet.getEntryCount();
                                                float scaledData = data.component2() / 1000f;
                                                Entry entry = new Entry(index, scaledData);
                                                dataSet.addEntry(entry);

                                                lineData.notifyDataChanged();
                                                lineChart.notifyDataSetChanged();
                                                lineChart.invalidate();

                                                lineChart.moveViewToX(dataSet.getEntryCount() - Constants.MAX_VISIBLE_ENTRIES);
                                                lineChart.fitScreen();
                                                lineChart.setVisibleXRangeMaximum(Constants.MAX_VISIBLE_ENTRIES);
                                            }
                                        }
                                    }
                                },
                                (Consumer<Throwable>) error -> {
                                    showToast("Error");
                                    disposeEcg();
                                    disconnectButton.setEnabled(true);
                                    enableButtonsIfPossible();
                                    startMeasuringButton.setText(getString(R.string.start_measurement));
                                },
                                (Action) () -> {
                                    showToast("Ecg stream complete");
                                    disposeEcg();
                                    disconnectButton.setEnabled(true);
                                    enableButtonsIfPossible();
                                    startMeasuringButton.setText(getString(R.string.start_measurement));
                                }
                        );
            } else {
                disposeEcg();
                disconnectButton.setEnabled(true);
                enableButtonsIfPossible();
                startMeasuringButton.setText(getString(R.string.start_measurement));
            }
        });

        clearButton = findViewById(R.id.ecgClearButton);
        clearButton.setEnabled(false);
        clearButton.setOnClickListener(onClick -> {
            lineChart.getData().getDataSetByIndex(0).clear();
            readyToMeasure = false;
            saveButton.setEnabled(false);
            clearButton.setEnabled(false);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        });

        saveButton = findViewById(R.id.ecgSaveMeasurementButton);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(onClick -> {
            Intent saveScreen = new Intent(this, SaveDataActivity.class);
            saveScreen.putExtra(Constants.MEASUREMENT_TYPE, Constants.ECG_EXTENSION);

            LineDataSet dataSetToSave = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            String formattedData = DataFormatter.formatEntriesToString(dataSetToSave.getValues());

            saveScreen.putExtra(Constants.DATA_TO_SAVE, formattedData);
            startActivity(saveScreen);
        });

        disconnectButton = findViewById(R.id.ecgDisconnectButton);
        disconnectButton.setOnClickListener(onClick -> {
            disposeEcg();
            finish();
        });

        batteryValue = findViewById(R.id.ecgBatteryValue);
        batteryValue.setText(batteryLevel != -1 ? String.format(Locale.ENGLISH, "%d%%", batteryLevel) : "Not found");

        deviceIdText = findViewById(R.id.ecgDeviceID);
        deviceIdText.setText(deviceId);


    }

    private void disposeEcg() {
        if(ecgDisposable != null) {
            ecgDisposable.dispose();
            ecgDisposable = null;
        }
    }

    private void enableButtonsIfPossible() {
        if(lineChart.getData().getEntryCount() > 0) {
            saveButton.setEnabled(true);
            clearButton.setEnabled(true);
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show();
    }
}
