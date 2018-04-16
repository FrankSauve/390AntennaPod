package de.danoeh.antennapod.dialog;

import android.app.Activity;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBWriter;

public class RenameFeedDialog {

    private final WeakReference<Activity> activityRef;
    private final Feed feed;
    private final Folder folder;

    public RenameFeedDialog(Activity activity, Feed feed) {
        this.activityRef = new WeakReference<>(activity);
        this.feed = feed;
        this.folder = null;
    }

    public RenameFeedDialog(Activity activity, Folder folder) {
        this.activityRef = new WeakReference<>(activity);
        this.feed = null;
        this.folder = folder;
    }

    public void show() {
        Activity activity = activityRef.get();
        if(activity == null) {
            return;
        }
        if(feed == null) {
            new MaterialDialog.Builder(activity)
                    .title(de.danoeh.antennapod.core.R.string.rename_feed_label)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(folder.getName(), folder.getName(), true, (dialog, input) -> {
                        folder.setName(input.toString());
                        DBWriter.setFolderCustomTitle(folder);
                        dialog.dismiss();
                    })
                    .neutralText(de.danoeh.antennapod.core.R.string.reset)
                    .onNeutral((dialog, which) -> dialog.getInputEditText().setText(folder.getName()))
                    .negativeText(de.danoeh.antennapod.core.R.string.cancel_label)
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .autoDismiss(false)
                    .show();
        }
        else{

            new MaterialDialog.Builder(activity)
                    .title(de.danoeh.antennapod.core.R.string.rename_feed_label)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(feed.getTitle(), feed.getTitle(), true, (dialog, input) -> {
                        feed.setCustomTitle(input.toString());
                        DBWriter.setFeedCustomTitle(feed);
                        dialog.dismiss();
                    })
                    .neutralText(de.danoeh.antennapod.core.R.string.reset)
                    .onNeutral((dialog, which) -> dialog.getInputEditText().setText(feed.getFeedTitle()))
                    .negativeText(de.danoeh.antennapod.core.R.string.cancel_label)
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .autoDismiss(false)
                    .show();

        }
    }

}
