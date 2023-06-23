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
            ToastShower.show(activity, Constants.SAVING_NO_FILES_MESSAGE);
            activity.finish();
        } else {
            activity.displayFilesInLayout(savedFiles);
        }
    }

    public void deleteFileAndRemoveView(String name, View viewToDelete) {
        if (activity.getApplicationContext().deleteFile(name)) {
            ToastShower.show(activity, String.format(Constants.DELETING_SUCCESS_MESSAGE, name));
            activity.getSavesList().removeView(viewToDelete);
        } else {
            ToastShower.show(activity, String.format(Constants.DELETING_FAILED_MESSAGE, name));
        }
    }

    private String loadFileData(String name) {
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
            ToastShower.show(activity, String.format(Constants.LOADING_FAILED_MESSAGE, name));
            return null;
        }
    }

    public void onFileLoadButtonClick(File file) {
        String data = loadFileData(file.getName());
        if (data == null) {
            return;
        }
        if (isExtensionMatching(file.getName())) {
            Intent savedMeasureScreen = new Intent(activity, SavedMeasureActivity.class);
            savedMeasureScreen.putExtra(Constants.LOADED_DATA, data);
            savedMeasureScreen.putExtra(Constants.FILE_NAME, file.getName());
            activity.startActivity(savedMeasureScreen);
        } else {
            ToastShower.show(activity, String.format(Constants.LOADING_FAILED_MESSAGE, file.getName()));
        }
    }

    private boolean isExtensionMatching(String name) {
        return name.endsWith(Constants.PPG_EXTENSION)
            || name.endsWith(Constants.ECG_EXTENSION);
    }

}