package com.fyp.musclefatigue.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.controllers.SpinnerAdapter;
import com.fyp.musclefatigue.databinding.ActivityFatigueAnalysisBinding;
import com.fyp.musclefatigue.databinding.BottomSpinnerDialogBinding;
import com.fyp.musclefatigue.databinding.FatigueAnalysisResultViewDialogBinding;
import com.fyp.musclefatigue.helpers.Constants;
import com.fyp.musclefatigue.widgets.CircularProgressDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class FatigueAnalysisActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = FatigueAnalysisActivity.class.getSimpleName();
    private ActivityFatigueAnalysisBinding binding;
    private BottomSpinnerDialogBinding spinnerDialogBinding;
    private FatigueAnalysisResultViewDialogBinding fatigueAnalysisBinding;
    private Dialog fatigueAnalysisDialog;
    private Dialog spinnerDialog;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private List<Map<String,String>> dataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFatigueAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == binding.backIconIv.getId()){
            onBackPressed();
        }
        if(v.getId() == binding.btnCalculate.getId()){
            calculateResult();
        }
        if(v.getId() == binding.selectWorkoutEt.getId()){
            if (spinnerDialog != null)
                spinnerDialog.show();
        }
    }

    private void init(){
        firestoreDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        binding.btnCalculate.setOnClickListener(this);
        binding.backIconIv.setOnClickListener(this);
        binding.selectWorkoutEt.setOnClickListener(this);

        initSpinnerDialog();
        initAnalysisResultDialog();

        firestoreDB.collection(Constants.FIRE_STORE_TABLE_USER)
                .document(mUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        double userHeight = Double.parseDouble(value.getString(Constants.HEIGHT_TABLE_USER_FIELD));
                        double userWeight = Double.parseDouble(value.getString(Constants.WEIGHT_TABLE_USER_FIELD));

                        double userHeightInCentimeters = 2.54 * userHeight;

                        binding.ageEt.setText(value.getString(Constants.AGE_TABLE_USER_FIELD) != null ? value.getString(Constants.AGE_TABLE_USER_FIELD) : "");
                        binding.bmiEt.setText(String.valueOf(calculateBMI((float) userHeightInCentimeters, (float) userWeight)));

                    }
                });


        firestoreDB.collection(Constants.FIRE_STORE_TABLE_AVERAGE_PERSON_FATIGUE)
                .document("oHrOxF8jkZ5XkUaTXfJP")
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        dataSet = (List<Map<String, String>>) value.get("fatigue_data_set_biceps");
                    }
                });

    }

    private void initSpinnerDialog() {
        spinnerDialogBinding = BottomSpinnerDialogBinding.inflate(getLayoutInflater());
        spinnerDialog = new Dialog(this, R.style.MaterialDialogBottomSheet);
        spinnerDialog.setContentView(spinnerDialogBinding.getRoot()); // your custom view.
        spinnerDialog.setCancelable(true);
        spinnerDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        spinnerDialog.getWindow().setGravity(Gravity.BOTTOM);
        spinnerDialog.getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));

        List<String> workoutList = new ArrayList<>();
        workoutList.add("Biceps");
        workoutList.add("Running");
        workoutList.add("Jogging");
        com.fyp.musclefatigue.controllers.SpinnerAdapter adapter = new com.fyp.musclefatigue.controllers.SpinnerAdapter(workoutList);
        adapter.setListener(new SpinnerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                spinnerDialog.dismiss();
                binding.selectWorkoutEt.setText(item);
            }
        });

        spinnerDialogBinding.bottomSpinnerDialogRV.setAdapter(adapter);
        spinnerDialogBinding.bottomDialogTitleTv.setText("Select Workout");

        spinnerDialogBinding.getRoot().setOnClickListener(v -> {
            spinnerDialog.dismiss();
        });
    }


    private void initAnalysisResultDialog() {
        fatigueAnalysisBinding = FatigueAnalysisResultViewDialogBinding.inflate(getLayoutInflater());
        fatigueAnalysisDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fatigueAnalysisDialog.setContentView(fatigueAnalysisBinding.getRoot()); // your custom view.
        fatigueAnalysisDialog.setCancelable(true);

        fatigueAnalysisBinding.closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigueAnalysisDialog.dismiss();
            }
        });
    }

    private void calculateResult(){
        if (dataSet != null && dataSet.size() > 0){
            String age = binding.ageEt.getText().toString();
            String bmi = binding.bmiEt.getText().toString();
            String workout = binding.selectWorkoutEt.getText().toString();

            if (age.isEmpty()){
                binding.ageEt.setError("Please enter age");
                return;
            }

            if (bmi.isEmpty()){
                binding.bmiEt.setError("Please enter BMI");
                return;
            }

            if (workout.isEmpty()){
                binding.selectWorkoutEt.setError("Please select workout");
                return;
            }

//            Map<String, String> findItem = null;
//
//            for (Map<String, String> map: dataSet) {
//                String ageRange = map.get("angRange");
//                String bmiRange = map.get("bmiRange");
//                String[] ageRangeValues = ageRange.split("-");
//                String[] bmiRangeValues = bmiRange.split("-");
//                if (Integer.parseInt(ageRangeValues[0]) <= Integer.parseInt(age)
//                        && Integer.parseInt(ageRangeValues[1]) >= Integer.parseInt(age)
//                        && Integer.parseInt(bmiRangeValues[0]) <= Double.parseDouble(bmi)
//                        && Integer.parseInt(bmiRangeValues[1]) >= Double.parseDouble(bmi)){
//                    findItem = map;
//                    break;
//                }
//            }
//            if (findItem != null) {
//                if (workout.equals("Biceps")){
//                    showResult(findItem.get("fatigueTime"),
//                            findItem.get("imageLink"),
//                            findItem.get("recommendationbicep"));
//                } else if (workout.equals("Running")) {
//                    showResult(findItem.get("fatigueTimewalk"),
//                            findItem.get("imageLink"),
//                            findItem.get("recommendationwalk"));
//                } else if (workout.equals("Jogging")) {
//                    showResult(findItem.get("fatigueTimejog"),
//                            findItem.get("imageLink"),
//                            findItem.get("recommendationjog"));
//                }
//
//            } else {
//                Toast.makeText(this, "Data not found for your values", Toast.LENGTH_SHORT).show();
//            }

            Optional<Map<String, String>> optionalItem = dataSet.stream()
                    .filter(map -> {
                        String ageRange = map.get("angRange");
                        String bmiRange = map.get("bmiRange");
                        String[] ageRangeValues = ageRange.split("-");
                        String[] bmiRangeValues = bmiRange.split("-");
                        // Check if the age value falls within the age range
                        return Integer.parseInt(ageRangeValues[0]) <= Integer.parseInt(age)
                                && Integer.parseInt(ageRangeValues[1]) >= Integer.parseInt(age)
                                && Integer.parseInt(bmiRangeValues[0]) <= Double.parseDouble(bmi)
                                && Integer.parseInt(bmiRangeValues[1]) >= Double.parseDouble(bmi);
                    }).findFirst();

            // Check if the item is present and output the result
            if (optionalItem.isPresent()) {
                Map<String, String> foundItem = optionalItem.get();
                if (workout.equals("Biceps")){
                    showResult(foundItem.get("fatigueTime"),
                            foundItem.get("imageLink"),
                            foundItem.get("recommendationbicep"));
                } else if (workout.equals("Running")) {
                    showResult(foundItem.get("fatigueTimewalk"),
                            foundItem.get("imageLinkrun"),
                            foundItem.get("recommendationwalk"));
                } else if (workout.equals("Jogging")) {
                    showResult(foundItem.get("fatigueTimejog"),
                            foundItem.get("imageLinkjog"),
                            foundItem.get("recommendationjog"));
                }

            } else {
                Toast.makeText(this, "We only Accept Age(18 to 45) and BMI(18 to 33)", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "No server data found for calculate", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResult(String fatigue, String imageLink, String recommendation){
        fatigueAnalysisBinding.fatigueTimeTv.setText(fatigue);
        fatigueAnalysisBinding.recommendationTv.setText(recommendation);
        fatigueAnalysisBinding.fatigueTimeTv.setText(fatigue);

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(this);
        circularProgressDrawable.setStrokeWidth(8f);
        circularProgressDrawable.setCenterRadius(56f);
        circularProgressDrawable.start();

        Glide.with(this)
                .load(imageLink) // image url
                .placeholder(circularProgressDrawable) // any placeholder to load at start
                .error(R.drawable.image_not_available)  // any image in case of error
                .into(fatigueAnalysisBinding.workoutImageIv);

        fatigueAnalysisDialog.show();
    }

    private float calculateBMI(float heightInCentimeter, float weightInKG){
            if (heightInCentimeter > 0 && weightInKG > 0){
                heightInCentimeter = heightInCentimeter / 100;
                float bmi = weightInKG / (heightInCentimeter * heightInCentimeter);
                return bmi;
            }else{
                Toast.makeText(this, "Invalid height or weight", Toast.LENGTH_SHORT).show();
                return 0;
            }
    }
}