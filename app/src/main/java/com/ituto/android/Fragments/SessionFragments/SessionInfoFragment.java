package com.ituto.android.Fragments.SessionFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.ituto.android.Adapters.AssessmentsAdapter;
import com.ituto.android.Adapters.TutorsAdapter;
import com.ituto.android.Constant;
import com.ituto.android.ConversationActivity;
import com.ituto.android.Fragments.AssessmentAnswerFragment;
import com.ituto.android.Fragments.AssessmentCreateFragment;
import com.ituto.android.HomeActivity;
import com.ituto.android.Models.Assessment;
import com.ituto.android.R;
import com.muddzdev.styleabletoast.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class SessionInfoFragment extends Fragment implements AssessmentsAdapter.OnItemListener {

    private View view;
    private ImageView imgBackButton;
    private MaterialCardView crdTutee, crdDescription;
    private TextView txtSubjectName, txtTime, txtName, txtCourse, txtYearLevel, txtDescription, txtTutor, txtTutee;
    private RecyclerView recyclerAssessments;
    private ImageButton btnAddAssessment;
    private MaterialButton btnReviewTutor, btnSessionDone;
    private Dialog dialog;

    private AssessmentsAdapter assessmentsAdapter;
    private ArrayList<Assessment> assessmentArrayList;
    private SharedPreferences sharedPreferences;
    private String loggedInAs;

    private RatingBar rtbTutorRating;
    private TextInputEditText txtComment;
    private MaterialButton btnCancel;
    private MaterialButton btnSubmit;

    private String sessionID, tuteeID, tutorID, subjectID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_session_info, container, false);
        init();
        return view;
    }

    private void init() {
        sharedPreferences = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        BottomAppBar bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        bottomAppBar.setVisibility(View.GONE);
        sessionID = getArguments().getString("_id");

        txtSubjectName = view.findViewById(R.id.txtSubjectName);
        txtTime = view.findViewById(R.id.txtTime);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtTutor = view.findViewById(R.id.txtTutor);
        txtTutee = view.findViewById(R.id.txtTutee);
        recyclerAssessments = view.findViewById(R.id.recyclerAssessments);
        recyclerAssessments.setLayoutManager(new LinearLayoutManager(getContext()));
        btnAddAssessment = view.findViewById(R.id.btnAddAssessment);
        imgBackButton = view.findViewById(R.id.imgBackButton);
        btnSessionDone = view.findViewById(R.id.btnSessionDone);
        btnReviewTutor = view.findViewById(R.id.btnReviewTutor);

        dialog = new Dialog(getContext(), R.style.DialogTheme);
        dialog.getWindow().getAttributes().windowAnimations = R.style.SplashScreenDialogAnimation;
        dialog.setContentView(R.layout.layout_dialog_progress);
        dialog.setCancelable(false);
        dialog.show();

        sharedPreferences = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        loggedInAs = sharedPreferences.getString("loggedInAs", "");

        imgBackButton.setOnClickListener(view -> getActivity().getSupportFragmentManager().popBackStack());

        if (loggedInAs.equals("TUTOR")) {
            btnAddAssessment.setVisibility(View.VISIBLE);
            btnReviewTutor.setVisibility(View.GONE);
            btnSessionDone.setVisibility(View.VISIBLE);
        } else {
            btnAddAssessment.setVisibility(View.GONE);
            btnReviewTutor.setVisibility(View.VISIBLE);
            btnSessionDone.setVisibility(View.GONE);
        }

        getSession();

        btnSessionDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog doneDialog = new Dialog(getContext());
                doneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                doneDialog.setContentView(R.layout.layout_dialog_done);

                Button btnYes = doneDialog.findViewById(R.id.btnYes);
                Button btnNo = doneDialog.findViewById(R.id.btnNo);

                doneDialog.show();

                btnYes.setOnClickListener(v -> {
                    doneDialog.dismiss();
                    doneSession();
                });

                btnNo.setOnClickListener(v -> doneDialog.cancel());
            }
        });

        btnAddAssessment.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            AssessmentCreateFragment assessmentCreateFragment = new AssessmentCreateFragment();
            bundle.putString("sessionID", sessionID);
            bundle.putString("subjectID", subjectID);
            bundle.putString("tuteeID", tuteeID);
            assessmentCreateFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.fragment_container, assessmentCreateFragment).addToBackStack(null).commit();
        });

        btnReviewTutor.setOnClickListener(view -> {
            Dialog reviewDialog = new Dialog(getContext());

            reviewDialog.setContentView(R.layout.layout_dialog_review);
            reviewDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            reviewDialog.getWindow().getAttributes().windowAnimations = R.style.AddQuestionDialogAnimation;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            reviewDialog.getWindow().setLayout((6 * width) / 7, reviewDialog.getWindow().getAttributes().height);

            rtbTutorRating = reviewDialog.findViewById(R.id.rtbTutorRating);
            txtComment = reviewDialog.findViewById(R.id.txtComment);
            btnCancel = reviewDialog.findViewById(R.id.btnCancel);
            btnSubmit = reviewDialog.findViewById(R.id.btnSubmit);

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateReview()) {
                        submitReview();
                        reviewDialog.dismiss();
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reviewDialog.cancel();
                }
            });

            reviewDialog.show();
        });

    }

    private void getSession() {
        assessmentArrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.GET_SESSION + "/" + sessionID, response -> {
            try {
                JSONObject object = new JSONObject(response);

                if (object.getBoolean("success")) {
                    JSONObject sessionObject = object.getJSONObject("session");
                    JSONObject tutorObject = sessionObject.getJSONObject("tutor");
                    JSONObject tuteeObject = sessionObject.getJSONObject("tutee");
                    JSONObject courseObject = tuteeObject.getJSONObject("course");
                    JSONObject avatarObject = tuteeObject.getJSONObject("avatar");
                    JSONObject subjectObject = sessionObject.getJSONObject("subject");
                    JSONObject timeObject = sessionObject.getJSONObject("time");
                    JSONArray assessments = sessionObject.getJSONArray("assessments");

                    if (sessionObject.getString("status").equals("Done")) {
                        btnAddAssessment.setVisibility(View.GONE);
                        btnSessionDone.setVisibility(View.GONE);
                        btnReviewTutor.setVisibility(View.GONE);
                    }


                    JSONObject availabilityObject = object.getJSONObject("availability");
                    JSONArray days = availabilityObject.getJSONArray("days");
                    JSONArray time = availabilityObject.getJSONArray("time");

                    tutorID = tutorObject.getString("_id");
                    tuteeID = tuteeObject.getString("_id");
                    subjectID = subjectObject.getString("_id");

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Date date = format.parse(sessionObject.getString("startDate"));
                    String outputPattern = "yyyy-MM-dd";
                    SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

                    txtSubjectName.setText(subjectObject.getString("name"));
                    txtTime.setText(timeObject.getString("min") + " - " + timeObject.getString("max"));
                    txtDescription.setText(sessionObject.getString("description"));
                    txtTutor.setText(tutorObject.getString("firstname") + " " + tutorObject.getString("lastname"));
                    txtTutee.setText(tuteeObject.getString("firstname") + " " + tuteeObject.getString("lastname"));

                    for (int i = 0; i < assessments.length(); i++) {
                        JSONObject assessmentObject = assessments.getJSONObject(i);
                        Assessment assessment = new Assessment();

                        assessment.setAssessmentID(assessmentObject.getString("_id"));
                        assessment.setName(assessmentObject.getString("name"));
                        assessment.setScore(assessmentObject.getInt("score"));
                        assessment.setTotalItems(assessmentObject.getJSONArray("questions").length());

                        assessmentArrayList.add(assessment);
                    }

                    assessmentsAdapter = new AssessmentsAdapter(getContext(), assessmentArrayList, this);
                    recyclerAssessments.setAdapter(assessmentsAdapter);
                }

                dialog.dismiss();
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
//                btnConfirmSchedule.setEnabled(false);
                dialog.dismiss();
            }

        }, error -> {
            dialog.dismiss();
//            btnConfirmSchedule.setEnabled(false);
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

    private void submitReview() {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.REVIEW_TUTOR, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    StyleableToast.makeText(getContext(), "Review submitted!", R.style.CustomToast).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            StyleableToast.makeText(getContext(), "Unable to submit review", R.style.CustomToast).show();
            error.printStackTrace();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("sessionID", sessionID);
                map.put("tutorID", tutorID);
                map.put("tutee", tuteeID);
                map.put("subject", subjectID);
                map.put("rating", String.valueOf(rtbTutorRating.getRating()));
                map.put("comment", txtComment.getText().toString().trim());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    private void doneSession() {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.DONE_SESSION + "/" + sessionID, response -> {

            try {
                JSONObject object = new JSONObject(response);

                if (object.getBoolean("success")) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    StyleableToast.makeText(getContext(), "Session Done", R.style.CustomToast).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                StyleableToast.makeText(getContext(), "There was a problem completing the session", R.style.CustomToast).show();
            }

        }, error -> {
            error.printStackTrace();
            error.getMessage();
            StyleableToast.makeText(getContext(), "There was a problem completing the session", R.style.CustomToast).show();
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

    private Boolean validateReview() {

        if (rtbTutorRating.getRating() < 0) {
            StyleableToast.makeText(getContext(), "Please select your rating", R.style.CustomToast).show();
            return false;
        }

        if (txtComment.getText().toString().isEmpty()) {
            StyleableToast.makeText(getContext(), "Please input your comment", R.style.CustomToast).show();
            return false;
        }

        return true;
    }

    private Boolean checkAssessments() {

        for (int i = 0; i < assessmentArrayList.size(); i++) {
//            if (assessmentArrayList.get(i).getScore() ) {
//            }
        }

        return true;
    }

    @Override
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        AssessmentAnswerFragment assessmentAnswerFragment = new AssessmentAnswerFragment();
        bundle.putString("_id", assessmentArrayList.get(position).getAssessmentID());
        assessmentAnswerFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragment_container, assessmentAnswerFragment).addToBackStack(null).commit();
    }


}