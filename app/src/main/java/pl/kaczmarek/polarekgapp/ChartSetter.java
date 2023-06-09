package pl.kaczmarek.polarekgapp;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Locale;

public class ChartSetter {
    public static void setEcgChart(LineChart lineChart){
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDoubleTapToZoomEnabled(false);

        LineData lineData = new LineData();
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), "ECG");
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);

        lineData.addDataSet(dataSet);
        lineChart.setData(lineData);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMaximum(2f);
        leftAxis.setAxisMinimum(-1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.format(Locale.ENGLISH,"%f [V]", value);
            }
        });

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("ECG Chart");
        lineChart.setDescription(description);

        lineChart.invalidate();
    }
}
