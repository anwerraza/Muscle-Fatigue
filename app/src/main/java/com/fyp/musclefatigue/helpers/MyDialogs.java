package com.fyp.musclefatigue.helpers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;

import com.fyp.musclefatigue.MusclesFatigue;
import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.screen.SplashActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by M.Sufwan Ansari on 10/5/2021.
 * Copyright (c) 2021 MTPixels. All rights reserved.
 */
public class MyDialogs {

    private Dialog myDialog;
    private ImageView dialogIcon;
    private Button yesBtn;
    private TextView titleTv, descriptionTv, cancelTv;
    private NotifyDialogListeners dialogListeners;


    public MyDialogs(){
        myDialog = new Dialog(MusclesFatigue.getInstance(), R.style.MaterialDialogSheet);
        initDialog();
    }

    public MyDialogs(Context context){
        myDialog = new Dialog(context, R.style.MaterialDialogSheet);
        initDialog();
    }


    private void initDialog(){

        myDialog.setContentView(R.layout.item_view_notify_dialog);
        myDialog.setCanceledOnTouchOutside(false);

        myDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        myDialog.getWindow().getDecorView().setBackground(new ColorDrawable(Color.parseColor("#4D000000")));

        dialogIcon = myDialog.findViewById(R.id.dialogIconIv);
        titleTv = myDialog.findViewById(R.id.dialogTitle);
        descriptionTv = myDialog.findViewById(R.id.dialogMessage);
        yesBtn = myDialog.findViewById(R.id.dialogOkBtn);
        cancelTv = myDialog.findViewById(R.id.dialogCancelTv);

        cancelTv.setOnClickListener(v -> {
            myDialog.cancel();
            if (dialogListeners != null)
                dialogListeners.onClickCancel();
        });

        yesBtn.setOnClickListener(v -> {
            myDialog.cancel();
            if (dialogListeners != null)
                dialogListeners.onClickOk();
        });

    }


    public void setDialogListeners(NotifyDialogListeners dialogListeners) {
        this.dialogListeners = dialogListeners;
    }

    public void show(){
        if (myDialog != null && !myDialog.isShowing())
            myDialog.show();
    }

    public void dialogCancelTvVisibility(@Visibility int visibility){
        cancelTv.setVisibility(visibility);
    }

    public void setDialogIcon(@DrawableRes int iconId) {
        dialogIcon.setImageResource(iconId);
    }

    public void setTitle(String title) {
        titleTv.setText(title);
    }

    public void setMessage(String message) {
        descriptionTv.setText(message);
    }

    public void setDialogCancelTvText(String buttonText) {
        cancelTv.setText(buttonText);
    }

    public void setDialogOkButtonText(String buttonText) {
        yesBtn.setText(buttonText);
    }


    public static void notifyDialog(Context context, String title, String message, NotifyDialogListeners dialogListeners) {
        notifyDialog(context, title, message, 0, "","",dialogListeners,true);
    }
    public static void notifyDialog(Context context, String title, String message, String okButtonText, String cancelButtonText, NotifyDialogListeners notifyDialogListeners, boolean showCancel) {
        notifyDialog(context, title, message, 0,okButtonText,cancelButtonText, notifyDialogListeners,showCancel);
    }


    public static void notifyDialog(Context context,
                                    String title,
                                    String message,
                                    @DrawableRes int iconId,
                                    String okButtonText,
                                    String cancelButtonText,
                                    NotifyDialogListeners dialogListeners,
                                    boolean showCancel) {

        Dialog notifyDialog = new Dialog(context, R.style.MaterialDialogSheet);

        notifyDialog.setContentView(R.layout.item_view_notify_dialog);
        notifyDialog.setCanceledOnTouchOutside(false);

        notifyDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        notifyDialog.getWindow().getDecorView().setBackground(new ColorDrawable(Color.parseColor("#4D000000")));

        ImageView iconIv = notifyDialog.findViewById(R.id.dialogIconIv);
        TextView titleTv = notifyDialog.findViewById(R.id.dialogTitle);
        TextView messageTv = notifyDialog.findViewById(R.id.dialogMessage);
        Button okBtn = notifyDialog.findViewById(R.id.dialogOkBtn);
        TextView cancelTv = notifyDialog.findViewById(R.id.dialogCancelTv);

        if (iconId != 0)
            iconIv.setImageResource(iconId);

        if (!title.isEmpty())
            titleTv.setText(title);

        if (!message.isEmpty())
            messageTv.setText(message);

        if (!okButtonText.isEmpty())
            okBtn.setText(okButtonText);

        if (!cancelButtonText.isEmpty())
            cancelTv.setText(cancelButtonText);

        if (!showCancel)
            cancelTv.setVisibility(View.GONE);

        cancelTv.setOnClickListener(v -> {
            notifyDialog.cancel();
            if (dialogListeners != null)
                dialogListeners.onClickCancel();
        });

        okBtn.setOnClickListener(v -> {
            notifyDialog.cancel();
            if (dialogListeners != null)
                dialogListeners.onClickOk();
        });
        notifyDialog.show();
    }


    @IntDef({VISIBLE, INVISIBLE, GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {}

    public static final int VISIBLE = 0x00000000;
    public static final int INVISIBLE = 0x00000004;
    public static final int GONE = 0x00000008;

}
