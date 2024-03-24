package com.fyp.musclefatigue.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fyp.musclefatigue.databinding.ActivityEditProfileBinding;
import com.fyp.musclefatigue.helpers.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = EditProfileActivity.class.getSimpleName();
    private ActivityEditProfileBinding binding;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private String backStackActivityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestoreDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        binding.emailEt.setOnTouchListener(null);
        binding.backIv.setOnClickListener(this);
        binding.btnContinue.setOnClickListener(this);

        if (getIntent().hasExtra("from")){
            backStackActivityName = getIntent().getStringExtra("from");
            if (backStackActivityName.equals(SignUpActivity.class.getSimpleName())){
                binding.backIv.setVisibility(View.GONE);
            }
        }

        if (mUser != null){
            binding.emailEt.setText(mUser.getEmail());
            getAndSetUserProfileData();
        } else{
            finish();
        }

    }

    @Override
    public void onClick(View v) {
        if (binding.backIv.getId() == v.getId()) {
            onBackPressed();
        }
        if (binding.btnContinue.getId() == v.getId()) {
            saveProfile();
        }
    }

    private void getAndSetUserProfileData() {
        firestoreDB.collection(Constants.FIRE_STORE_TABLE_USER)
                .document(mUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        binding.nameEt.setText(value.getString(Constants.NAME_TABLE_USER_FIELD));
                        binding.emailEt.setText(value.getString(Constants.EMAIL_TABLE_USER_FIELD));
                        binding.ageEt.setText(value.getString(Constants.AGE_TABLE_USER_FIELD));
                        binding.heightEt.setText(value.getString(Constants.HEIGHT_TABLE_USER_FIELD));
                        binding.weightEt.setText(value.getString(Constants.WEIGHT_TABLE_USER_FIELD));
                        Log.d("TAG", "name already exists");
                    }
                });
    }


    private void saveProfile() {

        String name = binding.nameEt.getText().toString().trim();
        String email = binding.emailEt.getText().toString().trim();
        String age = binding.ageEt.getText().toString().trim();
        String height = binding.heightEt.getText().toString().trim();
        String weight = binding.weightEt.getText().toString().trim();

        if (name.isEmpty()) {
            binding.nameEt.setError("Name Required");
            return;
        }

        if (email.isEmpty()) {
            binding.emailEt.setError("Email Required");
            return;
        }

        if (age.isEmpty()) {
            binding.ageEt.setError("Age Required");
            return;
        }

        if (height.isEmpty()) {
            binding.heightEt.setError("Height Required");
            return;
        }

        if (weight.isEmpty()) {
            binding.weightEt.setError("Weight Required");
            return;

        }
        if (Double.parseDouble(height) < 55 || Double.parseDouble(height) > 96) {
            binding.heightEt.setError("Height should be between 55 to 96 inches");
            return;
        }
        if (Double.parseDouble(weight) < 1 || Double.parseDouble(weight) > 120) {
            binding.weightEt.setError("Weight should be between 1 to 120 kg");
            return;
        }

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put(Constants.NAME_TABLE_USER_FIELD, name);
        user.put(Constants.EMAIL_TABLE_USER_FIELD, email);
        user.put(Constants.AGE_TABLE_USER_FIELD, age);
        user.put(Constants.HEIGHT_TABLE_USER_FIELD, height);
        user.put(Constants.WEIGHT_TABLE_USER_FIELD, weight);

        firestoreDB.collection(Constants.FIRE_STORE_TABLE_USER)
                .document(mUser.getUid())
                .set(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(EditProfileActivity.this, "Profile Update Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding document "+ e, Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding document", e);
                });
    }
}