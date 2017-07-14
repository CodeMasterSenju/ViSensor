/* Copyright 2017 Artur Baltabayev, Jean-Josef Büschel, Martin Kern, Gabriel Scheibler
 *
 * This file is part of ViSensor.
 *
 * ViSensor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViSensor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViSensor.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.artur.softwareproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Martin Kern on 06.06.2017.
 * A popup to confirm file deletion.
 */

public class FileDeleteDialog extends DialogFragment
{
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.del_dialog)
                .setPositiveButton(R.string.del, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {
                        ((VRmenu)getActivity()).getAdapter().onDialogPositiveClick();//Delete file
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}

//EOF