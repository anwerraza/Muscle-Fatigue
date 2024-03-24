package com.fyp.musclefatigue.helpers;

public interface NotifyDialogListeners {
    void onClickOk();
    default void onClickCancel() {}
}
