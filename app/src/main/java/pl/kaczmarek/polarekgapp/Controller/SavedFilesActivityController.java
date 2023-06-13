package pl.kaczmarek.polarekgapp.Controller;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

import pl.kaczmarek.polarekgapp.Activity.SavedFilesActivity;
import pl.kaczmarek.polarekgapp.Activity.SavedMeasureActivity;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class SavedFilesActivityController {

    private SavedFilesActivity activity;

    public SavedFilesActivityController(SavedFilesActivity activity) {
        this.activity = activity;
    }

    public void getSavedFiles() {
        FileFilter filter = file -> file.getAbsolutePath().matches(String.format(".*\\.(%s|%s)$", Constants.ECG_EXTENSION, Constants.PPG_EXTENSION));
        File[] savedFiles = activity.getApplicationContext().getFilesDir().listFiles(filter);

        if (savedFiles == null || savedFiles.length == 0) {
            ToastShower.show(activity, "No saved files found!");
            activity.finish();
        } else {
            activity.displayFilesInLayout(savedFiles);
        }
    }

    public void deleteFileAndRemoveView(String name, View viewToDelete) {
        if (activity.getApplicationContext().deleteFile(name)) {
            ToastShower.show(activity, "Deleted file " + name);
            activity.getSavesList().removeView(viewToDelete);
        } else {
            ToastShower.show(activity, "Unable to delete file " + name);
        }
    }

    public String loadFileData(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(new File(activity.getApplicationContext().getFilesDir(), name)));
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            ToastShower.show(activity, "Unable to load file " + name);
            return null;
        }
    }

    public void onFileLoadButtonClick(File file) {
        String data = loadFileData(file.getName());
        if (data == null) {
            return;
        }
        if (file.getName().endsWith(Constants.ECG_EXTENSION)) {
            Intent savedEcgScreen = new Intent(activity, SavedMeasureActivity.class);
            savedEcgScreen.putExtra(Constants.LOADED_DATA, data);
            savedEcgScreen.putExtra(Constants.FILE_NAME, file.getName());
            activity.startActivity(savedEcgScreen);
        } else if (file.getName().endsWith(Constants.PPG_EXTENSION)) {
            // Obsługa ładowania plików PPG
        } else {
            ToastShower.show(activity, "Unable to load file - unknown extension!");
        }
    }

}