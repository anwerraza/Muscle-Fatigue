package com.fyp.musclefatigue.widgets;

public interface DrawableClickListener {

    enum DrawablePosition {TOP, BOTTOM, LEFT, RIGHT}
    void onClick(DrawablePosition target);
}
