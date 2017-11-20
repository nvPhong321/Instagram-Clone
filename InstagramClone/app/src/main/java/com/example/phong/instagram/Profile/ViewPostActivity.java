package com.example.phong.instagram.Profile;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.BottomNavigationViewHelper;
import com.example.phong.instagram.Utils.FirebaseMethods;
import com.example.phong.instagram.Utils.Heart;
import com.example.phong.instagram.Utils.SquareImageView;
import com.example.phong.instagram.Utils.UniversalImageLoader;
import com.example.phong.instagram.model.Comment;
import com.example.phong.instagram.model.Like;
import com.example.phong.instagram.model.Photo;
import com.example.phong.instagram.model.User;
import com.example.phong.instagram.model.UserAccountSetting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by phong on 8/12/2017.
 */

public class ViewPostActivity extends AppCompatActivity {

    private static final String TAG = "ViewPostActivity";

    final int color = Color.parseColor("#3f8cff");

    private static final int ACTIVITY_NUM = 4;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private ImageView backPost,mComment,mLike,mDisLike,mImageLike;
    private Intent intent;
    private Photo photo;
    private Toolbar mToolbar;
    private SquareImageView mPostImage;
    private CircleImageView mViewProfilePhoto;
    private TextView txtUserName,txtViewLike,txtViewComment,txtViewCaption,txtViewTimePost;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;

