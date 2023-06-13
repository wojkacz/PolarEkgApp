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

import pl.kaczmarek.polarekgapp.Controller.SavedFilesActivityController;
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class SavedFilesActivity extends AppCompatActivity {

    private SavedFilesActivityController controller;
    LayoutInflater layoutInflater;
    LinearLayout savesList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_files_activity);

        controller = new SavedFilesActivityController(this);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        savesList = findViewById(R.id.savesList);

        controller.getSavedFiles();
    }

    public void displayFilesInLayout(File[] savedFiles) {
        for (File file : savedFiles) {
            View saveFoundComponent = layoutInflater.inflate(R.layout.found_save, savesList, false);

            Button loadButton = saveFoundComponent.findViewById(R.id.loadButton);
            loadButton.setOnClickListener(onClick -> {
                controller.onFileLoadButtonClick(file);
            });

            Button deleteButton = saveFoundComponent.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(onClick -> {
                controller.deleteFileAndRemoveView(file.getName(), saveFoundComponent);
            });

            TextView savedFileName = saveFoundComponent.findViewById(R.id.savedFileNameText);
            savedFileName.setText(file.getName());

            savesList.addView(saveFoundComponent);
        }
    }

    public LinearLayout getSavesList() {
        return savesList;
    }
}
