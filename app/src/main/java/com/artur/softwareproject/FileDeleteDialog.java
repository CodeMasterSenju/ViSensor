package com.artur.softwareproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Martin Kern on 06.06.2017.
 * A popup to confirm file deletion.
 */

public class FileDeleteDialog extends DialogFragment {

    private static final String TAG = VRmenu.class.getSimpleName();

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.del_dialog)
                .setPositiveButton(R.string.del, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    NoticeDialogListener mListener;

    public void onAttach(Context context) {
        Log.d(TAG,"Delete Dialog onAttach was called.");
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            Log.d(TAG,"Delete Dialog onAttach success.");
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            Log.d(TAG,"Delete Dialog onAttach failure.");
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }



}
