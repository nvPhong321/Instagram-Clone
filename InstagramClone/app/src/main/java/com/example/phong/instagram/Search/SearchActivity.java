package com.example.phong.instagram.Search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.phong.instagram.Adapter.SearchAdapter;
import com.example.phong.instagram.Profile.ProfileActivity;
import com.example.phong.instagram.Profile.ViewProfileActivity;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.BottomNavigationViewHelper;
import com.example.phong.instagram.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private static final int ACTIVITY_NUM = 1;
    private EditText edtSearch;
    private ListView lvSearch;
    private ImageView imgBack;

    private SearchAdapter adapter;

    private List<User> mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        edtSearch = (EditText) findViewById(R.id.Search);
        lvSearch = (ListView) findViewById(R.id.lvSearch);
        imgBack = (ImageView) findViewById(R.id.backSearch);

        closeKeyboard();
        setupBottomNavigationView();
        initTextListener();
        Button();
    }

    private void searchForMatch(String keyWord) {
        Log.d(TAG, "search for match" + keyWord);
        mUserList.clear();
        if (keyWord.length() == 0) {
            mUserList.clear();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyWord);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found use:" + ds.getValue(User.class).toString());

                        mUserList.add(ds.getValue(User.class));
                        setupWidget();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void setupWidget() {
        adapter = new SearchAdapter(this, R.layout.layout_item_search, mUserList);
        lvSearch.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        lvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final User list = mUserList.get(position);
                final String s = mUserList.get(position).getUser_id();

                if (s.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(getString(R.string.intent_user), list);
                    Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                    intent.putExtra(getString(R.string.search_activity), bundle);
                    startActivity(intent);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(getString(R.string.intent_user), list);
                    Intent intent = new Intent(SearchActivity.this, ViewProfileActivity.class);
                    intent.putExtra(getString(R.string.search_activity), bundle);
                    startActivity(intent);
                }
            }
        });
    }

    private void Button() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initTextListener() {
        mUserList = new ArrayList<>();
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
                if (text.equals("") || text.length() < 1) {
                    mUserList.clear();
                    setupWidget();
                }
            }
        });
    }

    private void closeKeyboard() {
        View view = SearchActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupBottomNavigationView() {
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(SearchActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
