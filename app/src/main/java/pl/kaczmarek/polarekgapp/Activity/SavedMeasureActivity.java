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

import pl.kaczmarek.polarekgapp.Controller.SavedMeasureActivityController;
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
        LineChart lineChart = findViewById(R.id.savedMeasureLineChart);
        Button returnButton = findViewById(R.id.returnButton);

        fileNameText.setText(fileName);
        SavedMeasureActivityController controller = new SavedMeasureActivityController(this, lineChart, data, fileName);

        controller.setLineChartByType();
        controller.insertDataToChart();

        returnButton.setOnClickListener(onClick -> controller.onReturnButtonClick());
    }
}
