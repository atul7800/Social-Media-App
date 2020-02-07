package com.atulgupta.connect.registration;


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

import com.atulgupta.connect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateAccountFragment extends Fragment {


    public CreateAccountFragment() {
        // Required empty public constructor
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private EditText email,phone,password,confirmpassword;
    private Button createaccountbutton;
    private ProgressBar progressBar;
    private TextView logintextv;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       init(view);

       firebaseAuth = FirebaseAuth.getInstance();

       logintextv.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               ((RegisterActivity)getActivity()).setFragment(new LoginFragment());
           }
       });

       createaccountbutton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               email.setError(null);
               phone.setError(null);
               password.setError(null);
               confirmpassword.setError(null);

               if(email.getText().toString().isEmpty())
               {
                  email.setError("Required");
                  return;
               }

               if(phone.getText().toString().isEmpty())
               {
                   phone.setError("Required");
                   return;
               }

               if(password.getText().toString().isEmpty())
               {
                   password.setError("Required");
                   return;
               }

               if(confirmpassword.getText().toString().isEmpty())
               {
                   confirmpassword.setError("Required");
                   return;
               }

               if(!VALID_EMAIL_ADDRESS_REGEX .matcher(email.getText().toString()).find())
               {
                   email.setError("Please enter a valid email");
                   return;
               }

               if(phone.getText().toString().length() != 10)
               {
                   phone.setError("Please enter a valid phone number");
                   return;
               }

               if(!password.getText().toString().equals(confirmpassword.getText().toString()))
               {
                   confirmpassword.setError("Password mismatched");
                   return;
               }

                createAccount();
           }
       });
    }

    private void init(View view)
    {
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phonenumber);
        password = view.findViewById(R.id.password);
        confirmpassword = view.findViewById(R.id.confirmpassword);
        createaccountbutton = view.findViewById(R.id.createbutton);
        progressBar = view.findViewById(R.id.progressbar);
        logintextv = view.findViewById(R.id.logintext);
    }

    private void createAccount()
    {
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.fetchSignInMethodsForEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
              if (task.isSuccessful())
              {
                if (task.getResult().getSignInMethods().isEmpty())
                {
                    ((RegisterActivity)getActivity()).setFragment(new OTPFragment(email.getText().toString(), phone.getText().toString(), password.getText().toString()));
                }else{
                    email.setError("Email already used");
                    progressBar.setVisibility(View.INVISIBLE);
                }
              }else{
                  String error = task.getException().getMessage();
                  Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
              }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
