package pl.kaczmarek.polarekgapp;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class DataFormatter {

    public static String formatEntriesToString(List<Entry> entries) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Entry data : entries) {
            stringBuilder.append(data.getY())
                    .append(';');
        }
        return stringBuilder.toString();
    }

    public static List<Entry> formatStringToEntries(String data) {
        List<Entry> entries = new ArrayList<>();
        for(String singleData : data.split(";")) {
            float singleDataFloat = Float.parseFloat(singleData);
            Entry entry = new Entry(entries.size(), singleDataFloat);
            entries.add(entry);
        }
        return entries;
    }
}
