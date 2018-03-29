package com.example.diksha.chatapplication;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by diksha on 15/3/18.
 */

public class UserListFragment extends android.support.v4.app.Fragment {

    private Socket mSocket;
    private FirebaseUser mCurrentUser;
    private List<User> mUserList = new ArrayList<User>();
    private RecyclerView.Adapter mUserAdapter;

    private RecyclerView mUserListView;

    public UserListFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();

        mCurrentUser = app.getCurrentUser();

        sendCurrentUserToServer();

        mSocket.on("login", OnLogin);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set title bar
        ((MainActivity)getActivity()).setActionBarTitle("Users");
        mUserAdapter = new UserAdapter(mUserList, getActivity());



        mUserListView = (RecyclerView) view.findViewById(R.id.userList);

        mUserListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUserListView.setAdapter(mUserAdapter);
    }


    private Emitter.Listener OnLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            //to avoid crash on rotation
            // thread finishes work but activity is no longer visible
            if(getActivity() == null){
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: " + args[0], new Exception());
                    JSONArray result = (JSONArray) args[0];
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject jsonObject = (JSONObject) result.get(i);
                          //  mUserList.clear();
                            mUserList.add(new User(jsonObject.optString("phone")));
                            mUserAdapter.notifyItemInserted(mUserList.size() - 1);
                            mSocket.off("login", OnLogin);
                        } catch (JSONException e) {}
                    }
                }
            });
        }
    };

    public void sendCurrentUserToServer(){
        JSONObject data = new JSONObject();
        try {
            data.put("uid", mCurrentUser.getUid());
            data.put("phone", mCurrentUser.getPhoneNumber());
        } catch (JSONException e) { }
        mSocket.emit("login", data);
    }
}
