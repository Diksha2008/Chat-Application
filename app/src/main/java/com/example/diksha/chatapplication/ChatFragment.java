package com.example.diksha.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    private Socket mSocket;
    private RecyclerView mMessageView;
    private RecyclerView.Adapter mAdapter;
    private EditText mEditText;
    private TextView mStatus;
    private List<Message> mMessages = new ArrayList<Message>();
    private boolean mTyping = false;
    private Handler mMessageHandler = new Handler();
    private static final int RC_PHOTO_PICKER = 2;
    private static final String TAG = "ChatFragment";


    public ChatFragment() {
        super();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, OnConnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, OnConnectError);
        mSocket.on("new message", OnNewMessage);
        mSocket.on("image", OnNewImage);
        mSocket.on("typing", OnTyping);
        mSocket.on("stop typing", OnStopTyping);
        mSocket.connect();

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
        mSocket.off(Socket.EVENT_CONNECT, OnConnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, OnConnectError);
        mSocket.off("new message", OnNewMessage);
        mSocket.off("image", OnNewImage);
        mSocket.off("typing", OnTyping);
        mSocket.off("stop typing", OnStopTyping);
        mSocket.disconnect();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMessageView = (RecyclerView) view.findViewById(R.id.messages);
        mStatus = (TextView) view.findViewById(R.id.status);

        mAdapter = new MessageAdapter(mMessages);
        mMessageView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessageView.setAdapter(mAdapter);

        mEditText = (EditText) view.findViewById(R.id.message_input);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!mTyping){
                    mTyping = true;
                    mSocket.emit("typing");
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
                attemptSend();
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
        if(requestCode == RC_PHOTO_PICKER) {
            Uri selectedImageUri = data.getData();
            JSONObject sendImage = new JSONObject();
            try {
                sendImage.put("image", encodeImage(selectedImageUri));
                mSocket.emit("image", sendImage);
            }catch (JSONException e){ }
        }
        else{
            Log.e(TAG, "onActivityResult: ", new Exception());
        }
    }

    private String encodeImage(Uri image){
        String encodedImage = null;
        try {
            // uri -> bitmap -> byteArrayOutputStream -> byte array -> String
            Bitmap selectedImagebitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImagebitmap.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);
            byte[] b = byteArrayOutputStream.toByteArray();
            encodedImage = Base64.encodeToString(b,Base64.DEFAULT);
        }catch (IOException e){
            Log.e(TAG, "onActivityResult: ", e);
        }
        return encodedImage;
    }

    private Bitmap decodeImage(String data){
        byte[] b = Base64.decode(data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    //insert message in current user's field
    private void  attemptSend(){
        String message = mEditText.getText().toString().trim();
        mEditText.setText("");
        mSocket.emit("new message", message);
    }

    private void insertMessage(String message){
        mMessages.add(new Message(message, null));
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void insertImage(Bitmap image){
        mMessages.add(new Message(null,image));
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();

    }

    private void scrollToBottom(){
        mMessageView.scrollToPosition(mMessages.size() - 1);
    }
    private Emitter.Listener OnConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener OnConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
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
                    String message = (String) args[0];
                    insertMessage(message);
                }
            });
        }
    };

    private Emitter.Listener OnNewImage = new Emitter.Listener() {
        @Override
        public void call(final  Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String selectedImage;
                    JSONObject image = (JSONObject) args[0];
                    try {
                        selectedImage = image.getString("image");
                        Bitmap bitmap = decodeImage(selectedImage);
                        insertImage(bitmap);
                    }catch (JSONException e){ }
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
//                    mMessages.add(new Message("typing..."));
//                    mAdapter.notifyItemInserted(mMessages.size() - 1);

                    mStatus.setText("Typing...");
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
                }
            });
        }
    };

    private Runnable OnTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if(!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

}
