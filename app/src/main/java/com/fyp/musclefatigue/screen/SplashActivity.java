package com.fyp.musclefatigue.screen;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.databinding.ActivitySplashBinding;
import com.fyp.musclefatigue.helpers.Constants;
import com.fyp.musclefatigue.helpers.MyDialogs;
import com.fyp.musclefatigue.helpers.NotifyDialogListeners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();
    private ActivitySplashBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        askNotificationPermission();

    }

    private void navigateToNext() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserDataExist();
        } else {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }

    private void checkUserDataExist(){
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore.getInstance().collection(Constants.FIRE_STORE_TABLE_USER)
                .document(user.getUid())
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        startActivity(new Intent(this, HomeActivity.class));
                        Log.d("TAG", "name already exists");
                    }else{
                        startActivity(new Intent(this, EditProfileActivity.class)
                                .putExtra("from", SignUpActivity.class.getSimpleName())
                        );
                    }
                    finish();
                });
    }


    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                new Handler().postDelayed(this::navigateToNext, 2500);
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

                MyDialogs.notifyDialog(this,
                        getString(R.string.get_notified),
                        getString(R.string.app_name)+"would like to send you notifications about important updates, reminders, and offers. You can choose which types of notifications you want to receive and change your preferences at any time in the app settings.",
                        getString(R.string.ok),
                        getString(R.string.no_thanks),
                        new NotifyDialogListeners() {
                            @Override
                            public void onClickOk() {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                            }

                            @Override
                            public void onClickCancel() {
                                new Handler().postDelayed(() -> navigateToNext(), 2500);
                            }
                        },
                        true);
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            new Handler().postDelayed(this::navigateToNext, 2500);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    // FCM SDK (and your app) can post notifications.
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
                new Handler().postDelayed(this::navigateToNext, 1000);
            });
}