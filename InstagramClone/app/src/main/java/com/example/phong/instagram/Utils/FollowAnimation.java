package com.example.phong.instagram.Utils;

import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

/**
 * Created by phong on 10/1/2017.
 */

public class FollowAnimation {
    private static final String TAG = "Heart";

    public TextView Follow, UnFollow;
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    public FollowAnimation(TextView Follow, TextView UnFollow){
        this.Follow = Follow;
        this.UnFollow = UnFollow;
    }

    public void toggleFollow(){
        Log.d(TAG,"toggle like: toggle heart");
        if(Follow.getVisibility()== View.VISIBLE){
            Log.d(TAG,"toggle like: dislike");
            Follow.setVisibility(View.GONE);
            UnFollow.setVisibility(View.VISIBLE);

        }else if(Follow.getVisibility() == View.GONE){
            Log.d(TAG,"toggle like: like");
            Follow.setVisibility(View.VISIBLE);
            UnFollow.setVisibility(View.GONE);
        }
    }

}
