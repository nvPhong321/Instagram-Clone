package com.example.phong.instagram.Profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.phong.instagram.Adapter.SectionStatePagerAdapter;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.BottomNavigationViewHelper;
import com.example.phong.instagram.Utils.FirebaseMethods;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

/**
 * Created by phong on 8/16/2017.
 */

public class AccountSettingActivity extends AppCompatActivity {

    private static final String TAG = "AccountSetting";

    private static final int ACTIVITY_NUM = 4;
    private SectionStatePagerAdapter pagerAdapter;
    private RelativeLayout mRelativeLayout;
    private ViewPager viewPager;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        setupToolBar();
        setupBottomNavigationView();
        setupSettingList();
        setupFragment();
        getIncomingIntent();
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.optionsToolBar);
        setSupportActionBar(toolbar);

        ImageView back = (ImageView) findViewById(R.id.backArrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountSettingActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap))) {
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile))) {
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    FirebaseMethods mFirebaseMethods = new FirebaseMethods(AccountSettingActivity.this);
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                            intent.getStringExtra(getString(R.string.selected_image)), null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))){
                    FirebaseMethods mFirebaseMethods = new FirebaseMethods(AccountSettingActivity.this);
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0, null,
                            (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }
            }
        }

        if (intent.hasExtra(getString(R.string.calling_activity))) {
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile)));
        }
    }

    private void setupFragment() {
        viewPager = (ViewPager) findViewById(R.id.container);
        pagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile));
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out));

    }

    private void setViewPager(int fragmentNumber) {
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayout1);
        mRelativeLayout.setVisibility(View.GONE);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(fragmentNumber);
    }

    private void setupSettingList() {
        ListView listView = (ListView) findViewById(R.id.lvAccountSetting);
        ArrayList<String> options = new ArrayList<String>();
        options.add(getString(R.string.edit_profile));
        options.add(getString(R.string.sign_out));
        ArrayAdapter adapter = new ArrayAdapter(AccountSettingActivity.this, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewPager(position);
            }
        });
    }

    private void setupBottomNavigationView() {
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(AccountSettingActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
