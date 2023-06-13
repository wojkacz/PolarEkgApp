package pl.kaczmarek.polarekgapp.Controller;

import android.content.Intent;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.model.PolarEcgData;
import com.polar.sdk.api.model.PolarSensorSetting;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import pl.kaczmarek.polarekgapp.Activity.EcgActivity;
import pl.kaczmarek.polarekgapp.Activity.SaveDataActivity;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.DataFormatter;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class EcgActivityController {
    private boolean readyToMeasure = false;
    private boolean paused = true;
    private EcgActivity activity;
    private PolarBleApi api;
    private LineChart lineChart;
    private Disposable ecgDisposable;

    private String deviceId;

    public EcgActivityController(EcgActivity activity, PolarBleApi api, LineChart lineChart, String deviceId) {
        this.activity = activity;
        this.api = api;
        this.lineChart = lineChart;
        this.deviceId = deviceId;
    }

    public void onClearButtonClick() {
        disposeEcg();
        lineChart.getData().getDataSetByIndex(0).clear();
        readyToMeasure = false;
        activity.disableSaveAndClearButtons();
        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void onSaveButtonClick() {
        Intent saveScreen = new Intent(activity, SaveDataActivity.class);
        saveScreen.putExtra(Constants.MEASUREMENT_TYPE, Constants.ECG_EXTENSION);

        LineDataSet dataSetToSave = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
        String formattedData = DataFormatter.formatEntriesToString(dataSetToSave.getValues());

        saveScreen.putExtra(Constants.DATA_TO_SAVE, formattedData);
        activity.startActivity(saveScreen);
    }

    public void onDisconnectButtonClick() {
        disposeEcg();
        activity.finish();
    }

    public void onStartMeasurementButtonClick() {
        if(ecgDisposable == null) {
            resumeStreaming();
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
                                            activity.setWaitingTextVisibility();
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
                                ToastShower.show(activity,"Error");
                                pauseStreaming();
                            },
                            (Action) () -> {
                                ToastShower.show(activity, "Ecg stream complete");
                                pauseStreaming();
                            }
                    );
        } else {
            pauseStreaming();
        }
    }

    private void pauseStreaming() {
        paused = true;
        disposeEcg();
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

    private void disposeEcg() {
        if(ecgDisposable != null) {
            ecgDisposable.dispose();
            ecgDisposable = null;
        }
    }

}