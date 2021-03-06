package com.ituto.android.Fragments.AuthFragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.ituto.android.R;

@SuppressWarnings("ALL")
public class MainAuthFragment extends Fragment {
    private View view;
    private TextView txtSignIn;
    private Button btnFindTutor, btnBecomeTutor;
    private ProgressDialog dialog;

    public MainAuthFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_auth, container, false);
        init();
        return view;
    }

    private void init() {
        txtSignIn = view.findViewById(R.id.txtSignIn);
        btnFindTutor = view.findViewById(R.id.btnFindTutor);
        btnBecomeTutor = view.findViewById(R.id.btnBecomeTutor);

        txtSignIn.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.frameAuthContainer, new SignInFragment()).addToBackStack(null).commit();
        });

        btnFindTutor.setOnClickListener(v -> {
            Bundle args = new Bundle();
            SignUpFragment signUpFragment = new SignUpFragment();
            args.putBoolean("isTutor", false);
            signUpFragment.setArguments(args);
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.frameAuthContainer, signUpFragment).addToBackStack(null).commit();
        });

        btnBecomeTutor.setOnClickListener(v -> {
            Bundle args = new Bundle();
            SignUpFragment signUpFragment = new SignUpFragment();
            args.putBoolean("isTutor", true);
            signUpFragment.setArguments(args);
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.frameAuthContainer, signUpFragment).addToBackStack(null).commit();
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                Toast.makeText(getActivity().getApplicationContext(), personEmail, Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {

            Log.w("TAG", e.toString());

        }
    }

}
