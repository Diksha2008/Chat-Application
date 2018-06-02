package com.example.diksha.chatapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by priyanka on 15/3/18.
 */

public class UserListFragment extends android.support.v4.app.Fragment {
    private Socket mSocket;
    private FirebaseUser mCurrentUser;
    private List<User> mUserList = new ArrayList<User>();
    private RecyclerView.Adapter mUserAdapter;
    private RecyclerView mUserListView;
    private int counter=0;
    private  int size=0;
    public UserListFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mCurrentUser = app.getCurrentUser();
        ((MainActivity)getActivity()).setActionBarTitle("CHATBIZ");
        mSocket.connect();

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
        mUserAdapter.notifyDataSetChanged();

        if (!getUserVisibleHint())
        {
            return;
        }
        ((MainActivity)getActivity()).setActionBarSubTitle(null);
        JSONObject data = new JSONObject();
        try
        {
            data.put("uid", mCurrentUser.getUid());
            data.put("phone", mCurrentUser.getPhoneNumber());
        }
        catch (JSONException e) {}
        counter++;
        mUserList.clear();
        mSocket.on("login", OnLogin);
        mSocket.emit("login", data);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserAdapter = new UserAdapter(mUserList, getActivity());
        mUserListView = (RecyclerView) view.findViewById(R.id.userList);
        mUserListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((MainActivity)getActivity()).setActionBarTitle("CHATBIZ");
        mUserListView.setAdapter(mUserAdapter);
    }


    private Emitter.Listener OnLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "LALALALA", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "run: " + args[0], new Exception());
                    JSONArray result = (JSONArray) args[0];
                    int c=0;
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject jsonObject = (JSONObject) result.get(i);
                            String unread = jsonObject.optString("unread");
                            String phone = jsonObject.optString("phone");
                            String color = jsonObject.optString("color");
                            User user = new User(phone, color, unread);
                            if( counter == 1) {
                                mUserList.add(user);
                                mUserAdapter.notifyItemInserted(mUserList.size() - 1);
                                size = mUserAdapter.getItemCount();
                            }
                            else {
                                c++;
                                mUserList.add(user);
                                if( c == size) {
                                    mUserAdapter.notifyDataSetChanged();
                                }
                            }

                        } catch (JSONException e) {}
                        mSocket.off("login",OnLogin);
                    }
                }
            });
        }
    };

}
