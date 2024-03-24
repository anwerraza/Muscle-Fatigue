package com.fyp.musclefatigue.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.databinding.ActivitySignupBinding;
import com.fyp.musclefatigue.helpers.Validation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private ActivitySignupBinding binding;
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signInTv.setText(Html.fromHtml(getString(R.string.already_have_an_account_sign_in)));
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        binding.signInTv.setOnClickListener(this);
        binding.btnContinue.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (binding.signInTv.getId() == v.getId()) {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        }
        if (binding.btnContinue.getId() == v.getId()) {
            registerUser();
        }
    }

    private void registerUser() {

        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();
        String password2 = binding.confirmPasswordEt.getText().toString().trim();

        if (!Validation.isValidEmail(email)) {
            binding.emailEt.setError("Invalid email address");
            return;
        }

        if (!Validation.isValidPassword(password)) {
            binding.passwordEt.setError("Password length must be greater then 8  and must include alpha, numeric and special characters");
            return;
        }

        if (!password.equals(password2)) {
            binding.confirmPasswordEt.setError("Confirm Password not match");
            return;
        }


        progressDialog.setMessage("Please wait while signing up");
        progressDialog.setTitle("Signing Up");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    navigateToEditProfile();
                    Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "SignUp failed" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(SignUpActivity.this, EditProfileActivity.class);
        intent.putExtra("from",SignUpActivity.class.getSimpleName());
        startActivity(intent);
        finish();
    }


}

