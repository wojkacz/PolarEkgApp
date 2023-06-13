package pl.kaczmarek.polarekgapp.Activity;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;
import pl.kaczmarek.polarekgapp.Utility.ToastShower;

public class SaveDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_data_activity);

        String measurementType = getIntent().getStringExtra(Constants.MEASUREMENT_TYPE);
        String dataToSave = getIntent().getStringExtra(Constants.DATA_TO_SAVE);

        EditText fileNameInput = findViewById(R.id.saveFileNameTextInput);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy-HH:mm", Locale.ENGLISH);
        String suggestedFileName = String.format("%s_%s", measurementType, format.format(Calendar.getInstance().getTime()));
        fileNameInput.setText(suggestedFileName);

        Button saveButton = findViewById(R.id.saveSaveButton);
        saveButton.setOnClickListener(onClick -> {

            String fileNameFromUser = fileNameInput.getText().toString().isEmpty()
                    ? suggestedFileName
                    : fileNameInput.getText().toString();
            String fileName = String.format("%s.%s", fileNameFromUser, measurementType);
            Context context = getApplicationContext();
            try {
                FileWriter out = new FileWriter(new File(context.getFilesDir(), fileName));
                out.write(dataToSave);
                out.close();
                ToastShower.show(this, String.format("Successfully saved data to %s!", fileName));
                finish();
            }
            catch (IOException e) {
                ToastShower.show(this, "Failed to save data!");
                finish();
            }
        });
    }
    

}
