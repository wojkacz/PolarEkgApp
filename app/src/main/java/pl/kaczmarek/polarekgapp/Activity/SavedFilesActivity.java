package pl.kaczmarek.polarekgapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;

public class SavedFilesActivity extends AppCompatActivity {

    LayoutInflater layoutInflater;
    LinearLayout savesList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_files_activity);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        savesList = findViewById(R.id.savesList);

        File[] savedFiles = getSavedFiles();
        if(savedFiles == null) { return; }

        displayFilesInLayout(savedFiles);
    }

    private File[] getSavedFiles() {
        FileFilter filter = file -> file.getAbsolutePath().matches( String.format(".*\\.(%s|%s)$", Constants.ECG_EXTENSION, Constants.PPG_EXTENSION));
        File[] savedFiles = getApplicationContext().getFilesDir().listFiles(filter);

        if(savedFiles == null || savedFiles.length == 0) {
            showToast("No saved files found!");
            finish();
            return null;
        }
        return savedFiles;
    }

    private void displayFilesInLayout(File[] savedFiles) {
        for(File file : savedFiles) {
            View saveFoundComponent = layoutInflater.inflate(R.layout.found_save, savesList, false);

            Button loadButton = saveFoundComponent.findViewById(R.id.loadButton);
            loadButton.setOnClickListener(onClick -> {
                String data = loadFileData(file.getName());
                if(data == null) { return; }
                if(file.getName().endsWith(Constants.ECG_EXTENSION)) {
                    Intent savedEcgScreen = new Intent(this, SavedMeasureActivity.class);
                    savedEcgScreen.putExtra(Constants.LOADED_DATA, data);
                    savedEcgScreen.putExtra(Constants.FILE_NAME, file.getName());
                    startActivity(savedEcgScreen);
                } else if(file.getName().endsWith(Constants.PPG_EXTENSION)) {

                } else {
                    showToast("Unable to load file - unknown extension!");
                }
            });

            Button deleteButton = saveFoundComponent.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(onClick -> deleteFileAndRemoveView(file.getName(), saveFoundComponent));

            TextView savedFileName = saveFoundComponent.findViewById(R.id.savedFileNameText);
            savedFileName.setText(file.getName());

            savesList.addView(saveFoundComponent);
        }
    }

    private void deleteFileAndRemoveView(String name, View viewToDelete) {
        if(getApplicationContext().deleteFile(name)) {
            showToast("Deleted file " + name);
            savesList.removeView(viewToDelete);
        }
        else {
            showToast("Unable to delete file " + name);
        }
    }

    private String loadFileData(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(new File(getApplicationContext().getFilesDir(), name)));
            while ((line = in.readLine()) != null) stringBuilder.append(line);
            return stringBuilder.toString();
        } catch (Exception e) {
            showToast("Unable to load file " + name);
            return null;
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show();
    }
}
