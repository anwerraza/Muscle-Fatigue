package com.fyp.musclefatigue.screen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fyp.musclefatigue.R;
import com.fyp.musclefatigue.databinding.ActivitySignInBinding;
import com.fyp.musclefatigue.helpers.Constants;
import com.fyp.musclefatigue.helpers.Validation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

//        SHA1: E2:14:9B:7A:53:30:ED:96:FB:19:BD:B8:E1:D1:5B:37:75:60:B6:97
//        SHA256: 55:66:58:A4:18:65:CB:A3:6F:62:6B:42:DD:B8:80:BC:B1:B3:B7:8E:F9:7C:F2:4F:51:6F:35:B3:CA:1E:0E:97

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = SignInActivity.class.getSimpleName();
    private final int GOOGLE_SIGNING_REQUEST_CODE = 100;

    private ActivitySignInBinding binding;
    private GoogleSignInClient client;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this, options);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        binding.signUpTv.setText(Html.fromHtml(getString(R.string.dont_have_an_account_sign_up)));
        binding.googleBtn.setOnClickListener(this);
        binding.signUpTv.setOnClickListener(this);
        binding.forgotPasswordTv.setOnClickListener(this);
        binding.btnContinue.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (binding.btnContinue.getId() == v.getId()) {
            login();
        }
        if (binding.googleBtn.getId() == v.getId()) {
            Intent i = client.getSignInIntent();
            startActivityForResult(i, GOOGLE_SIGNING_REQUEST_CODE);
        }
        if (binding.signUpTv.getId() == v.getId()) {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        }
        if (binding.forgotPasswordTv.getId() == v.getId()) {
            startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
        }
    }

    private void login() {

        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (!Validation.isValidEmail(email)) {
            binding.emailEt.setError("Invalid email address");
            return;
        }

        if (!Validation.isValidPassword(password)) {
            binding.passwordEt.setError("Password length must be greater then 8  and must include alpha, numeric and special characters");
            return;
        }

        progressDialog.setMessage("Please wait while login");
        progressDialog.setTitle("Login");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                checkUserDataExist();
                Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignInActivity.this, "Login Failed" + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void navigateToHome() {
        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToProfile() {
        Intent intent = new Intent(SignInActivity.this, EditProfileActivity.class);
        intent.putExtra("from",SignUpActivity.class.getSimpleName());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGNING_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                checkUserDataExist();
                                Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignInActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        });

            } catch (ApiException e) {
                e.printStackTrace();
            }

        }
    }

    private void checkUserDataExist(){
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore.getInstance().collection(Constants.FIRE_STORE_TABLE_USER)
                .document(user.getUid())
                .addSnapshotListener((value, error) -> {
            if (value != null && value.exists()) {
                navigateToHome();
                Log.d("TAG", "name already exists");
            }else{
                navigateToProfile();
            }
        });
    }
}

