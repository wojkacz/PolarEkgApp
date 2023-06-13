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
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.ChartSetter;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.DataFormatter;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class PpgActivity extends AppCompatActivity {

    private String deviceId;
    private Integer batteryLevel;

    PolarBleApi api;

    Disposable ppgDisposable;
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
        setContentView(R.layout.ppg_activity);
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
        lineChart = findViewById(R.id.ppgLineChart);
        ChartSetter.setPpgChart(lineChart);

        bpmText = findViewById(R.id.ppgBpmText);

        startMeasuringButton = findViewById(R.id.ppgMeasureButton);
        startMeasuringButton.setOnClickListener(onClick -> {
            if(ppgDisposable == null) {
                startMeasuringButton.setText(getString(R.string.pause_measurement));
                saveButton.setEnabled(false);
                disconnectButton.setEnabled(false);
                clearButton.setEnabled(false);
                ppgDisposable = api
                        .requestStreamSettings(deviceId, PolarBleApi.DeviceStreamingFeature.PPG)
                        .toFlowable()
                        .flatMap((Function<PolarSensorSetting, Publisher<?>>) sensorSetting -> api.startOhrStreaming(deviceId, sensorSetting.maxSettings()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (Consumer<Object>) polarPpgData -> {
                                    if (polarPpgData instanceof PolarOhrData) {
                                        PolarOhrData ppgData = (PolarOhrData) polarPpgData;
                                        LineData lineData = lineChart.getData();
                                        ILineDataSet dataSet = lineData.getDataSetByIndex(0);
                                        for (Integer data : ppgData.getSamples().get(0).getChannelSamples()) {
                                            if(!readyToMeasure) {
                                                readyToMeasure = true;
                                                findViewById(R.id.waitingText).setVisibility(View.GONE);
                                            } else {
                                                float index = dataSet.getEntryCount();
                                                Entry entry = new Entry(index, data);
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
                                    ToastShower.show(this, "Error");
                                    disposePpg();
                                    disconnectButton.setEnabled(true);
                                    enableButtonsIfPossible();
                                    startMeasuringButton.setText(getString(R.string.start_measurement));
                                },
                                (Action) () -> {
                                    ToastShower.show(this, "Ppg stream complete");
                                    disposePpg();
                                    disconnectButton.setEnabled(true);
                                    enableButtonsIfPossible();
                                    startMeasuringButton.setText(getString(R.string.start_measurement));
                                }
                        );
            } else {
                disposePpg();
                disconnectButton.setEnabled(true);
                enableButtonsIfPossible();
                startMeasuringButton.setText(getString(R.string.start_measurement));
            }
        });

        clearButton = findViewById(R.id.ppgClearButton);
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

        saveButton = findViewById(R.id.ppgSaveMeasurementButton);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(onClick -> {
            Intent saveScreen = new Intent(this, SaveDataActivity.class);
            saveScreen.putExtra(Constants.MEASUREMENT_TYPE, Constants.PPG_EXTENSION);

            LineDataSet dataSetToSave = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            String formattedData = DataFormatter.formatEntriesToString(dataSetToSave.getValues());

            saveScreen.putExtra(Constants.DATA_TO_SAVE, formattedData);
            startActivity(saveScreen);
        });

        disconnectButton = findViewById(R.id.ppgDisconnectButton);
        disconnectButton.setOnClickListener(onClick -> {
            disposePpg();
            finish();
        });

        batteryValue = findViewById(R.id.ppgBatteryValue);
        batteryValue.setText(batteryLevel != -1 ? String.format(Locale.ENGLISH, "%d%%", batteryLevel) : "Not found");

        deviceIdText = findViewById(R.id.ppgDeviceID);
        deviceIdText.setText(deviceId);
    }

    private void disposePpg() {
        if(ppgDisposable != null) {
            ppgDisposable.dispose();
            ppgDisposable = null;
        }
    }

    private void enableButtonsIfPossible() {
        if(lineChart.getData().getEntryCount() > 0) {
            saveButton.setEnabled(true);
            clearButton.setEnabled(true);
        }
    }

    
}
