package org.stellar.authentication.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;


@SuppressLint("NewApi")
public class AlertDialogMultipleChoiceList extends DialogFragment {
	private boolean[] selectedItems = new boolean[] { false, true, false, true, true, false };
	private boolean[] clickedItems = selectedItems;
	
	String[] listofapps = {
			  
            "TrackCare",
            "Citrix",
           
    };

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//final String[] listapps = getResources().getStringArray(
		//		R.array.listofapps);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity())
				.setTitle("List of apps for User")
				.setMultiChoiceItems(listofapps, selectedItems,
						new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								clickedItems[which] = isChecked;
								Toast.makeText(getActivity(), listofapps[which],
										Toast.LENGTH_SHORT).show();
							}
						})
				.setPositiveButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedItems = clickedItems;
					}
				})
				.setNegativeButton("Cancel", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog dialog = alertDialogBuilder.create();
		return dialog;
	}
}
