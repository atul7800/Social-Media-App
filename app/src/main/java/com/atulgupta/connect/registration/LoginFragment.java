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

import com.atulgupta.connect.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {


    public LoginFragment() {
        // Required empty public constructor
    }

    private EditText emailorphone, password;
    private  Button loginbtn;
    private  ProgressBar progressbar;
    private TextView createAccountTv, forgotPassword;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegisterActivity)getActivity()).setFragment(new ForgotPasswordFragment());
            }
        });

        createAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegisterActivity)getActivity()).setFragment(new CreateAccountFragment());
            }
        });

    }

    private void init(View view)
    {
        emailorphone = view.findViewById(R.id.email_or_phone);
        password = view.findViewById(R.id.password);
        loginbtn = view.findViewById(R.id.loginbutton);
        progressbar = view.findViewById(R.id.progressbar);
        createAccountTv = view.findViewById(R.id.createaccounttext);
        forgotPassword = view.findViewById(R.id.forgotpassword);
    }


}
