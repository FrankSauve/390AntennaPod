package de.danoeh.antennapod.dialog;

import android.app.Activity;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBWriter;

public class RenameFolderDialog {

    private final WeakReference<Activity> activityRef;
    private final Folder folder;

    public RenameFolderDialog(Activity activity, Folder folder) {
        this.activityRef = new WeakReference<>(activity);
        this.folder = folder;
    }

    public void show() {
        Activity activity = activityRef.get();
        if(activity == null) {
            return;
        }
        new MaterialDialog.Builder(activity)
                .title(de.danoeh.antennapod.core.R.string.rename_folder_label)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(folder.getName(), folder.getName(), true, (dialog, input) -> {
                    DBWriter.setFolderName(folder, folder.getName(), input.toString());
                    folder.setName(input.toString());
                    dialog.dismiss();
                })
                .neutralText(de.danoeh.antennapod.core.R.string.reset)
                .onNeutral((dialog, which) -> dialog.getInputEditText().setText(folder.getName()))
                .negativeText(de.danoeh.antennapod.core.R.string.cancel_label)
                .onNegative((dialog, which) -> dialog.dismiss())
                .autoDismiss(false)
                .show();
    }

}