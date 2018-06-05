package com.example.diksha.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseUser;


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
import java.util.TimeZone;
import java.util.UUID;


public class ChatFragment extends Fragment {

    private Socket mSocket;
    private RecyclerView mMessageView;
    private RecyclerView.Adapter mAdapter;
    private EditText mEditText;
    private TextView mStatus;
    private ProgressBar mLoading;
    private BottomNavigationView mNavigationView;
    private List<Message> mMessages = new ArrayList<Message>();
    private boolean mTyping = false;
    private Handler mMessageHandler = new Handler();
    private FirebaseUser mCurrentUser;
    private String mToUser;
    private InfiniteRecyclerViewScrollListener scrollListener;
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
        // mSocket.on(Socket.EVENT_CONNECT, OnConnect);

        mCurrentUser = app.getCurrentUser();
        mSocket.on(Socket.EVENT_CONNECT_ERROR, OnConnectError);
        mSocket.on("new message", OnNewMessage);
        mSocket.on("image", OnNewImage);
        mSocket.on("typing", OnTyping);
        mSocket.on("stop typing", OnStopTyping);
        mSocket.on("get messages", OnGetMessages);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessageView = (RecyclerView) view.findViewById(R.id.messages);
        mStatus = (TextView) view.findViewById(R.id.status);
        mLoading = (ProgressBar) view.findViewById(R.id.loading);
        mNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);

        mLoading.setVisibility(View.VISIBLE);
        mNavigationView.setVisibility(View.GONE);

        mAdapter = new MessageAdapter(mMessages, getContext());
        mMessageView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessageView.setAdapter(mAdapter);

        mToUser = getArguments().getString("toUser");

        //Set title bar
        //((MainActivity) getActivity()).setActionBarTitle(mToUser);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mMessageView.setLayoutManager(linearLayoutManager);

        scrollListener = new InfiniteRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount, RecyclerView view) {
                loadNextData(page);
            }
        };

        mMessageView.addOnScrollListener(scrollListener);

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
                String time = getCurrentTime();
                Log.d(TAG, "onClick: " + time);
                insertMessage(message, false, time);
                mEditText.setText("");
                JSONObject messageData = createBaseJSONObject();
                try {
                    messageData.put("messageText", message);
                    messageData.put("time", time);
                } catch (JSONException e) {
                }
                mSocket.emit("new message", messageData);
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

        isBackPressed();
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
            insertImage(selectedImageUri, false, time);
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

    private void insertMessage(String message, Boolean isReceived, String time) {
        mMessages.add(new Message(message, null, isReceived, time));
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void insertImage(Uri image, Boolean isReceived, String time) {
        mMessages.add(new Message(null, image, isReceived, time));
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
        //get the instance of the vibrator
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 400 milliseconds
        v.vibrate(400);
    }

    public void playBeep() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
            r.play();
            //Log.i(TAG,"inside here");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject createBaseJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("person1", mToUser);
            jsonObject.put("person2", mCurrentUser.getPhoneNumber());

        } catch (JSONException e) {
        }
        return jsonObject;
    }

    public String getCurrentTime() {
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

    public void isBackPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    if (getFragmentManager().getBackStackEntryCount() > 0){
                        getFragmentManager().popBackStack();
                    }
                    mNavigationView.setVisibility(View.VISIBLE);
//                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    mSocket.emit("leave room", createBaseJSONObject());
                    return true;
                }
                return false;
            }
        });
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

    private Emitter.Listener OnNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO: insert message in the room where username is not equal to the person who emitted the message
                    JSONObject message = (JSONObject) args[0];
                    try {
                        String messageText = message.getString("messageText");
                        String time = message.getString("time");
                        String person = message.getString("person1");
                        if (person.equals(mCurrentUser.getPhoneNumber())) {
                            insertMessage(messageText, true, time);
                        } else {
                            insertMessage(messageText, false, time);
                        }

                    } catch (JSONException e) {
                    }
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
                        if (person.equals(mCurrentUser.getPhoneNumber())) {
                            insertImage(uri, true, time);
                        } else {
                            insertImage(uri, false, time);
                        }
                    } catch (JSONException e) {
                    }
                }
            });
        }
    };

    private Emitter.Listener OnTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO:show typing only to the other user in ther room where username is not equal to the person who emitted the message
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String toUser = data.getString("person2");
                        Log.i(TAG, "run: " + toUser + " " + mToUser);
                        if(toUser.equals(mToUser)) {
                            mStatus.setVisibility(View.VISIBLE);
                            mStatus.setText("Typing...");
                        }
                    }catch (JSONException e){
                        Log.e(TAG, "OnTyping: person1 not found ", e);
                    }
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
                    mStatus.setText("");
                    mStatus.setVisibility(View.GONE);
                }
            });
        }
    };

    private Emitter.Listener OnGetMessages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) {
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
                                    mMessages.add(0, new Message(null, uri, true, time));
                                } else {
                                    mMessages.add(0, new Message(null, uri, false, time));
                                }
                            } else {
                                if (person.equals(mCurrentUser.getPhoneNumber())) {
                                    mMessages.add(0, new Message(messageText, null, true, time));
                                } else {
                                    mMessages.add(0, new Message(messageText, null, false, time));
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
            mSocket.emit("stop typing", createBaseJSONObject());
        }
    };

}
