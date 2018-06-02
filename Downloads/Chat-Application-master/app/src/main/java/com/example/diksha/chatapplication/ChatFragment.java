package com.example.diksha.chatapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.EmbossMaskFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.Manifest;
import org.json.JSONArray;
import java.util.TimeZone;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseUser;
import java.lang.reflect.Array;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;


public class ChatFragment extends Fragment {

    private Socket mSocket;
    private RecyclerView mMessageView;
    private RecyclerView.Adapter mAdapter;
    private EditText mEditText;
    private TextView mStatus;
    private ProgressBar mLoading;
    private List<Message> mMessages = new ArrayList<Message>();
    private boolean mTyping = false;
    private InfiniteRecyclerViewScrollListener scrollListener;
    public Message mMessage;
    private boolean isOtherUserOnline = false;
    private Handler mMessageHandler = new Handler();
    private  FirebaseUser mCurrentUser;
    private List<User> mUserList = new ArrayList<User>();
    private RecyclerView.Adapter mUserAdapter;
    private  String mToUser;
    private static final int RC_PHOTO_PICKER = 2;
    private static final int REQUEST_PERMISSION = 1;
    private static final int MAX_MESSAGES_PER_REQUEST = 10;
    private static final String TAG = "ChatFragment";

    public ChatFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_DISCONNECT,OnDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, OnConnectError);
        mCurrentUser = app.getCurrentUser();
        mSocket.on("update offline status",UpdateOfflineStatus);
        mSocket.on("new message", OnNewMessage);
        mSocket.on("image", OnNewImage);
        mSocket.on("typing", OnTyping);
        mSocket.on("get messages", OnGetMessages);
        mSocket.on("stop typing", OnStopTyping);
        //mSocket.on("get messages", OnGetMessages);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // mSocket.off(Socket.EVENT_CONNECT, OnConnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, OnConnectError);
        mSocket.off("new message", OnNewMessage);
        mSocket.off("image", OnNewImage);
        mSocket.off("typing", OnTyping);
        mSocket.off("stop typing", OnStopTyping);
    }
    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            onResume();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!getUserVisibleHint())
        {
            return;
        }
        JSONObject data=createBaseJSONObject();
        mSocket.emit("update delivery status",data);
        mSocket.on("online result",UpdateOnlineStatus);
        mSocket.emit("check online presence",mToUser);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessageView = (RecyclerView) view.findViewById(R.id.messages);
        mStatus = (TextView) view.findViewById(R.id.status);
//        mLoading = view.findViewById(R.id.loading);
//        mLoading.setVisibility(View.VISIBLE);

        mAdapter = new MessageAdapter(mMessages, getContext());
        mMessageView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessageView.setAdapter(mAdapter);

        mToUser = getArguments().getString("toUser");

        //Set title bar
        ((MainActivity)getActivity()).setActionBarTitle(mToUser);

