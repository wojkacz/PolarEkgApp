package pl.kaczmarek.polarekgapp.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import pl.kaczmarek.polarekgapp.Controller.SaveDataActivityController;
import pl.kaczmarek.polarekgapp.R;
import pl.kaczmarek.polarekgapp.Utility.Constants;

public class SaveDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_data_activity);

        String measurementType = getIntent().getStringExtra(Constants.MEASUREMENT_TYPE);
        String dataToSave = getIntent().getStringExtra(Constants.DATA_TO_SAVE);

        EditText fileNameInput = findViewById(R.id.saveFileNameTextInput);
        Button saveButton = findViewById(R.id.saveSaveButton);

        SaveDataActivityController controller = new SaveDataActivityController(this, measurementType, dataToSave);
        fileNameInput.setText(controller.getSuggestedFileName());

        saveButton.setOnClickListener(onClick -> controller.onSaveButtonClick(fileNameInput.getText().toString()));
    }
    

}
