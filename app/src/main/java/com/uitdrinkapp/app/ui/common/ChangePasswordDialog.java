package com.uitdrinkapp.app.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uitdrinkapp.app.R;

/**
 * UI-only "change password" dialog shared by buyer and seller profiles.
 * The confirm button does not change any password yet.
 */
public final class ChangePasswordDialog {
    private ChangePasswordDialog() { }

    public static void show(Context context) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.pw_title)
                .setView(content)
                .setNegativeButton(R.string.pw_cancel, null)
                .setPositiveButton(R.string.pw_confirm, (dialog, which) ->
                        Toast.makeText(context, R.string.home_unavailable, Toast.LENGTH_SHORT).show())
                .show();
    }
}
