package com.example.phong.instagram.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Created by phong on 10/1/2017.
 */

public class Heart {
    private static final String TAG = "Heart";

    public ImageView Like, DisLike;
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    public Heart(ImageView Like,ImageView DisLike){
        this.Like = Like;
        this.DisLike = DisLike;
    }

    public void toggleLike(){
        Log.d(TAG,"toggle like: toggle heart");
        AnimatorSet animatorSet = new AnimatorSet();
        if(Like.getVisibility()== View.VISIBLE){
            Log.d(TAG,"toggle like: dislike");
            DisLike.setScaleX(0.1f);
            DisLike.setScaleY(0.1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(DisLike,"scaleY",0.1f,1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(DisLike,"scaleX",0.1f,1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            Like.setVisibility(View.GONE);
            DisLike.setVisibility(View.VISIBLE);

            animatorSet.playTogether(scaleDownX,scaleDownY);
        }else if(Like.getVisibility() == View.GONE){
            Log.d(TAG,"toggle like: like");
            Like.setScaleX(0.1f);
            Like.setScaleY(0.1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(Like,"scaleY",0.1f,1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(Like,"scaleX",0.1f,1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            Like.setVisibility(View.VISIBLE);
            DisLike.setVisibility(View.GONE);

            animatorSet.playTogether(scaleDownY,scaleDownX);
        }
        animatorSet.start();
    }

}
