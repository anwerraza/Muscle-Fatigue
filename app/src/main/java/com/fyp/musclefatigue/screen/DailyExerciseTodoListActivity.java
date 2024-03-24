package com.fyp.musclefatigue.screen;

import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_FRIDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_MONDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_SATURDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_SUNDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_THURSDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_TUESDAY;
import static com.fyp.musclefatigue.helpers.Constants.NOTIFICATION_ID_WEDNESDAY;
import static com.fyp.musclefatigue.helpers.Constants.TABLE_USER_ALERTS_FIELD_DAY;
import static com.fyp.musclefatigue.helpers.Constants.TABLE_USER_ALERTS_FIELD_TIME;
import static com.fyp.musclefatigue.helpers.Constants.TABLE_USER_ALERTS_FIELD_WORKOUT;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fyp.musclefatigue.AlarmReceiver;
import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.controllers.WorkoutAdapter;
import com.fyp.musclefatigue.databinding.ActivityDailyExerciseTodoListBinding;
import com.fyp.musclefatigue.databinding.AddWorkoutAlertDialogViewBinding;
import com.fyp.musclefatigue.databinding.BottomSpinnerDialogBinding;
import com.fyp.musclefatigue.helpers.AlarmScheduler;
import com.fyp.musclefatigue.helpers.Constants;
import com.fyp.musclefatigue.helpers.TimestampUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DailyExerciseTodoListActivity extends AppCompatActivity implements View.OnClickListener, WorkoutAdapter.WorkoutCallbacks {

    private static final String TAG = DailyExerciseTodoListActivity.class.getSimpleName();
    private ActivityDailyExerciseTodoListBinding binding;
    private BottomSpinnerDialogBinding spinnerDialogBinding;
    private AddWorkoutAlertDialogViewBinding workoutAlertDialogViewBinding;

    private Dialog addWorkoutAlertDialog;
    private Dialog spinnerDialog;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private WorkoutAdapter workoutAdapter;
    private List<String> workoutList;
    private List<Map<String, Object>> workoutAlerts = new ArrayList<>();

    private DocumentReference userAlertRefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDailyExerciseTodoListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == binding.backIconIv.getId()) {
            onBackPressed();
        }
        if (v.getId() == binding.addIconIv.getId()) {
            if (workoutAlerts.size() < 7){
                workoutAlertDialogViewBinding.dayOfWorkoutRG.clearCheck();
                addWorkoutAlertDialog.show();
            }else{
                Toast.makeText(this, "Buy pro version for adding more then 7 alerts", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void init() {
        firestoreDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        userAlertRefs = firestoreDB.collection(Constants.FIRE_STORE_TABLE_USER_ALERTS).document(mUser.getUid());
        binding.backIconIv.setOnClickListener(this);
        binding.addIconIv.setOnClickListener(this);
        initAddWorkoutAlertDialog();

        firestoreDB.collection(Constants.FIRE_STORE_TABLE_WORKOUTS)
                .document("mM2jW7aSrzUe2jK3gj1j")
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        workoutList = (List<String>) value.get("list");
                        initSpinnerDialog(workoutList);
                    }
                });


        workoutAdapter = new WorkoutAdapter(this, workoutAlerts);
        workoutAdapter.setListeners(this);
        binding.workoutAlertsRV.setAdapter(workoutAdapter);
        setPreviousAlerts();
    }

    private void initAddWorkoutAlertDialog() {
        workoutAlertDialogViewBinding = AddWorkoutAlertDialogViewBinding.inflate(getLayoutInflater());
        addWorkoutAlertDialog = new Dialog(this, R.style.MaterialDialogSheet);
        addWorkoutAlertDialog.setContentView(workoutAlertDialogViewBinding.getRoot()); // your custom view.
        addWorkoutAlertDialog.setCancelable(false);
        addWorkoutAlertDialog.setCanceledOnTouchOutside(false);

        workoutAlertDialogViewBinding.selectWorkoutEt.setOnClickListener(v -> {
            if (spinnerDialog != null)
                spinnerDialog.show();
        });

        workoutAlertDialogViewBinding.selectTimeEt.setOnClickListener(v -> {
            showTimePickerDialog(workoutAlertDialogViewBinding.selectTimeEt);
        });

        workoutAlertDialogViewBinding.dialogCancelTv.setOnClickListener(v -> {
            addWorkoutAlertDialog.dismiss();
        });

        workoutAlertDialogViewBinding.dialogOkBtn.setOnClickListener(v -> {

            String time = workoutAlertDialogViewBinding.selectTimeEt.getText().toString();
            String workout = workoutAlertDialogViewBinding.selectWorkoutEt.getText().toString();

            if (time.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please select workout time", Toast.LENGTH_SHORT).show();
                return;
            }

            if (workout.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please select workout", Toast.LENGTH_SHORT).show();
                return;
            }

            if (workoutAlertDialogViewBinding.dayOfWorkoutRG.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getApplicationContext(), "Please select Day", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = workoutAlertDialogViewBinding.dayOfWorkoutRG.getCheckedRadioButtonId();

            // find the radiobutton by returned id
            RadioButton radioButton = workoutAlertDialogViewBinding.dayOfWorkoutRG.findViewById(selectedId);

            HashMap<String, Object> addItem = new HashMap<>();
            addItem.put(TABLE_USER_ALERTS_FIELD_DAY, radioButton.getText().toString());
            addItem.put(TABLE_USER_ALERTS_FIELD_WORKOUT, workout);
            addItem.put(TABLE_USER_ALERTS_FIELD_TIME, time);//Add the exact URL
            addAlertIfNotExists(addItem);
            addWorkoutAlertDialog.dismiss();
        });
    }


    private void initSpinnerDialog(List<String> list) {
        spinnerDialogBinding = BottomSpinnerDialogBinding.inflate(getLayoutInflater());
        spinnerDialog = new Dialog(this, R.style.MaterialDialogBottomSheet);
        spinnerDialog.setContentView(spinnerDialogBinding.getRoot()); // your custom view.
        spinnerDialog.setCancelable(true);
        spinnerDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        spinnerDialog.getWindow().setGravity(Gravity.BOTTOM);
        spinnerDialog.getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));

        com.fyp.musclefatigue.controllers.SpinnerAdapter adapter
                = new com.fyp.musclefatigue.controllers.SpinnerAdapter(list);
        adapter.setListener(item -> {
            spinnerDialog.dismiss();
            workoutAlertDialogViewBinding.selectWorkoutEt.setText(item);
        });

        spinnerDialogBinding.bottomSpinnerDialogRV.setAdapter(adapter);
        spinnerDialogBinding.bottomDialogTitleTv.setText("Select Workout");

        spinnerDialogBinding.getRoot().setOnClickListener(v -> {
            spinnerDialog.dismiss();
        });
    }

    private void showTimePickerDialog(TextView tv) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            String AM_PM;
            if (selectedHour < 12) {
                AM_PM = "AM";
                if (selectedHour == 0)
                    selectedHour = 12;
            } else {
                AM_PM = "PM";
                if (selectedHour != 12)
                    selectedHour -= 12;
            }

            String hh = String.format(Locale.getDefault(), "%02d", selectedHour);
            String mm = String.format(Locale.getDefault(), "%02d", selectedMinute);

            tv.setText(hh + ":" + mm + " " + AM_PM);
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Workout Time");
        mTimePicker.show();
    }


    private void setPreviousAlerts() {
        userAlertRefs.addSnapshotListener((value, error) -> {
            if (value != null && value.exists()) {
                workoutAlerts.clear();
                workoutAlerts.addAll((List<Map<String, Object>>) value.get("alerts"));
                for (int i = 0; i < workoutAlertDialogViewBinding.dayOfWorkoutRG.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) workoutAlertDialogViewBinding.dayOfWorkoutRG.getChildAt(i);
                    rb.setEnabled(!workoutAlerts.stream()
                            .anyMatch(c -> c.get(TABLE_USER_ALERTS_FIELD_DAY).equals(rb.getText())));
                }
                workoutAdapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteWorkoutAlert(Map<String, Object> workoutAlert) {
        HashMap<String, Object> elementToDelete = new HashMap<>();
        elementToDelete.put(TABLE_USER_ALERTS_FIELD_DAY, workoutAlert.get(TABLE_USER_ALERTS_FIELD_DAY));
        elementToDelete.put(TABLE_USER_ALERTS_FIELD_WORKOUT, workoutAlert.get(TABLE_USER_ALERTS_FIELD_WORKOUT));
        elementToDelete.put(TABLE_USER_ALERTS_FIELD_TIME, workoutAlert.get(TABLE_USER_ALERTS_FIELD_TIME));//Add the exact URL
        userAlertRefs.update("alerts", FieldValue.arrayRemove(elementToDelete));
    }

    private void addAlertIfNotExists(HashMap<String, Object> addItem) {
        firestoreDB.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(userAlertRefs);
            if (!snapshot.exists()) {
                List<HashMap<String, Object>> alerts = new ArrayList<>();
                alerts.add(addItem);
                Alerts alert = new Alerts();
                alert.setAlerts(alerts);
                transaction.set(userAlertRefs, alert);
            }else{
                userAlertRefs.update("alerts", FieldValue.arrayUnion(addItem));
            }
            // Return null to indicate success
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Record added successfully", Toast.LENGTH_SHORT).show();
            // Document creation or update successful
        });
    }

    public class Alerts {
        private List<HashMap<String, Object>> alerts;

        public Alerts() {
            // Required empty constructor for Firestore
        }

        public List<HashMap<String, Object>> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<HashMap<String, Object>> alerts) {
            this.alerts = alerts;
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onCheckedChange(String day, String time, String workout, boolean isOn) {
        String toastMassage;
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
        if (isOn) {
            toastMassage = "Alarm On at "+TimestampUtil.getFutureDateByDayName(day) +" "+time;
            long triggerTime = TimestampUtil.dateTimeToMillis(TimestampUtil.getFutureDateByDayName(day) +" "+time);
            AlarmScheduler.scheduleAlarm(this,notifyDay,time,workout,triggerTime);
        } else {
            AlarmScheduler.cancelAlarm(this,notifyDay,time,workout);
            toastMassage = "Alarm Off!";
        }
        //Show a toast to say the alarm is turned on or off.
        Toast.makeText(this, toastMassage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(Map<String, Object> alert, int position) {
        int notifyDay = 0;
        switch (alert.get(TABLE_USER_ALERTS_FIELD_DAY).toString()){
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
        AlarmScheduler.cancelAlarm(this,
                notifyDay,
                alert.get(TABLE_USER_ALERTS_FIELD_TIME).toString(),
                alert.get(TABLE_USER_ALERTS_FIELD_WORKOUT).toString());
        deleteWorkoutAlert(alert);
    }

}