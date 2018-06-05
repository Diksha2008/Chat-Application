package com.example.diksha.chatapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by diksha on 4/6/18.
 */

public class RecentChatListFragment extends Fragment {

    private List<User> mUserList = new ArrayList<User>();
    private UserAdapter mUserAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatApplication app = (ChatApplication) getActivity().getApplication();
        Socket mSocket = app.getSocket();
        String currentUser = app.getCurrentUser().getPhoneNumber();

        mSocket.on("get recent chats", OnGetUsers);

        mSocket.emit("get recent chats", currentUser);
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

        RecyclerView userListView = (RecyclerView) view.findViewById(R.id.userList);
        userListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        userListView.setAdapter(mUserAdapter);

    }

    private Emitter.Listener OnGetUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if( getActivity() == null){
                return;
            }
            //TODO: get labels
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray data = (JSONArray) args[0];
                    for (int i = 0; i<data.length(); i++) {
                        try {
                            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
                            JSONObject object = (JSONObject) data.get(i);
                            String phone = object.getString("toId");
                            String selectionArgs[] = {phone};
                            Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null);
                            if (phones != null && phones.moveToNext()) {

                                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                mUserList.add(new User(phoneNumber, name));
                                mUserAdapter.notifyDataSetChanged();
                            }
                            else{
                                mUserList.add((new User(phone, phone)));
                            }
                            if (phones != null) {
                                phones.close();
                            }
                        }catch (JSONException e){
                            Log.e(TAG, "run: Could not Parse",e);
                        }
                    }
                }
            });
        }
    };
}

