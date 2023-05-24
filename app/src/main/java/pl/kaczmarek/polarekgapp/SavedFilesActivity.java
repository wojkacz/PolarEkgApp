package pl.kaczmarek.polarekgapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileFilter;

public class SavedFilesActivity extends AppCompatActivity {

    LayoutInflater layoutInflater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_files_activity);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FileFilter filter = file -> file.getAbsolutePath().matches(".*\\.(ecg|ppg)$");
        File[] savedFiles = getApplicationContext().getFilesDir().listFiles(filter);

        if(savedFiles == null || savedFiles.length == 0) {
            showToast("No saved files found!");
            finish();
            return;
        }

        LinearLayout savesList = findViewById(R.id.savesList);

        for(File file : savedFiles) {
            View saveFoundComponent = layoutInflater.inflate(R.layout.found_save, savesList, false);

            Button loadButton = saveFoundComponent.findViewById(R.id.loadButton);
            loadButton.setOnClickListener(onClick -> showToast("Trying to load file " + file.getName()));

            Button deleteButton = saveFoundComponent.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(onClick -> {
                if(getApplicationContext().deleteFile(file.getName())) {
                    showToast("Deleted file " + file.getName());
                    savesList.removeView(saveFoundComponent);
                }
                else {
                    showToast("Unable to delete file " + file.getName());
                }
            });

            TextView savedFileName = saveFoundComponent.findViewById(R.id.savedFileNameText);
            savedFileName.setText(file.getName());

            savesList.addView(saveFoundComponent);
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show();
    }
}
