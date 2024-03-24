package com.fyp.musclefatigue.screen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.helpers.Constants;
import com.fyp.musclefatigue.helpers.MyDialogs;
import com.fyp.musclefatigue.helpers.NotifyDialogListeners;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.fyp.musclefatigue.databinding.ActivityHomeMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    public static final String TAG = HomeActivity.class.getSimpleName();
    private ActivityHomeMainBinding binding;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.navView.setNavigationItemSelectedListener(this);
        binding.contentMain.menuIconIv.setOnClickListener(this);
        binding.logoutBtn.setOnClickListener(this);
        init();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == binding.contentMain.menuIconIv.getId()){
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                binding.drawerLayout.openDrawer(GravityCompat.START);
            else
                binding.drawerLayout.closeDrawer(GravityCompat.END);
        }
        if (v.getId() == binding.logoutBtn.getId()){
            MyDialogs.notifyDialog(this,"Alert!","Are you sure! You want to logout", () -> {
                mAuth.signOut();
                Intent logoutIntent = new Intent(HomeActivity.this, SignInActivity.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(logoutIntent);
            });
        }
        if (v.getId() == binding.contentMain.analysisMusclesFatigueLL.getId()){
            startActivity(new Intent(this,FatigueAnalysisActivity.class));
        }
        if (v.getId() == binding.contentMain.trackMusclesFatigueLL.getId()){
            startActivity(new Intent(this, TrackMusclesFatigueActivity.class));
        }
    }

    private void init(){
        firestoreDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        binding.contentMain.analysisMusclesFatigueLL.setOnClickListener(this);
        binding.contentMain.trackMusclesFatigueLL.setOnClickListener(this);

        TextView tv = binding.navView.getHeaderView(0).findViewById(R.id.userNameTv);

        firestoreDB.collection(Constants.FIRE_STORE_TABLE_USER)
                .document(mUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        tv.setText(value.getString(Constants.NAME_TABLE_USER_FIELD));
                        Log.d("TAG", "name already exists");
                    }
                });

        firestoreDB.collection(Constants.FIRE_STORE_TABLE_AVERAGE_PERSON_FATIGUE)
                .document("GGBzaww4uLLWtVcuFsvx")
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        List<Map<String,Object>> dataSet = (List<Map<String, Object>>) value.get("data_set");
                        dailyGymChart(dataSet);
                    }
                });


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        if (item.getItemId() == R.id.nav_home){
            startActivity(new Intent(HomeActivity.this, HomeActivity.class));
        }
        if (item.getItemId() == R.id.nav_edit_profile){
            startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
        }
        if (item.getItemId() == R.id.nav_daily_workout_alert){
            startActivity(new Intent(HomeActivity.this, DailyExerciseTodoListActivity.class));
        }
        if (item.getItemId() == R.id.nav_analysis_muscles_fatigue){
            startActivity(new Intent(this,FatigueAnalysisActivity.class));
        }
        if (item.getItemId() == R.id.nav_track_muscles_fatigue){
            startActivity(new Intent(this, TrackMusclesFatigueActivity.class));
        }

        //close navigation drawer
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void dailyGymChart(List<Map<String,Object>> data_set) {

        binding.contentMain.dailyGymTimeChart.clear();

        binding.contentMain.dailyGymTimeChart.setDrawBarShadow(false);
        binding.contentMain.dailyGymTimeChart.getDescription().setEnabled(false);
        binding.contentMain.dailyGymTimeChart.setPinchZoom(false);

//        int[] gymTimes = {5, 10, 8, 11, 2, 4};
//        String[] labels = {"Sat","Sun","Mon","Tue","Wed","Thu","Fri"};

        String[] labels = new String[data_set.size()];

        for (int i = 0; i < data_set.size(); i++) {
            labels[i] = data_set.get(i).get("age_range").toString();
        }

        XAxis xAxis = binding.contentMain.dailyGymTimeChart.getXAxis();
        xAxis.setCenterAxisLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(labels.length);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.textGrey));
        xAxis.setTextSize(10);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));


        YAxis leftAxis = binding.contentMain.dailyGymTimeChart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.textGrey));
        leftAxis.setTextSize(12);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(2);
        leftAxis.setLabelCount(5, true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ((int) value) + "min(s)";
            }
        });

        binding.contentMain.dailyGymTimeChart.getAxisRight().setEnabled(false);
        binding.contentMain.dailyGymTimeChart.getLegend().setEnabled(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < data_set.size(); i++) {
            ArrayList<BarEntry> barOne = new ArrayList<>();
            barOne.add(new BarEntry(i, Float.parseFloat(data_set.get(i).get("fatigue_time").toString())));
            BarDataSet set1 = new BarDataSet(barOne, String.valueOf(i + 1));
            set1.setHighlightEnabled(false);
            set1.setDrawValues(false);
            set1.setGradientColor(ContextCompat.getColor(this, R.color.colorPrimaryLight), ContextCompat.getColor(this, R.color.colorPrimary));
            dataSets.add(set1);
        }

        BarData data = new BarData(dataSets);

        binding.contentMain.dailyGymTimeChart.setData(data);
        binding.contentMain.dailyGymTimeChart.setScaleEnabled(false);
        binding.contentMain.dailyGymTimeChart.setVisibleXRangeMaximum(15);
        binding.contentMain.dailyGymTimeChart.animateY(1500);
        binding.contentMain.dailyGymTimeChart.invalidate();

    }
}