package com.fyp.musclefatigue.models;

public class WorkoutAlert {

    private String workout;
    private String workoutTime;
    private String dayOfWorkout;
    private boolean isAlertOn;

    public WorkoutAlert() {
    }

    public WorkoutAlert(String workout, String workoutTime, String dayOfWorkout) {
        this.workout = workout;
        this.workoutTime = workoutTime;
        this.dayOfWorkout = dayOfWorkout;
    }

    public String getWorkout() {
        return workout;
    }

    public void setWorkout(String workout) {
        this.workout = workout;
    }

    public String getWorkoutTime() {
        return workoutTime;
    }

    public void setWorkoutTime(String workoutTime) {
        this.workoutTime = workoutTime;
    }

    public String getDayOfWorkout() {
        return dayOfWorkout;
    }

    public void setDayOfWorkout(String dayOfWorkout) {
        this.dayOfWorkout = dayOfWorkout;
    }

    public boolean isAlertOn() {
        return isAlertOn;
    }

    public void setAlertOn(boolean alertOn) {
        isAlertOn = alertOn;
    }

    @Override
    public String toString() {
        return "WorkoutAlert{" +
                "workout='" + workout + '\'' +
                ", workoutTime='" + workoutTime + '\'' +
                ", dayOfWorkout='" + dayOfWorkout + '\'' +
                ", isAlertOn=" + isAlertOn +
                '}';
    }
}
