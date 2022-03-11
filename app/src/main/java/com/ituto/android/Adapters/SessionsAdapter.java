package com.ituto.android.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ituto.android.Models.Session;
import com.ituto.android.Models.Tutor;
import com.ituto.android.Models.User;
import com.ituto.android.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.SessionHolder> {
    private Context context;
    private ArrayList<Session> sessionArrayList;
    private SharedPreferences sharedPreferences;
    private Session session;
    private String loggedInAs;

    public SessionsAdapter(Context context, ArrayList<Session> sessionArrayList) {
        this.context = context;
        this.sessionArrayList = sessionArrayList;
        sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        loggedInAs = sharedPreferences.getString("loggedInAs", "");
    }

    @NonNull
    @Override
    public SessionsAdapter.SessionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_session, parent, false);
        return new SessionsAdapter.SessionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionsAdapter.SessionHolder holder, int position) {
        session = sessionArrayList.get(position);
        Tutor tutor = session.getTutor();
        User user = session.getTutee();

        holder.txtSubjectName.setText(session.getSubject().getName());
        holder.txtTutorTutee.setText(loggedInAs.equals("TUTEE") ? tutor.getFirstname() + " " + tutor.getLastname() : user.getFirstname() + " " + user.getLastname());
    }

    class SessionHolder extends RecyclerView.ViewHolder {

        private TextView txtSubjectName, txtSessionDays, txtSessionTime, txtTutorTutee;
        private ImageView imgMore;

        public SessionHolder(@NonNull View itemView) {
            super(itemView);
            txtSubjectName = itemView.findViewById(R.id.txtSubjectName);
            txtSessionDays = itemView.findViewById(R.id.txtSessionDays);
            txtSessionTime = itemView.findViewById(R.id.txtSessionTime);
            txtTutorTutee = itemView.findViewById(R.id.txtTutorTutee);
            imgMore = itemView.findViewById(R.id.imgMore);

            itemView.setClickable(true);
        }

    }

    @Override
    public int getItemCount() {
        return sessionArrayList.size();
    }
}