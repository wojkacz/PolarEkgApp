package pl.kaczmarek.polarekgapp.Utility;

import android.content.Context;
import android.widget.Toast;

public class ToastShower {
    public static void show(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG)
                .show();
    }
}
