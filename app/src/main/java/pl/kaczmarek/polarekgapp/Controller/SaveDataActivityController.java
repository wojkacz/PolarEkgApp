package pl.kaczmarek.polarekgapp.Controller;


import android.content.Context;
import android.icu.util.Calendar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import pl.kaczmarek.polarekgapp.Activity.SaveDataActivity;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class SaveDataActivityController {
    String measurementType;
    SaveDataActivity activity;
    String dataToSave;

    public SaveDataActivityController(SaveDataActivity activity, String measurementType, String dataToSave) {
        this.activity = activity;
        this.measurementType = measurementType;
        this.dataToSave = dataToSave;
    }

    public String getSuggestedFileName() {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy-HH:mm", Locale.ENGLISH);
        return String.format("%s_%s", measurementType, format.format(Calendar.getInstance().getTime()));
    }

    public void onSaveButtonClick(String fileNameInput) {
        String fileNameFromUser = fileNameInput.isEmpty()
                ? getSuggestedFileName()
                : fileNameInput;
        String fileName = String.format("%s.%s", fileNameFromUser, measurementType);
        Context context = activity.getApplicationContext();
        try {
            FileWriter out = new FileWriter(new File(context.getFilesDir(), fileName));
            out.write(dataToSave);
            out.close();
            ToastShower.show(activity, String.format("Successfully saved data to %s!", fileName));
            activity.finish();
        }
        catch (IOException e) {
            ToastShower.show(activity, "Failed to save data!");
            activity.finish();
        }
    }
}
