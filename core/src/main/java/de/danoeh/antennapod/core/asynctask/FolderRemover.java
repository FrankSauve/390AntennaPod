package de.danoeh.antennapod.core.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

import de.danoeh.antennapod.core.R;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.service.playback.PlaybackService;
import de.danoeh.antennapod.core.storage.DBWriter;

/**
 * Created by William on 2018-04-08.
 */

/** Removes a folder in the background. */
public class FolderRemover extends AsyncTask<Void, Void, Void> {

    Context context;
    ProgressDialog dialog;
    Folder folder;
    public boolean skipOnCompletion = false;

    public FolderRemover(Context context, Folder folder) {
        super();
        this.context = context;
        this.folder = folder;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            DBWriter.deleteFolder(context, folder.getId()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if(skipOnCompletion) {
            context.sendBroadcast(new Intent(PlaybackService.ACTION_SKIP_CURRENT_EPISODE));
        }
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.folder_remover_msg));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void executeAsync() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
