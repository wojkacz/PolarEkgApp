package pl.kaczmarek.polarekgapp;

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

public class SavedEcgActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_ecg_activity);

        String data = getIntent().getStringExtra(Constants.LOADED_DATA);
        String fileName = getIntent().getStringExtra(Constants.FILE_NAME);

        TextView fileNameText = findViewById(R.id.loadedFileNameText);
        fileNameText.setText(fileName);

        LineChart lineChart = findViewById(R.id.savedEcgLineChart);
        ChartSetter.setEcgChart(lineChart);
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
