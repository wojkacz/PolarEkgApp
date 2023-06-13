package pl.kaczmarek.polarekgapp.Controller;

import android.content.Intent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.model.PolarOhrData;
import com.polar.sdk.api.model.PolarSensorSetting;

import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import pl.kaczmarek.polarekgapp.Activity.PpgActivity;
import pl.kaczmarek.polarekgapp.Activity.SaveDataActivity;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.DataFormatter;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class PpgActivityController {
    private boolean readyToMeasure = false;
    private boolean paused = true;
    private PpgActivity activity;
    private PolarBleApi api;
    private LineChart lineChart;
    private Disposable ppgDisposable;

    private String deviceId;

    public PpgActivityController(PpgActivity activity, PolarBleApi api, LineChart lineChart, String deviceId) {
        this.activity = activity;
        this.api = api;
        this.lineChart = lineChart;
        this.deviceId = deviceId;
    }

    public void onClearButtonClick() {
        disposePpg();
        lineChart.getData().getDataSetByIndex(0).clear();
        readyToMeasure = false;
        activity.disableSaveAndClearButtons();
        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void onSaveButtonClick() {
        Intent saveScreen = new Intent(activity, SaveDataActivity.class);
        saveScreen.putExtra(Constants.MEASUREMENT_TYPE, Constants.PPG_EXTENSION);

        LineDataSet dataSetToSave = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
        String formattedData = DataFormatter.formatEntriesToString(dataSetToSave.getValues());

        saveScreen.putExtra(Constants.DATA_TO_SAVE, formattedData);
        activity.startActivity(saveScreen);
    }

    public void onDisconnectButtonClick() {
        disposePpg();
        activity.finish();
    }

    public void onStartMeasurementButtonClick() {
        if(ppgDisposable == null) {
            resumeStreaming();
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
                                            activity.setWaitingTextVisibility();
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
                                ToastShower.show(activity, "Error");
                                pauseStreaming();
                            },
                            (Action) () -> {
                                ToastShower.show(activity, "Ppg stream complete");
                                pauseStreaming();
                            }
                    );
        } else {
            pauseStreaming();
        }
    }

    private void pauseStreaming() {
        disposePpg();
        paused = true;
        activity.enableDisconnectButton();
        activity.enableSaveAndClearButtonsIfPossible();
        activity.changeStartMeasuringButtonText();
    }

    private void resumeStreaming() {
        paused = false;
        activity.changeStartMeasuringButtonText();
        activity.disableDisconnectButton();
        activity.disableSaveAndClearButtons();
    }

    public boolean isPaused() {
        return paused;
    }

    private void disposePpg() {
        if(ppgDisposable != null) {
            ppgDisposable.dispose();
            ppgDisposable = null;
        }
    }
}
