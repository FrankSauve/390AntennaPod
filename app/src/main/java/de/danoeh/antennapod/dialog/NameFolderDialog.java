package de.danoeh.antennapod.dialog;

import android.app.Activity;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBWriter;

/**
 * Created by William on 2018-03-16.
 */

public class NameFolderDialog {

    private final WeakReference<Activity> activityRef;

    public NameFolderDialog(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    public void createFolderDialog() {
        Activity activity = activityRef.get();
        if(activity == null) {
            return;
        }
        new MaterialDialog.Builder(activity)
                .title(de.danoeh.antennapod.core.R.string.name_folder_label)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Enter folder's name", "", false, (dialog, input) -> { //does not work with de.danoeh.antennapod.core.R.string.name_folder_hint ...
                    Folder folder = new Folder(input.toString(), null);
                    DBWriter.addFolder(folder);
                    dialog.dismiss();
                })
                .negativeText(de.danoeh.antennapod.core.R.string.cancel_label)
                .onNegative((dialog, which) -> dialog.dismiss())
                .autoDismiss(false)
                .show();
    }

}
