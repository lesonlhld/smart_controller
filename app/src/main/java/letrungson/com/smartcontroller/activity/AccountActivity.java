package letrungson.com.smartcontroller.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import letrungson.com.smartcontroller.R;

public class AccountActivity extends Activity {
    private static final String TAG = "EmailPassword";
    TextView inputEmail, inputPassword, inputPasswordConfirm, btnLogin;
    Button btnSignup;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            login();
        } else {
            updatePassword();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void createAccount() {
        setContentView(R.layout.activity_register);

        inputEmail = findViewById(R.id.email_edt_text);
        inputPassword = findViewById(R.id.pass_edt_text);
        inputPasswordConfirm = findViewById(R.id.pass_edt_text_confirm);
        btnLogin = findViewById(R.id.login_btn);
        btnSignup = findViewById(R.id.signup_btn);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();
                final String passwordConfirm = inputPasswordConfirm.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_email_required), Toast.LENGTH_LONG).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_password_required), Toast.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.minimum_password), Toast.LENGTH_LONG).show();
                } else if (passwordConfirm.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_confirm_password_required), Toast.LENGTH_LONG).show();
                } else if (!password.equals(passwordConfirm)) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_match_password), Toast.LENGTH_LONG).show();
                } else {
                    // [START create_user_with_email]
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(AccountActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        if (task.getException().toString().indexOf("The email address is badly formatted") != -1) {
                                            Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_invalid_email), Toast.LENGTH_LONG).show();
                                        } else if (task.getException().toString().indexOf("The email address is already in use by another account") != -1) {
                                            Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_exist_email), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(AccountActivity.this, getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();
                                        }
                                        Log.w(TAG, "signUpWithEmail:failure", task.getException());
                                    } else {
                                        startActivity(new Intent(AccountActivity.this, MainActivity.class));
                                        Log.d(TAG, "signUpWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        finish();
                                    }
                                }
                            });
                }
                // [END create_user_with_email]
            }
        });
    }

    private void login() {
        setContentView(R.layout.activity_login);
        TextView inputEmail, inputPassword, btnSignup, forget_pass;
        Button btnLogin;

        inputEmail = findViewById(R.id.email_edt_text);
        inputPassword = findViewById(R.id.pass_edt_text);
        btnLogin = findViewById(R.id.login_btn);
        btnSignup = findViewById(R.id.signup_btn);
        forget_pass = findViewById(R.id.forget_pass);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        forget_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_email_required), Toast.LENGTH_LONG).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_password_required), Toast.LENGTH_LONG).show();
                } else {
                    // [START sign_in_with_email]
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(AccountActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    } else {
                                        startActivity(new Intent(AccountActivity.this, MainActivity.class));
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        finish();
                                    }
                                }
                            });
                }
                // [END sign_in_with_email]
            }
        });
    }

    private void sendEmailVerification() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Email sent
                    }
                });
        // [END send_email_verification]
    }


    private void updatePassword() {
        setContentView(R.layout.activity_changepass);

        TextView old_pass_edt_text, inputPassword, inputPasswordConfirm, btnCancel;
        Button btnConfirm;

        old_pass_edt_text = findViewById(R.id.old_pass_edt_text);
        inputPassword = findViewById(R.id.pass_edt_text);
        inputPasswordConfirm = findViewById(R.id.pass_edt_text_confirm);
        btnConfirm = findViewById(R.id.confirm_btn);
        btnCancel = findViewById(R.id.cancel_btn);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String oldPassword = old_pass_edt_text.getText().toString();
                final String newPassword = inputPassword.getText().toString();
                final String passwordConfirm = inputPasswordConfirm.getText().toString();
                if (oldPassword.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_old_password_required), Toast.LENGTH_LONG).show();
                } else if (newPassword.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_new_password_required), Toast.LENGTH_LONG).show();
                } else if (passwordConfirm.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_confirm_password_required), Toast.LENGTH_LONG).show();
                } else if (newPassword.length() < 6) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.minimum_password), Toast.LENGTH_LONG).show();
                } else if (!newPassword.equals(passwordConfirm)) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_match_password), Toast.LENGTH_LONG).show();
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
                    currentUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        currentUser.updatePassword(newPassword)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "User password updated.");
                                                            Toast.makeText(AccountActivity.this, getResources().getString(R.string.change_password_successfully), Toast.LENGTH_LONG).show();
                                                            finish();
                                                        } else {
                                                            Toast.makeText(AccountActivity.this, getResources().getString(R.string.change_password_failed), Toast.LENGTH_LONG).show();
                                                            Log.d(TAG, "Error password not updated ");
                                                        }
                                                    }
                                                });
                                        Log.d(TAG, "User re-authenticated.");
                                    } else {
                                        Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_incorrect_old_password), Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "User re-authenticated failed.");
                                    }
                                }
                            });
                }
            }
        });

    }

    private void forgetPassword() {
        setContentView(R.layout.activity_forgetpass);
        TextView inputEmail, btnLogin;
        Button resetpass_btn;

        inputEmail = findViewById(R.id.email_edt_text);
        btnLogin = findViewById(R.id.login_btn);
        resetpass_btn = findViewById(R.id.resetpass_btn);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        resetpass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_email_required), Toast.LENGTH_LONG).show();
                } else {
                    // [START sign_in_with_email]
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, getResources().getString(R.string.reset_password_failed), Toast.LENGTH_LONG).show();
                                        Log.w(TAG, "resetWithEmail:failure", task.getException());
                                    } else {
                                        Toast.makeText(AccountActivity.this, getResources().getString(R.string.send_password_reset_email), Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "resetWithEmail:success");
                                        login();
                                    }
                                }
                            });
                }
                // [END sign_in_with_email]
            }
        });
    }
}