    private GestureDetector mGestureDetector,mGestureDetectors;
    private Heart mHeart;
    private Animation zoom_in;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private UserAccountSetting mUserAccountSetting;
    private String mLikesString = "";
    private User mCurrentUser;

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        zoom_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.takingoffanimator);

        bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        mPostImage = (SquareImageView) findViewById(R.id.post_image);
        mViewProfilePhoto = (CircleImageView) findViewById(R.id.profile_photo_view);
        txtUserName = (TextView) findViewById(R.id.user_name_view);
        txtViewLike = (TextView) findViewById(R.id.txtViewLike);
        txtViewCaption = (TextView) findViewById(R.id.txtViewCaption);
        txtViewComment = (TextView) findViewById(R.id.txtViewComment);
        txtViewTimePost = (TextView) findViewById(R.id.txtViewTimePost);

        mToolbar = (Toolbar) findViewById(R.id.optionsToolBarPost);
        setSupportActionBar(mToolbar);

        backPost = (ImageView) findViewById(R.id.backPost);
        mComment = (ImageView) findViewById(R.id.comment);
        mLike = (ImageView) findViewById(R.id.like);
        mDisLike = (ImageView) findViewById(R.id.dislike);
        mImageLike = (ImageView) findViewById(R.id.img_like);

        mImageLike.setVisibility(View.GONE);

        mGestureDetector = new GestureDetector(this, new SingleTapConfirm());
        mGestureDetectors = new GestureDetector(this, new DoubleTapConfirm());
        mHeart = new Heart(mDisLike,mLike);
        try{
            //photo = getPhoto();

            UniversalImageLoader.setImage(getPhoto().getImage_path(),mPostImage,null,"");
            String photo_id = getPhoto().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getApplication().getString(R.string.dbname_photos))
                    .orderByChild(getApplication().getString(R.string.field_photo_id))
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapShot : dataSnapshot.getChildren()){
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectsMap = (HashMap<String, Object>) singleSnapShot.getValue();

                        newPhoto.setCaption(objectsMap.get(getApplication().getString(R.string.field_caption)).toString());
                        newPhoto.setTag(objectsMap.get(getApplication().getString(R.string.field_tags)).toString());
                        newPhoto.setPhoto_id(objectsMap.get(getApplication().getString(R.string.field_photo_id)).toString());
                        newPhoto.setUser_id(objectsMap.get(getApplication().getString(R.string.field_user_id)).toString());
                        newPhoto.setDate_created(objectsMap.get(getApplication().getString(R.string.field_date_created)).toString());
                        newPhoto.setImage_path(objectsMap.get(getApplication().getString(R.string.field_image_path )).toString());

                        List<Comment> commentList = new ArrayList<Comment>();

                        for (DataSnapshot ds : singleSnapShot
                                .child(getApplication().getString(R.string.field_comment)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(ds.getValue(Comment.class).getUser_id());
                            comment.setComment(ds.getValue(Comment.class).getComment());
                            comment.setDate_created(ds.getValue(Comment.class).getDate_created());
                            comment.setComment_id(ds.getValue(Comment.class).getComment_id());
                            commentList.add(comment);
                        }

                        newPhoto.setComments(commentList);

                        photo = newPhoto;
                        getCurrentUser();
                        getPhotoDetail();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){

        }
        setupFirebaseAuth();
        setupBottomNavigationView();
        Button();

    }

    @Nullable
    private Photo getPhoto(){
        intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getBundleExtra(getString(R.string.selected_view_post_image));
            if (bundle != null) {
                return bundle.getParcelable(getString(R.string.photo));
            } else {
                return null;
            }
        }else {
            return null;
        }
    }

    private void setupWidgets(){
        String timestampDiff = getTimestampDifference();
        if(!timestampDiff.equals("0")){
            txtViewTimePost.setText(timestampDiff + " Day Ago");
        }else {
            txtViewTimePost.setText("To Day");
        }
        if (mLikedByCurrentUser) {
            txtViewLike.setText(mLikesString);
        }else {
            txtViewLike.setText("0 Likes");
        }

        if(mLikedByCurrentUser){
            mDisLike.setVisibility(View.GONE);
            mLike.setVisibility(View.VISIBLE);
            mLike.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });
            mPostImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return mGestureDetectors.onTouchEvent(event);
                }
            });
        }else {
            mLike.setVisibility(View.GONE);
            mDisLike.setVisibility(View.VISIBLE);
            mDisLike.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
    }

    private void LikeDoubleTap(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(photo.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    //case 1: then user already liked photo
                    if(mLikedByCurrentUser &&
                            singleSnapshot.getValue(Like.class).getUser_id()
                                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        animationLikeDoubleTap();
                       getLikesString();
                    }
                    //case 2: then user has not liked photo
                    else if(!mLikedByCurrentUser){
                        //add new like
                        addNewLikeDoubleTap();
                        break;
                    }
                }
                if(!dataSnapshot.exists()){
                    //add new like
                    addNewLikeDoubleTap();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void animationLikeDoubleTap(){
        AnimatorSet animatorSet = new AnimatorSet();
        if(mDisLike.getVisibility() == View.VISIBLE){
            Log.d(TAG,"toggle like: like");
            mLike.setScaleX(0.1f);
            mLike.setScaleY(0.1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mLike,"scaleY",0.1f,1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mLike,"scaleX",0.1f,1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            mLike.setVisibility(View.VISIBLE);
            mDisLike.setVisibility(View.GONE);

            animatorSet.playTogether(scaleDownY,scaleDownX);
        }
        animatorSet.start();
    }
    private void Like(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(photo.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String keyID = singleSnapshot.getKey();
                    //case 1: then user already liked photo
                    if(mLikedByCurrentUser &&
                            singleSnapshot.getValue(Like.class).getUser_id()
                                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        mRef.child(getString(R.string.dbname_photos))
                                .child(photo.getPhoto_id())
                                .child(getString(R.string.field_likes))
                                .child(keyID)
                                .removeValue();

                        mRef.child(getString(R.string.dbname_user_photos))
                                .child(photo.getUser_id())
                                .child(photo.getPhoto_id())
                                .child(getString(R.string.field_likes))
                                .child(keyID)
                                .removeValue();

                        mHeart.toggleLike();
                        getLikesString();
                    }
                    //case 2: then user has not liked photo
                    else if(!mLikedByCurrentUser){
                        //add new like
                        addNewLike();
                        break;
                    }
                }
                if(!dataSnapshot.exists()){
                    //add new like
                    addNewLike();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getTimestampDifference(){
        Log.d(TAG,"getTimestampDifference");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd GGG hh:mm aaa", Locale.getDefault());
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        }catch (ParseException e){
            difference = "0";
        }
        return difference;
    }

    private void getLikesString(){
        Log.d(TAG,"getLikesString: getting likes string");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(photo.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUsers = new StringBuilder();
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                Log.d(TAG,"onDataChange: found like :" + singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] spilitUser = mUsers.toString().split(",");

                            if(mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser = true;
                            }else {
                                mLikedByCurrentUser = false;
                            }

                            int length = spilitUser.length;
                            if(length == 1){
                                mLikesString = "Likes by " + spilitUser[0];
                            }else if(length == 2){
                                mLikesString = "Likes by " + spilitUser[0]
                                + " and " + spilitUser[1];

                            }else if(length == 3){
                                mLikesString = "Likes by " + spilitUser[0]
                                        + ", " + spilitUser[1]
                                        + " and " + spilitUser[2];

                            }else if(length == 4){
                                mLikesString = "Likes by " + spilitUser[0]
                                        + ", " + spilitUser[1]
                                        + ", " + spilitUser[2]
                                        + " and " + spilitUser[3];

                            }else if (length > 4){
                                mLikesString = "Likes by " + spilitUser[0]
                                        + ", " + spilitUser[1]
                                        + ", " + spilitUser[2]
                                        + " and " + (spilitUser.length - 3) + " others";

                            }
                            setupWidgets();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    mLikesString="";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    mCurrentUser = singleSnapshot.getValue(User.class);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onSingleTapConfirmed (MotionEvent e){
            Like();
            return true;
        }
    }

    public class DoubleTapConfirm extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            LikeDoubleTap();
            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG,"addNewLike: adding new like");
        String newLikeID = mRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mRef.child(getString(R.string.dbname_photos))
                .child(photo.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mRef.child(getString(R.string.dbname_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    private void addNewLikeDoubleTap(){
        Log.d(TAG,"addNewLike: adding new like");
        String newLikeID = mRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mRef.child(getString(R.string.dbname_photos))
                .child(photo.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mRef.child(getString(R.string.dbname_user_photos))
                .child(photo.getUser_id())
                .child(photo.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        animationLikeDoubleTap();
        mImageLike.setVisibility(View.VISIBLE);
        mImageLike.startAnimation(zoom_in);
        getLikesString();
    }

    private void Button(){
        backPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ViewPostActivity.this,ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.photo), photo);
                intent = new Intent(ViewPostActivity.this,ViewCommentActivity.class);
                intent.putExtra(getString(R.string.selected_view_comment),bundle);
                startActivity(intent);
            }
        });
    }

    private void getPhotoDetail(){
        Log.d(TAG, "getphotodetail: photodetail");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(photo.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    mUserAccountSetting = singleSnapshot.getValue(UserAccountSetting.class);
                }
                UniversalImageLoader.setImage(mUserAccountSetting.getProfile_photo(),mViewProfilePhoto,null,"");

                String Caption = photo.getCaption().toString();
                String UserName = mUserAccountSetting.getUsername();

                SpannableStringBuilder builder = new SpannableStringBuilder();

                SpannableString str1= new SpannableString(UserName.toString() + " ");
                str1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str1.length(), 0);
                str1.setSpan(new StyleSpan(Typeface.BOLD),0,str1.length(),0);
                builder.append(str1);

                SpannableString str2= new SpannableString(Caption.toString() + " ");

                if(Caption.toString().startsWith("#") || Caption.toString().startsWith("@")) {

                    str2.setSpan(new ForegroundColorSpan(Color.parseColor("#285480")), 0, str2.length(), 0);

                }else {

                    str2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str2.length(), 0);

                }
                builder.append(str2);
                if(photo.getComments().size() > 0){
                    txtViewComment.setText("View all " + photo.getComments().size() + " comment");
                }else {
                    txtViewComment.setText("");
                    RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW, R.id.txtViewCaption);
                    params.setMargins(0,15,0,0);
                    txtViewTimePost.setLayoutParams(params);
                }

                txtViewComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(getString(R.string.photo), photo);
                        intent = new Intent(ViewPostActivity.this,ViewCommentActivity.class);
                        intent.putExtra(getString(R.string.selected_view_comment),bundle);
                        startActivity(intent);
                    }
                });
                txtViewCaption.setText(builder, TextView.BufferType.SPANNABLE);
                txtUserName.setText(UserName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupBottomNavigationView() {
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        intent = new Intent(ViewPostActivity.this,ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    /* ------------------------- Fire Base ----------------------------*/

    private void setupFirebaseAuth() {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