//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        mMessageView.setLayoutManager(linearLayoutManager);
//        scrollListener = new InfiniteRecyclerViewScrollListener(linearLayoutManager)
//        {
//            @Override
//            public void onLoadMore(int page, int totalItemCount, RecyclerView view)
//            {
//                loadNextData(page);
//            }
//        };
//        mMessageView.addOnScrollListener(scrollListener);
        mEditText = (EditText) view.findViewById(R.id.message_input);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing", createBaseJSONObject());
                }
                mMessageHandler.removeCallbacks(OnTypingTimeout);
                mMessageHandler.postDelayed(OnTypingTimeout, 500);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mEditText.getText().toString().trim();
                int len = message.length();                if(len!=0) {
                    String time = getCurrentTime();
                    Log.d(TAG, "onClick: " + time);
                    mEditText.setText("");
                    JSONObject messageData = createBaseJSONObject();
                    try {
                        messageData.put("messageText", message);
                        messageData.put("time", time);
                    } catch (JSONException e) {
                    }
                    mSocket.emit("new message", messageData);
                    if( isOtherUserOnline )
                        insertMessage(message, false, time,true);
                    else
                        insertMessage(message, false, time,false);

                }
            }
        });

        ImageButton photoPickerButton = (ImageButton) view.findViewById((R.id.photoPickerButton));
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: first check if user is signed in
        if (requestCode == RC_PHOTO_PICKER) {
            Uri selectedImageUri = data.getData();
            JSONObject sendImage = createBaseJSONObject();
            String time = getCurrentTime();
            try {
                sendImage.put("time", time);
                sendImage.put("image", encodeImage(selectedImageUri));
                mSocket.emit("image", sendImage);
            } catch (JSONException e) {
            }
            insertImage(selectedImageUri, false, time,false);
        } else {
            Log.e(TAG, "onActivityResult: ", new Exception());
        }
    }

    private String encodeImage(Uri image) {
        String encodedImage = null;
        try {
            // uri -> bitmap -> byteArrayOutputStream -> byte array -> String
            Bitmap selectedImagebitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImagebitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] b = byteArrayOutputStream.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        } catch (IOException e) {
            Log.e(TAG, "onActivityResult: ", e);
        }
        return encodedImage;
    }

    private Bitmap decodeImage(String data) {
        byte[] b = Base64.decode(data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    private void insertMessage(String message, Boolean isReceived, String time,Boolean isDelivered) {
        mMessage =  new Message(message, null, isReceived, time,isDelivered);
        mMessages.add(mMessage);
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void insertImage(Uri image, Boolean isReceived, String time,Boolean isDelivered) {
        mMessages.add(new Message(null, image, isReceived, time,isDelivered));
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private Uri saveImage(Bitmap image) {
        File directory = getContext().getExternalFilesDir("my Images");
        String id = UUID.randomUUID().toString();
        File path = new File(directory, id + ".jpg");
        Log.d(TAG, "saveImage: " + path);
        try {
            FileOutputStream out = new FileOutputStream(path);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
//             MediaStore.Images.Media.insertImage(getContext().getContentResolver(),path.getAbsolutePath(),file.getName(),file.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);
            } else {
                Log.i(TAG, "saveImage: " + getContext().getExternalFilesDir(null));
//                MediaStore.Images.Media.insertImage(getContext().getContentResolver(), image, "image", "diksha");
//                MediaStore.Images.Media.insertImage(getContext().getContentResolver(), path.getAbsolutePath(), path.getName(), path.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", path);
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }
    }

    private void scrollToBottom() {
        mMessageView.scrollToPosition(mMessages.size() - 1);
    }

    public void vibrate() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);
    }

    public void playBeep() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject createBaseJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("person1", mToUser);
            jsonObject.put("person2", mCurrentUser.getPhoneNumber());

        }catch (JSONException e){ }
        return jsonObject;
    }

    public String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String messageCreatedAt = dateFormat.format(date);
        return messageCreatedAt;
    }
    public void loadNextData(int offset) {
//        mLoading.setVisibility(View.VISIBLE);
        JSONObject data = createBaseJSONObject();
        try {
            data.put("offset", offset);
            data.put("maxMessages", MAX_MESSAGES_PER_REQUEST);
        } catch (JSONException e) {
        }
        mSocket.emit("get messages", data);
        Log.d(TAG, "run: " + offset);

    }

    /* Listeners */
    private Emitter.Listener OnConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Connection error: " + args[0], new Exception());

                }
            });
        }
    };
    private Emitter.Listener OnDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSocket.emit("disconnect",mCurrentUser.getPhoneNumber());
                }
            });
        }
    };
    private Emitter.Listener UpdateOnlineStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null){
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray result= (JSONArray) args[0];
                    try {
                        JSONObject json = (JSONObject) result.get(0);
                        String onlineCheck = json.optString("result");
                        String online = "true";
                        if (onlineCheck.equals(online)){
                            isOtherUserOnline=true;
                            ((MainActivity)getActivity()).setActionBarSubTitle("online");
                        }
                        else
                        {
                            isOtherUserOnline=false;
                        }
                    }
                    catch (JSONException e) {}
                }
            });
        }
    };
    private Emitter.Listener OnNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO: insert message in the room where username is not equal to the person who emitted the message
                    JSONObject message = (JSONObject) args[0];
                    try{
                        String messageText = message.getString("messageText");
                        String time = message.getString("time");
                        String person = message.getString("person1");
                        Log.d(TAG, "run: " + messageText);
                            if (person.equals(mCurrentUser.getPhoneNumber())) {
                                insertMessage(messageText, true, time,false);
                            }
                            else
                            {
                                //insertMessage(messageText, true, time,false);
                                if( isOtherUserOnline )
                                    insertMessage(messageText, false, time,true);
                                else
                                    insertMessage(messageText, false, time,false);
                            }

                    }catch (JSONException e){ }
                    //playBeep();
                    //vibrate();
                }
            });
        }
    };
    private Emitter.Listener OnNewImage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String selectedImage;
                    JSONObject image = (JSONObject) args[0];
                    try {
                        selectedImage = image.getString("image");
                        Bitmap bitmap = decodeImage(selectedImage);
                        Uri uri = saveImage(bitmap);
                        String time = image.getString("time");
                        String person = image.getString("person1");
                        //Log.d(TAG, "run: " + person + " " + mCurrentUser.getPhoneNumber());
                        if(person.equals(mCurrentUser.getPhoneNumber())){
                            insertImage(uri, true, time,false);
                        }
                        else {
                            insertImage(uri, false, time,true);
                        }
                    } catch (JSONException e) {
                    }
                }
            });
        }
    };

    private Emitter.Listener UpdateOfflineStatus = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity)getActivity()).setActionBarSubTitle(null);
                }
            });
        }
    };
    private Emitter.Listener OnTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO:show typing only to the other user in ther room where username is not equal to the person who emitted the message
                    ((MainActivity)getActivity()).setActionBarSubTitle("typing");
                }
            });
        }
    };
    private Emitter.Listener OnStopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isOtherUserOnline)
                    ((MainActivity)getActivity()).setActionBarSubTitle("online");
                    else
                        ((MainActivity)getActivity()).setActionBarSubTitle(null);
                }
            });
        }
    };
    private Emitter.Listener OnGetMessages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null){
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray data = (JSONArray) args[0];
                    Log.d(TAG, "length: " + data.length());
                    if (data.length() == 0) {
                        mLoading.setVisibility(View.GONE);
                        mMessageView.removeOnScrollListener(scrollListener);
                    }
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject message = (JSONObject) data.get(i);
                            String time = message.getString("time");
                            time = time.replaceAll("[TZ]", " ");
                            String person = message.getString("toId");
                            String messageText = message.getString("messageText");
                            if (messageText.equals("null")) {
                                String selectedImage = message.getString("image");
                                Log.d(TAG, "run: image" + selectedImage);
                                Bitmap bitmap = decodeImage(selectedImage);
                                Uri uri = saveImage(bitmap);
                                if (person.equals(mCurrentUser.getPhoneNumber())) {
                                    mMessages.add(0, new Message(null, uri, true, time,false));
                                } else {
                                    mMessages.add(0, new Message(null, uri, false, time,false));
                                }
                            }
                            else {
                                if (person.equals(mCurrentUser.getPhoneNumber())) {
                                    mMessages.add(0, new Message(messageText, null, true, time,false));
                                } else {
                                    mMessages.add(0, new Message(messageText, null, false, time,false));
                                }

                            }
                            mLoading.setVisibility(View.GONE);
                            mAdapter.notifyItemInserted(0);
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "onGetMessage: could not parse " + e);
                    }
                    //when data is loaded for the first time
                    if (data.length() > 1) {
                        scrollToBottom();
                    }
                }
            });
        }
    };

    private Runnable OnTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing",createBaseJSONObject());
        }
    };


}