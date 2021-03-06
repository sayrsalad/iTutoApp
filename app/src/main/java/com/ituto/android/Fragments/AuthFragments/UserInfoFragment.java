package com.ituto.android.Fragments.AuthFragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ituto.android.AuthActivity;
import com.ituto.android.Constant;
import com.ituto.android.HomeActivity;
import com.ituto.android.Models.Course;
import com.ituto.android.R;
import com.muddzdev.styleabletoast.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserInfoFragment extends Fragment {

    private View view;

    private SharedPreferences sharedPreferences;

    private TextInputLayout layoutFirstName, layoutLastName, layoutBirthdate, layoutGender, layoutCourse, layoutYearLevel;
    private TextInputEditText txtFirstName, txtLastName, txtBirthdate;
    private AutoCompleteTextView txtGender;
    private AutoCompleteTextView txtCourse;
    private AutoCompleteTextView txtYearLevel;
    private Button btnSignUp;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Boolean isTutor;
    private String courseID;

    private String email, password, confirmPassword;

    private ArrayList<Course> courseArrayList;
    private ArrayList<String> stringCourseArrayList;

    private ProgressDialog dialog;

    private static final String[] YEAR = new String[]{
            "First", "Second", "Third", "Fourth"
    };

    private static final String[] GENDERS = new String[]{
            "Male", "Female", "Prefer not to say"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_info, container, false);
        init();
        return view;
    }

    private void init() {
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        isTutor = getArguments().getBoolean("isTutor");

        email = getArguments().getString("email");
        password = getArguments().getString("password");
        confirmPassword = getArguments().getString("password_confirmation");

        layoutFirstName = view.findViewById(R.id.txtLayoutFirstNameSignUp);
        layoutLastName = view.findViewById(R.id.txtLayoutLastNameSignUp);
        layoutBirthdate = view.findViewById(R.id.txtLayoutBirthdateSignUp);
        layoutGender = view.findViewById(R.id.txtLayoutGenderSignUp);
        layoutCourse = view.findViewById(R.id.txtLayoutCourseSignUp);
        layoutYearLevel = view.findViewById(R.id.txtLayoutYearLevelSignUp);

        txtFirstName = view.findViewById(R.id.txtFirstNameSignUp);
        txtLastName = view.findViewById(R.id.txtLastNameSignUp);
        txtBirthdate = view.findViewById(R.id.txtBirthdateSignUp);
        txtGender = view.findViewById(R.id.txtGenderSignUp);
        txtCourse = view.findViewById(R.id.txtCourseSignUp);
        txtYearLevel = view.findViewById(R.id.txtYearLevelSignUp);

        btnSignUp = view.findViewById(R.id.btnSignUp);

        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);

        getCourses();

        if (isTutor) {
            btnSignUp.setText("Continue");
        } else {
            btnSignUp.setText("Register");
        }

        txtFirstName.setText(getArguments().getString("firstname"));
        txtLastName.setText(getArguments().getString("lastname"));

        txtBirthdate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        monthOfYear = monthOfYear + 1;
                        String date = year1 + "-" + monthOfYear + "-" + dayOfMonth;
                        txtBirthdate.setText(date);
                    },
                    year,
                    month,
                    day);
            datePickerDialog.setAccentColor(getResources().getColor(R.color.colorPrimaryLight));
            datePickerDialog.setMaxDate(Calendar.getInstance());
            datePickerDialog.show(getParentFragmentManager(), "");
        });

        dateSetListener = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String date = year + "-" + month + "-" + dayOfMonth;
            txtBirthdate.setText(date);
        };

        ArrayAdapter<String> genderArrayAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_dropdown,
                R.id.txtDropdownItem,
                GENDERS
        );

        txtGender.setAdapter(genderArrayAdapter);

        ArrayAdapter<String> yearArrayAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_dropdown,
                R.id.txtDropdownItem,
                YEAR
        );

        txtYearLevel.setAdapter(yearArrayAdapter);


        txtCourse.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            courseID = courseArrayList.get(stringCourseArrayList.indexOf(selected)).getId();
        });

        txtFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtFirstName.getText().toString().isEmpty()) {
                    layoutFirstName.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtLastName.getText().toString().isEmpty()) {
                    layoutLastName.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtBirthdate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtBirthdate.getText().toString().isEmpty()) {
                    layoutBirthdate.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtGender.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtGender.getText().toString().isEmpty()) {
                    layoutGender.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtCourse.getText().toString().isEmpty()) {
                    layoutCourse.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtYearLevel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtYearLevel.getText().toString().isEmpty()) {
                    layoutYearLevel.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnSignUp.setOnClickListener(v -> {
            if (validate()) {
                redirectAuthentication();
            }
        });

    }

    private void getCourses() {
        stringCourseArrayList = new ArrayList<>();
        courseArrayList = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.GET, Constant.COURSES, response -> {

            try {
                JSONObject object = new JSONObject(response);

                if (object.getBoolean("success")) {

                    JSONArray coursesArray = new JSONArray(object.getString("courses"));

                    for (int i = 0; i < coursesArray.length(); i++) {
                        JSONObject courseObject = coursesArray.getJSONObject(i);

                        if (courseObject.getBoolean("active")) {
                            Course course = new Course();

                            course.setId(courseObject.getString("_id"));
                            course.setCode(courseObject.getString("code"));
                            course.setName(courseObject.getString("name"));

                            stringCourseArrayList.add(courseObject.getString("name"));
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                    getContext(),
                                    R.layout.item_dropdown,
                                    R.id.txtDropdownItem,
                                    stringCourseArrayList
                            );

                            courseArrayList.add(course);
                            txtCourse.setAdapter(arrayAdapter);
                        }

                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            error.printStackTrace();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    private void register() {
        dialog.setMessage("Registering");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.REGISTER, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    ).replace(R.id.frameAuthContainer, new SignInFragment()).addToBackStack(null).commit();
                    StyleableToast.makeText(getContext(), object.getString("msg"), R.style.CustomToast).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                StyleableToast.makeText(getContext(), "Register Unsuccessful", R.style.CustomToast).show();
            }
            dialog.dismiss();

        }, error -> {
            StyleableToast.makeText(getContext(), "Register Unsuccessful", R.style.CustomToast).show();
            error.printStackTrace();
            dialog.dismiss();
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("firstname", txtFirstName.getText().toString().trim());
                map.put("lastname", txtLastName.getText().toString().trim());
                map.put("birthdate", txtBirthdate.getText().toString().trim());
                map.put("gender", txtGender.getText().toString().trim());
                map.put("course", courseID);
                map.put("yearLevel", txtYearLevel.getText().toString().trim());
                map.put("email", email);
                map.put("password", password);
                map.put("password_confirmation", confirmPassword);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    public void googleRegister() {
        dialog.setMessage("Registering");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.GOOGLE_LOGIN, response -> {

            try {
                JSONObject object = new JSONObject(response);
                Log.d("TAGTAGTAGTAG", object.toString());
                if (object.getBoolean("success")) {
                    JSONObject user = object.getJSONObject("user");
                    SharedPreferences userPref = getActivity().getApplicationContext().getSharedPreferences("user", getContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("token", object.getString("token"));
                    editor.putString("_id", user.getString("_id"));
                    editor.putString("firstname", user.getString("firstname"));
                    editor.putString("lastname", user.getString("lastname"));
                    editor.putString("isTutor", user.getString("isTutor"));
                    editor.putString("loggedInAs", getArguments().getBoolean("isTutor") ? "TUTOR" : "TUTEE");
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();
                    startActivity(new Intent(((AuthActivity) getContext()), HomeActivity.class));
                    ((AuthActivity) getContext()).finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                StyleableToast.makeText(getContext(), "Register Unsuccessful", R.style.CustomToast).show();
            }
            dialog.dismiss();

        }, error -> {
            StyleableToast.makeText(getContext(), "Register Unsuccessful", R.style.CustomToast).show();
            error.printStackTrace();
            dialog.dismiss();
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("tokenId", getArguments().getString("tokenId"));
                map.put("birthdate", txtBirthdate.getText().toString().trim());
                map.put("gender", txtGender.getText().toString().trim());
                map.put("yearLevel", txtYearLevel.getText().toString().trim());
                map.put("course", courseID);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    private boolean validate() {

        if (txtFirstName.getText().toString().isEmpty()) {
            layoutFirstName.setErrorEnabled(true);
            layoutFirstName.setError("Enter your firstname");
            return false;
        }

        if (txtLastName.getText().toString().isEmpty()) {
            layoutLastName.setErrorEnabled(true);
            layoutLastName.setError("Enter your lastname");
            return false;
        }

        if (txtGender.getText().toString().isEmpty()) {
            layoutGender.setErrorEnabled(true);
            layoutGender.setError("Enter your gender");
            return false;
        }

        if (txtBirthdate.getText().toString().isEmpty()) {
            layoutBirthdate.setErrorEnabled(true);
            layoutBirthdate.setError("Enter your birthdate");
            return false;
        }

        if (txtCourse.getText().toString().isEmpty()) {
            layoutCourse.setErrorEnabled(true);
            layoutCourse.setError("Enter your course");
            return false;
        }

        if (txtYearLevel.getText().toString().isEmpty()) {
            layoutYearLevel.setErrorEnabled(true);
            layoutYearLevel.setError("Enter your year level");
            return false;
        }

        return true;
    }

    private void redirectAuthentication() {
        if (isTutor) {
            Bundle args = getArguments();
            SignUpAvailabilityFragment signUpAvailabilityFragment = new SignUpAvailabilityFragment();
            args.putString("firstname", txtFirstName.getText().toString().trim());
            args.putString("lastname", txtLastName.getText().toString().trim());
            args.putString("birthdate", txtBirthdate.getText().toString().trim());
            args.putString("gender", txtGender.getText().toString().trim());
            args.putString("course", courseID);
            args.putString("yearLevel", txtYearLevel.getText().toString().trim());
            signUpAvailabilityFragment.setArguments(args);
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.frameAuthContainer, signUpAvailabilityFragment).addToBackStack(null).commit();
        } else {
            if (getArguments().getBoolean("googleRegister")) {
                googleRegister();
            } else {
                register();
            }
        }
    }
}