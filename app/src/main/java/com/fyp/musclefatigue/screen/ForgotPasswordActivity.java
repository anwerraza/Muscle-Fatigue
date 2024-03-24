package com.fyp.musclefatigue.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.databinding.ActivityForgotPasswordBinding;
import com.fyp.musclefatigue.helpers.Validation;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ForgotPasswordActivity.class.getSimpleName();
    private ActivityForgotPasswordBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        binding.signInTv.setText(Html.fromHtml(getString(R.string.back_to_sign_in)));
        binding.signInTv.setOnClickListener(this);
        binding.btnContinue.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (binding.signInTv.getId() == v.getId()){
            onBackPressed();
        }
        if (binding.btnContinue.getId() == v.getId()){
            resetPassword();
        }
    }

    private void resetPassword() {

        String email = binding.emailEt.getText().toString().trim();

        if (!Validation.isValidEmail(email)) {
            binding.emailEt.setError("Invalid email address");
            return;
        }

        progressDialog.setMessage("Please wait while login");
        progressDialog.setTitle("Login");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPasswordActivity.this, "Reset Link successfully send to your email", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "Login Failed" + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}