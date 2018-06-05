package com.example.diksha.chatapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Created by diksha on 15/3/18.
 */

public class UserListFragment extends android.support.v4.app.Fragment {

    private Socket mSocket;
    private FirebaseUser mCurrentUser;
    private List<User> mUserList = new ArrayList<User>();
    private RecyclerView.Adapter mUserAdapter;
    private Map<String, JSONObject> mData;

    private RecyclerView mUserListView;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    public UserListFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on("get users", OnGetUsers);

        mData = new HashMap<String, JSONObject>();

        mCurrentUser = app.getCurrentUser();

        JSONObject userData = new JSONObject();
        try {
            userData.put("phone", mCurrentUser.getPhoneNumber());
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e);
        }
        mSocket.emit("get users", userData);

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
//        ((MainActivity)getActivity()).setActionBarTitle("Users");
        mUserAdapter = new UserAdapter(mUserList, getActivity());

        mUserListView = (RecyclerView) view.findViewById(R.id.userList);

        mUserListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUserListView.setAdapter(mUserAdapter);

    }

    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            getContactNames();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts();
            } else {
                Toast.makeText(getContext(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getContactNames() {
        LinkedHashSet<String> numbers = new LinkedHashSet<>();
        LinkedHashSet<String> names = new LinkedHashSet<>();

        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones!=null && phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            for (int i = 0; i < mUserList.size(); i++) {
                Log.i(TAG, "getContactNames: " + phoneNumber + " " + mUserList.get(i));
            }
            if (mData != null && mData.containsKey(phoneNumber)) {
                numbers.add(phoneNumber);
                names.add(name);
            }
        }

        for (Iterator<String> i = numbers.iterator(), j = names.iterator(); i.hasNext();) {
            mUserList.add(new User(i.next(), j.next()));
            mUserAdapter.notifyItemInserted(mUserList.size() - 1);
        }

        if (phones != null) {
            phones.close();
        }
    }

    private Emitter.Listener OnGetUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            //to avoid crash on rotation
            // thread finishes work but activity is no longer visible
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray result = (JSONArray) args[0];
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject jsonObject = (JSONObject) result.get(i);
                            mData.put(jsonObject.getString("phone"), jsonObject);
                            mSocket.off("get users", OnGetUsers);
                        } catch (JSONException e) {
                            Log.e(TAG, "OnGetUser: Could not parse", e);
                        }
                    }
                    showContacts();
                }
            });
        }
    };

}
