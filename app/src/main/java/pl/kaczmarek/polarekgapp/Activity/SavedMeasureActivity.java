package pl.kaczmarek.polarekgapp.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.ChartSetter;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.DataFormatter;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class SavedMeasureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_measure_activity);

        String data = getIntent().getStringExtra(Constants.LOADED_DATA);
        String fileName = getIntent().getStringExtra(Constants.FILE_NAME);

        TextView fileNameText = findViewById(R.id.loadedFileNameText);
        fileNameText.setText(fileName);

        LineChart lineChart = findViewById(R.id.savedMeasureLineChart);
        if(fileName.endsWith(Constants.ECG_EXTENSION)) {
            ChartSetter.setEcgChart(lineChart);
        } else if(fileName.endsWith(Constants.PPG_EXTENSION)) {
            ChartSetter.setPpgChart(lineChart);
        } else {
            ToastShower.show(this, "Incorrect file extension!");
            finish();
        }
        insertDataToChart(data, lineChart);

        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(onClick -> finish());
    }

    private void insertDataToChart(String data, LineChart lineChart) {
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

}
