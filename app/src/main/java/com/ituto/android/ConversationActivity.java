package com.ituto.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ituto.android.Adapters.MessagesAdapter;
import com.ituto.android.Models.Conversation;
import com.ituto.android.Models.Message;
import com.ituto.android.Models.User;
import com.ituto.android.Utils.FilePath;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConversationActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int ALL_FILE_REQUEST = 102;
    private static final int CAMERA_REQUEST = 103;
    private static final int GALLERY_REQUEST = 104;
    private String gallery_file_path, all_file_path, camera_file_path;

    private RecyclerView recyclerConversation;
    private EditText txtEnterMessage;
    private Button btnSend;
    private String conversationID;
    private CircleImageView imgYouHeader;
    private TextView txtConversationName;
    private ImageButton btnCamera, btnImage, btnAttachment;

    private ArrayList<Message> messageArrayList;
    private MessagesAdapter messagesAdapter;
    private User signedUser;

    private Socket socket;

    private int method = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        init();
    }

    private void init() {
        sharedPreferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        imgYouHeader = findViewById(R.id.imgConversationAvatar);
        txtConversationName = findViewById(R.id.txtConversationName);
        txtEnterMessage = findViewById(R.id.txtEnterMessage);
        btnCamera = findViewById(R.id.btnCamera);
        btnImage = findViewById(R.id.btnImage);
        btnAttachment = findViewById(R.id.btnAttachment);
        btnSend = findViewById(R.id.btnSend);
        recyclerConversation = findViewById(R.id.recyclerConversation);
        recyclerConversation.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        conversationID = getIntent().getStringExtra("conversationID");
        Picasso.get().load(getIntent().getStringExtra("avatar")).fit().centerCrop().into(imgYouHeader);
        txtConversationName.setText(getIntent().getStringExtra("name"));

        try {
            socket = IO.socket("http://192.168.1.2:8080");

            socket.connect();

            socket.emit("connection", conversationID);
            socket.emit("join", conversationID);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        btnAttachment.setOnClickListener(v -> {

        });

        btnSend.setOnClickListener(v -> {
            sendMessage();
            if (gallery_file_path == null) {
                Toast.makeText(ConversationActivity.this, "Gallery File Empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (all_file_path == null) {
                Toast.makeText(ConversationActivity.this, "ALl File File Empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (camera_file_path == null) {
                Toast.makeText(ConversationActivity.this, "CAmera File Empty", Toast.LENGTH_SHORT).show();
                return;
            }
            UploadTask uploadTask = new UploadTask();
            uploadTask.execute(new String[]{gallery_file_path, camera_file_path, all_file_path});
        });

        socket.on("received", args -> {
            Log.d("TAGTAGTAGTAGTAGTAG", args.toString());
            runOnUiThread(() -> {
                JSONObject messageObject = (JSONObject) args[0];
                try {
                    JSONObject senderObject = messageObject.getJSONObject("sender");
                    JSONObject conversationObject = messageObject.getJSONObject("conversationID");

                    Message newMessage = new Message();
                    User sender = new User();
                    Conversation conversation = new Conversation();
                    JSONObject avatar = senderObject.getJSONObject("avatar");

                    sender.setUserID(senderObject.getString("_id"));
                    sender.setFirstname(senderObject.getString("firstname"));
                    sender.setAvatar(avatar.getString("url"));

                    JSONArray userArray = conversationObject.getJSONArray("users");
                    conversation.setConversationID(conversationObject.getString("_id"));
                    conversation.setConversationName(conversationObject.getString("conversationName"));
                    ArrayList<String> userIDArrayList = new ArrayList<String>();
                    for (int a = 0; a < userArray.length(); a++) {
                        String userID = userArray.getString(a);
                        userIDArrayList.add(userID);
                    }
                    conversation.setUserIDArrayList(userIDArrayList);

                    newMessage.setUser(sender);
                    newMessage.setContent(messageObject.getString("content"));
                    newMessage.setConversation(conversation);

//                    recyclerConversation.getAdapter().notifyItemInserted(0);
                    messageArrayList.add(newMessage);

                    recyclerConversation.getAdapter().notifyDataSetChanged();

                    recyclerConversation.smoothScrollToPosition(recyclerConversation.getAdapter().getItemCount());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });

        getMessages();

        getSignedUser();

    }

    private void getMessages() {
        messageArrayList = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.MESSAGES + "/" + conversationID, response -> {
            try {
                JSONObject object = new JSONObject(response);

                if (object.getBoolean("success")) {

                    JSONArray resultArray = new JSONArray(object.getString("messages"));

                    for (int i = 0; i < resultArray.length(); i++) {

                        JSONObject messageObject = resultArray.getJSONObject(i);
                        JSONObject userObject = messageObject.getJSONObject("sender");
                        JSONObject avatar = userObject.getJSONObject("avatar");

                        Message message = new Message();
                        message.setMessageID(messageObject.getString("_id"));
                        message.setContent(messageObject.getString("content"));

                        User user = new User();
                        user.setUserID(userObject.getString("_id"));
                        user.setFirstname(userObject.getString("firstname"));
                        user.setAvatar(avatar.getString("url"));
                        message.setUser(user);

                        messageArrayList.add(message);
                    }

                    messagesAdapter = new MessagesAdapter(getApplicationContext(), messageArrayList, signedUser.getUserID());
                    recyclerConversation.setAdapter(messagesAdapter);
                    recyclerConversation.smoothScrollToPosition(messagesAdapter.getItemCount());
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
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

//        User user = messageArrayList.get(1).getUser();
//        Picasso.get().load(user.getAvatar()).fit().centerCrop().into(imgYouHeader);
    }

    private void sendMessage() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.SEND_MESSAGE, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    socket.emit("new message", response);
                    txtEnterMessage.setText("");
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

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("content", txtEnterMessage.getText().toString());
                map.put("conversationID", conversationID);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }

    private void getSignedUser() {
        signedUser = new User();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.USER_PROFILE, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONObject userObject = object.getJSONObject("user");
                    signedUser.setUserID(userObject.getString("_id"));
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
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void filePicker(int i) {
        if (i == 0) {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Choose File to Upload"), ALL_FILE_REQUEST);
        }

        if (i == 1) {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
        if (i == 2) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(intent, GALLERY_REQUEST);
        }

    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(ConversationActivity.this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ConversationActivity.this, permission)) {
            Toast.makeText(ConversationActivity.this, "Please Allow Permission", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(ConversationActivity.this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ConversationActivity.this, "Permission Successfull", Toast.LENGTH_SHORT).show();
                    filePicker(method);
                } else {
                    Toast.makeText(ConversationActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                if (data == null) {
                    return;
                }

                Uri uri = data.getData();
                String selectedPath = FilePath.getFilePath(ConversationActivity.this, uri);
                Log.d("File Path ", " " + selectedPath);
                if (selectedPath != null) {
                    gallery_file_name.setText("" + new File(selectedPath).getName());
                }
                Bitmap bitmap = BitmapFactory.decodeFile(selectedPath);
                gallery_preview.setImageBitmap(bitmap);
                gallery_file_path = selectedPath;
            }
            if (requestCode == CAMERA_REQUEST) {
                if (data == null) {
                    return;
                }

                //in camera request i will save my file to temp location

                Bitmap thumb = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(), "temp.jpg");

                if (destination.exists()) {
                    destination.delete();
                }

                FileOutputStream out;

                try {
                    out = new FileOutputStream(destination);
                    out.write(bytes.toByteArray());
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("File Path ", " " + destination.getPath());
                if (destination != null) {
                    camera_file_name.setText("" + destination.getName());
                }
                camera_preview.setImageBitmap(thumb);
                camer_file_path = destination.getPath();

            }

            if (requestCode == ALL_FILE_REQUEST) {
                if (data == null) {
                    return;
                }

                Uri uri = data.getData();
                String paths = FilePath.getFilePath(ConversationActivity.this, uri);
                Log.d("File Path : ", "" + paths);
                if (paths != null) {
                    all_file_name.setText("" + new File(paths).getName());
                }
                all_file_path = paths;
            }
        }
    }

    public class UploadTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s!=null){
                Toast.makeText(ConversationActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ConversationActivity.this, "File Upload Failed", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... strings) {

            File file1 = new File(strings[0]);
            File file2 = new File(strings[1]);
            File file3 = new File(strings[2]);

            try {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("files1", file1.getName(), RequestBody.create(MediaType.parse("*/*"), file1))
                        .addFormDataPart("files2", file2.getName(), RequestBody.create(MediaType.parse("*/*"), file2))
                        .addFormDataPart("files3", file3.getName(), RequestBody.create(MediaType.parse("*/*"), file3))
                        .addFormDataPart("some_key", "some_value")
                        .addFormDataPart("submit", "submit")
                        .build();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://192.168.0.2/project/upload2.php")
                        .post(requestBody)
                        .build();



                OkHttpClient okHttpClient = new OkHttpClient();
                //now progressbar not showing properly let's fixed it
                Response response = okHttpClient.newCall(request).execute();
                if (response != null && response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}