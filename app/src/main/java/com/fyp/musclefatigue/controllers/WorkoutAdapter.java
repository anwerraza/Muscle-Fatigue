package com.fyp.musclefatigue.controllers;

import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_FRIDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_MONDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_SATURDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_SUNDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_THURSDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_TUESDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_WEDNESDAY;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.musclefatigue.AlarmReceiver;
import com.fyp.musclefatigue.databinding.WorkoutAlertItemViewBinding;
import com.fyp.musclefatigue.models.WorkoutAlert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.MyViewHolder> {

    private List<Map<String, Object>> list;
    private Context mCtx;
    private WorkoutCallbacks listeners;

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private WorkoutAlertItemViewBinding binding;

        public MyViewHolder(WorkoutAlertItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public WorkoutAdapter(Context mContext, List<Map<String, Object>> list) {
        this.mCtx = mContext;
        this.list = list;
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void addAlert(Map<String, Object> alert){
        if (list != null){
            list.add(list.size()-1,alert);
            notifyItemInserted(list.size()-1);
        }
    }

    public void delete(int position){
        if (list != null){
            list.remove(position);
            notifyItemInserted(position);
        }
    }

    public void setListeners(WorkoutCallbacks listeners) {
        this.listeners = listeners;
    }

    @Override
    public WorkoutAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WorkoutAdapter.MyViewHolder(WorkoutAlertItemViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @Override
    public void onBindViewHolder(WorkoutAdapter.MyViewHolder holder, int position) {
        Map<String, Object> model = list.get(position);

        holder.binding.workoutNameTv.setText(model.get("workout").toString());
        holder.binding.workoutTimeTv.setText("At "+model.get("workoutTime").toString());
        holder.binding.dayOfWorkoutTv.setText(model.get("dayOfWorkout").toString().substring(0,3));
        holder.binding.workoutAlertSwitch.setChecked(checkAlarmIsSet(model.get("dayOfWorkout").toString()));

        holder.binding.workoutAlertSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listeners != null && buttonView.isPressed()){
                listeners.onCheckedChange(
                        model.get("dayOfWorkout").toString(),
                        model.get("workoutTime").toString(),
                        model.get("workout").toString(),
                        isChecked
                );
            }
        });

        holder.binding.deleteWorkoutAlertIV.setOnClickListener(v -> {
            if (listeners != null)
                listeners.onDelete(model, position);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private boolean checkAlarmIsSet(String day){
        int notifyDay = 0;
        switch (day){
            case "Sunday":
                notifyDay = NOTIFICATION_ID_SUNDAY;
                break;
            case "Monday":
                notifyDay = NOTIFICATION_ID_MONDAY;
                break;
            case "Tuesday":
                notifyDay = NOTIFICATION_ID_TUESDAY;
                break;
            case "Wednesday":
                notifyDay = NOTIFICATION_ID_WEDNESDAY;
                break;
            case "Thursday":
                notifyDay = NOTIFICATION_ID_THURSDAY;
                break;
            case "Friday":
                notifyDay = NOTIFICATION_ID_FRIDAY;
                break;
            case "Saturday":
                notifyDay = NOTIFICATION_ID_SATURDAY;
                break;
        }
        Intent notifyIntent = new Intent(mCtx, AlarmReceiver.class);
        return (PendingIntent.getBroadcast(mCtx, notifyDay, notifyIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE) != null);
    }

    public interface WorkoutCallbacks{
        void onCheckedChange(String day, String time, String workout, boolean isOn);
        void onDelete(Map<String, Object> alert, int position);
    }
}
