package pl.kaczmarek.polarekgapp.Controller;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

import pl.kaczmarek.polarekgapp.Activity.SavedMeasureActivity;
import pl.kaczmarek.polarekgapp.Utility.ChartSetter;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.DataFormatter;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class SavedMeasureActivityController {
    SavedMeasureActivity activity;
    LineChart lineChart;
    String data;
    String fileName;

    public SavedMeasureActivityController(SavedMeasureActivity activity, LineChart lineChart, String data, String fileName) {
        this.activity = activity;
        this.lineChart = lineChart;
        this.data = data;
        this.fileName = fileName;
    }

    public void setLineChartByType() {
        if(fileName.endsWith(Constants.ECG_EXTENSION)) {
            ChartSetter.setEcgChart(lineChart);
        } else if(fileName.endsWith(Constants.PPG_EXTENSION)) {
            ChartSetter.setPpgChart(lineChart);
        } else {
            ToastShower.show(activity, Constants.LOADING_INCORRECT_EXTENSION_MESSAGE);
            activity.finish();
        }
    }

    public void insertDataToChart() {
        List<Entry> entries = DataFormatter.formatStringToEntries(data);
        LineData lineData = lineChart.getData();
        ILineDataSet dataSet = lineData.getDataSetByIndex(0);

        for(Entry entry : entries) {
            dataSet.addEntry(entry);
        }

        lineChart.moveViewToX(dataSet.getEntryCount() - Constants.MAX_VISIBLE_ENTRIES);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        lineChart.fitScreen();
        lineChart.setVisibleXRangeMaximum(Constants.MAX_VISIBLE_ENTRIES);
    }

    public void onReturnButtonClick() {
        activity.finish();
    }
}
