package com.example.diksha.chatapplication;


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
import android.Manifest;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static android.content.ContentValues.TAG;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;



/**
 * Created by priyanka on 15/3/18.
 */

public class RecentChatListFragment extends android.support.v4.app.Fragment {
    private Socket mSocket;
    private FirebaseUser mCurrentUser;
    private JSONArray result;
    private List<User> mRecentList = new ArrayList<User>();
    private RecyclerView.Adapter mRecentChatAdapter;
    private RecyclerView mRecentListView;
    private int counter = 0;
    public String currentUser;
    private int size = 0;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    public RecentChatListFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        currentUser = app.getCurrentUser().getPhoneNumber();
        ((MainActivity) getActivity()).setActionBarTitle("CHATBIZ");
        mSocket.connect();

    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecentChatAdapter.notifyDataSetChanged();

        if (!getUserVisibleHint()) {
            return;
        }
        ((MainActivity) getActivity()).setActionBarTitle("CHATBIZ");
        ((MainActivity) getActivity()).setActionBarSubTitle(null);
        mRecentList.clear();
        counter++;
        mSocket.on("get recent chats", OnGetUsers);
        mSocket.emit("get recent chats", currentUser);
    }

    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            getContactNames();
        }
    }

    private void getContactNames() {
        for (int i = 0; i < result.length(); i++) {
            try {
                JSONObject obj = (JSONObject) result.get(i);
                int userType = obj.getInt("user_type");
                String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
                String selectionArgs[] = {obj.getString("phone")};
                Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null);
                if (phones != null && phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.i(TAG, "run: " + name + " " + phoneNumber);
                    mRecentList.add(new User(phoneNumber, name, userType));
                }
                if (phones != null) {
                    phones.close();
                }
            } catch (JSONException e) {
                Log.e(TAG, "run: Could not Parse", e);
            }
            //***changed from user list
            Collections.sort(mRecentList, new Comparator<User>() {
                @Override
                public int compare(User user1, User user2) {
                    return user1.getUsername().compareToIgnoreCase(user2.getUsername());
                }
            });
            mRecentChatAdapter.notifyDataSetChanged();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecentChatAdapter = new RecentChatAdapter(mRecentList, getActivity());
        mRecentListView = (RecyclerView) view.findViewById(R.id.userList);
        mRecentListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((MainActivity) getActivity()).setActionBarTitle("CHATBIZ");
        mRecentListView.setAdapter(mRecentChatAdapter);
    }


    private Emitter.Listener OnGetUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null){
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecentList.clear();
//                    Toast.makeText(getActivity(), "LALALALA", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "run: " + args[0], new Exception());
                    JSONArray result = (JSONArray) args[0];
                    int c = 0;
                    for (int i = 1; i < result.length(); i++) {
                        try {
                            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
                            JSONObject jsonObject = (JSONObject) result.get(i);
                            int unread = jsonObject.getInt("unread");
                            String phone = jsonObject.optString("phone");
                            String color = jsonObject.optString("color");
                            int userType = jsonObject.optInt("user_type");
                            Log.i(TAG, "run: " + color);
                            String selectionArgs[] = {phone};
                            Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null);
                            if (counter == 1) {
                                if (phones != null && phones.moveToNext()) {

                                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                    mRecentList.add(new User(phoneNumber, name, color, unread, userType));
                                    mRecentChatAdapter.notifyDataSetChanged();
                                } else {
                                    mRecentList.add((new User(phone, phone, color, unread, userType)));
                                }
                                if (phones != null) {
                                    phones.close();
                                }
                                mRecentChatAdapter.notifyItemInserted(mRecentList.size() - 1);
                                size = mRecentChatAdapter.getItemCount();
                            } else {
                                c++;
                                if (phones != null && phones.moveToNext()) {

                                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    mRecentList.add(new User(phoneNumber, name, color, unread, userType));
                                } else {
                                    mRecentList.add((new User(phone, phone, color, unread, userType)));
                                }
                                if (phones != null) {
                                    phones.close();
                                }
                                if (c == size) {
                                    mRecentChatAdapter.notifyDataSetChanged();
                                }
                            }
                            mSocket.off("get recent chats", OnGetUsers);
                        } catch (JSONException e) {
                            Log.e(TAG, "run: Could not parse", e);
                        }

                    }
                }
            });
        }
    };
}