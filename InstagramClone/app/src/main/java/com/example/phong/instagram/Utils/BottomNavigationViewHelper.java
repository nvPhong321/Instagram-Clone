package com.example.phong.instagram.Utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.example.phong.instagram.Home.HomeActivity;
import com.example.phong.instagram.Like.LikeActivity;
import com.example.phong.instagram.Profile.ProfileActivity;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Search.SearchActivity;
import com.example.phong.instagram.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Created by phong on 8/11/2017.
 */

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHelp";
    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Activity context, BottomNavigationViewEx ex) {
        ex.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_house:
                        Intent intent1 = new Intent(context, HomeActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent1);
                        context.overridePendingTransition(R.anim.pull_in_left,R.anim.pull_out_right);
                        break;
                    case R.id.ic_search:
                        Intent intent2 = new Intent(context, SearchActivity.class);
                        context.startActivity(intent2);
                        context.overridePendingTransition(R.anim.pull_in_left,R.anim.pull_out_right);
                        break;
                    case R.id.ic_circle:
                        Intent intent3 = new Intent(context, ShareActivity.class);
                        context.startActivity(intent3);
                        break;
                    case R.id.ic_alert:
                        Intent intent4 = new Intent(context, LikeActivity.class);
                        context.startActivity(intent4);
                        context.overridePendingTransition(R.anim.pull_in_left,R.anim.pull_out_right);
                        break;
                    case R.id.ic_android:
                        Intent intent5 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent5);
                        context.overridePendingTransition(R.anim.pull_in_left,R.anim.pull_out_right);
                        break;
                }
                return false;
            }
        });
    }
}
