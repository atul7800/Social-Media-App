package com.atulgupta.connect.registration;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atulgupta.connect.MainActivity;
import com.atulgupta.connect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class OTPFragment extends Fragment {


    public OTPFragment() {
        // Required empty public constructor
    }

    public OTPFragment(String email, String phone, String password) {
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    private Timer timer;
    private Integer count = 60;
    private TextView textViewPhone;
    private Button resendotpbutton;
    private EditText otp;
    private ProgressBar progressBar;
    private Button verifybutton;
    private String email, phone, password;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private FirebaseAuth firebaseAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        firebaseAuth = firebaseAuth.getInstance();

        textViewPhone.setText("Verification code has been sent to +91"+phone);

        sendOtp();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
              getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      if (count == 0)
                      {
                          resendotpbutton.setText("Rsend OTP");
                          resendotpbutton.setEnabled(true);
                          resendotpbutton.setAlpha(1f);
                      }else {
                          resendotpbutton.setText("Resend in " + count + " seconds");
                          count--;
                      }
                  }
              });
            }
        },0,1000);

        resendotpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendOtp();

                resendotpbutton.setEnabled(false);
                resendotpbutton.setAlpha(0.3f);
                count = 60;
            }
        });

        verifybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otp.getText() == null || otp.getText().toString().isEmpty())
                {
                    return;
                }

                otp.setError(null);
                String code = otp.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

    }

    private void init(View view)
    {
        otp = view.findViewById(R.id.otp);
        verifybutton = view.findViewById(R.id.verifybutton);
        progressBar = view.findViewById(R.id.progressbar);
        textViewPhone = view.findViewById(R.id.textViewPhone);
        resendotpbutton = view.findViewById(R.id.resendotpbutton);

    }

    private void sendOtp()
    {
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
//                        Log.d(TAG, "onVerificationCompleted:" + credential);
//
//                        signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
//                        Log.w(TAG, "onVerificationFailed", e);

                //Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    otp.setError(e.getMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    otp.setError(e.getMessage());
                }

                progressBar.setVisibility(View.INVISIBLE);

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
//                        Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phone,        // Phone number to verify
                60,                 // Timeout duration 60 seconds
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallback);        // OnVerificationStateChangedCallbacks
    }

    private void resendOtp()
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phone,        // Phone number to verify
                60,                 // Timeout duration 60 seconds
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallback, mResendToken);        // OnVerificationStateChangedCallbacks
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                            user.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Intent mainIntent = new Intent(getContext(), MainActivity.class);
                                        startActivity(mainIntent);
                                        getActivity().finish();
                                    }else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(getContext(),error,Toast.LENGTH_SHORT);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                otp.setError("Invalid OTP");
                            }

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();
    }
}
