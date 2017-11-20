package com.example.phong.instagram.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.phong.instagram.Adapter.MessSearchAdapter;
import com.example.phong.instagram.Profile.ProfileActivity;
import com.example.phong.instagram.Profile.ViewProfileActivity;
import com.example.phong.instagram.R;
import com.example.phong.instagram.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by phong on 8/12/2017.
 */

public class MessageFragment extends Fragment {

    private static final String TAG = "MessFragment";


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;

    private ListView lvMessSearch;
    private ArrayList<User> user;
    private ArrayList<String> follow;
    private MessSearchAdapter adapter;
    private EditText edtSearchMess;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        user = new ArrayList<>();
        follow = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        lvMessSearch = (ListView) view.findViewById(R.id.lvMessSearch);
        edtSearchMess = (EditText) view.findViewById(R.id.SearchMess);
        initTextListener();
        getListFollowing();
        closeKeyboard();
        return view;
    }

    private void getListFollowing() {

        DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();
        Query jquery = dbreference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        jquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    follow.add(ds.child(getString(R.string.field_user_id)).getValue().toString());
                }
                getInfoFollow();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getInfoFollow() {

        DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();

        for (int i = 0; i < follow.size(); i++) {

            final int count = i;
            Query jquery = dbreference.child(getString(R.string.dbname_user_account_settings))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(follow.get(i));
            jquery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        user.add(snapshot.getValue(User.class));
                    }
                    if (count >= follow.size() - 1) {
                        //display our photo
                        setupWidget();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void searchForMatch(String keyWord) {
        user.clear();
        if (keyWord.length() == 0) {
            user.clear();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_user_account_settings))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyWord);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {

                        user.add(ds.getValue(User.class));
                        setupWidget();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void initTextListener() {
        user = new ArrayList<>();
        edtSearchMess.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = edtSearchMess.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
                if (text.equals("") || text.length() < 1) {
                    user.clear();
                    getInfoFollow();
                    setupWidget();
                }
            }
        });
    }

    private void setupWidget() {
        adapter = new MessSearchAdapter(getActivity(), R.layout.layout_item_mess_search, user);
        lvMessSearch.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        lvMessSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final User list = user.get(position);
                final String s = user.get(position).getUser_id();

                if (s.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(getString(R.string.intent_user), list);
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(getString(R.string.search_activity), bundle);
                    startActivity(intent);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(getString(R.string.intent_user), list);
                    Intent intent = new Intent(getActivity(), ViewProfileActivity.class);
                    intent.putExtra(getString(R.string.search_activity), bundle);
                    startActivity(intent);
                }
            }
        });
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
