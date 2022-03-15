package com.ituto.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ituto.android.Fragments.MainFragments.AccountFragment;
import com.ituto.android.Fragments.MainFragments.ContactsFragment;
import com.ituto.android.Fragments.MainFragments.HomeFragment;
import com.ituto.android.Fragments.MainFragments.SessionsFragment;
import com.ituto.android.Fragments.MainFragments.TutorsFragment;

import io.socket.client.Socket;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigation;
    private static final int GALLERY_ADD_POST = 2;

    public Socket socket;
    private Boolean fromBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        Toolbar toolbar = findViewById(R.id.toolbar);

//        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
        init();
    }

    private void init() {

        bottomNavigation = findViewById(R.id.bottomNavigationView);
        bottomNavigation.setOnNavigationItemSelectedListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            fromBack = true;
            if (current instanceof HomeFragment)
                bottomNavigation.setSelectedItemId(R.id.item_home);
            else if (current instanceof TutorsFragment)
                bottomNavigation.setSelectedItemId(R.id.item_tutors);
            else if (current instanceof SessionsFragment)
                bottomNavigation.setSelectedItemId(R.id.item_tasks);
            else if (current instanceof ContactsFragment)
                bottomNavigation.setSelectedItemId(R.id.item_messages);
            else if (current instanceof AccountFragment)
                bottomNavigation.setSelectedItemId(R.id.item_account);
        });

//        getUser();
    }

//    private void getUser() {
//        StringRequest request = new StringRequest(Request.Method.GET, Constant.USER_PROFILE, response -> {
//
//            try {
//                JSONObject object = new JSONObject(response);
//                if (object.getBoolean("success")) {
//                    JSONObject user = object.getJSONObject("user");
//
//                    navUserName.setText(user.getString("name"));
//                    navUserEmail.setText(user.getString("email"));
////                    Picasso.get().load(Constant.URL+"storage/profiles/" + user.getString("photo")).fit().centerCrop().into(navUserPhoto);
//
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//        }, error -> {
//            error.printStackTrace();
//        }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                String token = userPref.getString("access_token", "");
//                HashMap<String, String> map = new HashMap<>();
//                map.put("Authorization", "Bearer" + token);
//                return map;
//            }
//        };
//        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        queue.add(request);
//    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
//        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);
//
//        if (currentFragment instanceof HomeFragment && id != R.id.item_home)
//            bottomNavigation.setSelectedItemId(R.id.item_home);
//        else if (currentFragment instanceof TutorsFragment && id != R.id.item_tutors)
//            bottomNavigation.setSelectedItemId(R.id.item_tutors);
//        else if (currentFragment instanceof SessionsFragment && id != R.id.item_tasks)
//            bottomNavigation.setSelectedItemId(R.id.item_tasks);
//        else if (currentFragment instanceof ContactsFragment && id != R.id.item_messages)
//            bottomNavigation.setSelectedItemId(R.id.item_messages);
//        else if (currentFragment instanceof AccountFragment && id != R.id.item_account)
//            bottomNavigation.setSelectedItemId(R.id.item_account);


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == bottomNavigation.getSelectedItemId() || fromBack) {
            fromBack = false;
            return true;
        }

        switch (item.getItemId()) {
            case R.id.item_home:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragment_container, new HomeFragment()).addToBackStack(null).commit();
                break;
            case R.id.item_tutors:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragment_container, new TutorsFragment()).addToBackStack(null).commit();
                break;
            case R.id.item_tasks:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragment_container, new SessionsFragment()).addToBackStack(null).commit();
                break;
            case R.id.item_messages:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragment_container, new ContactsFragment()).addToBackStack(null).commit();
                break;
            case R.id.item_account:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragment_container, new AccountFragment()).addToBackStack(null).commit();
                break;
//                dialog = new Dialog(HomeActivity.this);
//                dialog.setContentView(R.layout.custom_alert_dialog);
//
//                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//                Button btnYes = dialog.findViewById(R.id.btnYes);
//                Button btnNo = dialog.findViewById(R.id.btnNo);
//
//                dialog.show();
//
//                btnYes.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        logout();
//                    }
//                });
//
//                btnNo.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.cancel();
//                    }
//                });


        }
        return true;
    }
}