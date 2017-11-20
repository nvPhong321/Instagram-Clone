package com.example.phong.instagram.Profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.phong.instagram.Adapter.GridImageAdapter;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.BottomNavigationViewHelper;
import com.example.phong.instagram.Utils.FirebaseMethods;
import com.example.phong.instagram.Utils.UniversalImageLoader;
import com.example.phong.instagram.model.Comment;
import com.example.phong.instagram.model.Like;
import com.example.phong.instagram.model.Photo;
import com.example.phong.instagram.model.User;
import com.example.phong.instagram.model.UserAccountSetting;
import com.example.phong.instagram.model.UserSetting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView mPost,mFollower,mFollowing,mDisplayName,mUserName,mWebsite,mDescription,txtEditProfile;
    private TextView txtPost,txtFollower,txtFollowing,tvPost,tvFollower,tvFollowing;
    private LinearLayout linearLayout;
    private AVLoadingIndicatorView mProgressBar;
    private CircleImageView profilephoto;
    private GridView mGridView;
    private Toolbar mToolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;

    final int color = Color.parseColor("#e7e7e7");
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 4;
    private static final int CAMERA_REQUEST_CODE = 5;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;

    private Photo photo;

    private int FollowingCount = 0;
    private int FollowerCount = 0;
    private int PostCount = 0;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mPost = (TextView) findViewById(R.id.tvPosts);
        mFollower = (TextView) findViewById(R.id.tvFollowers);
        mFollowing = (TextView) findViewById(R.id.tvFollowing);
        mDisplayName = (TextView) findViewById(R.id.display_name);
        mUserName = (TextView) findViewById(R.id.username);
        mWebsite = (TextView) findViewById(R.id.website);
        mDescription = (TextView) findViewById(R.id.description);

        txtPost = (TextView) findViewById(R.id.textPosts);
        txtFollower = (TextView) findViewById(R.id.textFollowers);
        txtFollowing = (TextView) findViewById(R.id.textFollowing);
        tvPost = (TextView) findViewById(R.id.tvPosts);
        tvFollower = (TextView) findViewById(R.id.tvFollowers);
        tvFollowing = (TextView) findViewById(R.id.tvFollowing);
        linearLayout = (LinearLayout) findViewById(R.id.linLayout2);


        txtEditProfile = (TextView) findViewById(R.id.textEditfrofile);

        profilephoto = (CircleImageView) findViewById(R.id.profile_image);
        profileMenu = (ImageView) findViewById(R.id.profileMenu);
        mGridView = (GridView) findViewById(R.id.gridView);
        mToolbar = (Toolbar) findViewById(R.id.profileToolBar);
        mProgressBar = (AVLoadingIndicatorView) findViewById(R.id.profileProgressBar);
        bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);

        mFirebaseMethods = new FirebaseMethods(this);

        try {
            mUser = getUser();
        } catch (NullPointerException e) {

        }

        mProgressBar.setVisibility(View.GONE);
        mProgressBar.hide();
        setupBottomNavigationView();
        setupToolBar();
        setupFirebaseAuth();
        setupGridView();
        getPost();
        getFollowing();
        getFollower();
        moveEditProfile();
        Button();
    }

    private User getUser() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra(getString(R.string.search_activity));
            if (bundle != null) {
                return bundle.getParcelable(getString(R.string.intent_user));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void getFollower(){
        FollowerCount = 0;
        DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();
        Query jquery = dbreference.child(getString(R.string.dbname_follower))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        jquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    FollowerCount ++;
                }
                tvFollower.setText(String.valueOf(FollowerCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowing(){
        FollowingCount = 0;
        DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();
        Query jquery = dbreference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        jquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    FollowingCount ++;
                }
                tvFollowing.setText(String.valueOf(FollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPost(){
        PostCount = 0;
        DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();
        Query jquery = dbreference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        jquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PostCount ++;
                }
                tvPost.setText(String.valueOf(PostCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupGridView(){
        final ArrayList<Photo> photos = new ArrayList<>();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapShot : dataSnapshot.getChildren()){
                    Photo mPhoto = new Photo();
                    Map<String, Object> objectsMap = (HashMap<String, Object>) singleSnapShot.getValue();
                    mPhoto.setCaption(objectsMap.get(getString(R.string.field_caption)).toString());
                    mPhoto.setTag(objectsMap.get(getString(R.string.field_tags)).toString());
                    mPhoto.setPhoto_id(objectsMap.get(getString(R.string.field_photo_id)).toString());
                    mPhoto.setUser_id(objectsMap.get(getString(R.string.field_user_id)).toString());
                    mPhoto.setDate_created(objectsMap.get(getString(R.string.field_date_created)).toString());
                    mPhoto.setImage_path(objectsMap.get(getString(R.string.field_image_path )).toString());

                    ArrayList<Comment> mComments = new ArrayList<Comment>();

                    for (DataSnapshot ds : singleSnapShot
                            .child(getApplication().getString(R.string.field_comment)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(ds.getValue(Comment.class).getUser_id());
                        comment.setComment(ds.getValue(Comment.class).getComment());
                        comment.setDate_created(ds.getValue(Comment.class).getDate_created());
                        comment.setComment_id(ds.getValue(Comment.class).getComment_id());
                        mComments.add(comment);
                    }

                    mPhoto.setComments(mComments);

                    ArrayList<Like> likeList = new ArrayList<Like>();
                    for (DataSnapshot ds : singleSnapShot
                            .child(getString(R.string.field_likes)).getChildren()){
                        Like like = new Like();
                        like.setUser_id(ds.getValue(Like.class).getUser_id());
                        likeList.add(like);
                    }
                    mPhoto.setLikes(likeList);
                    photos.add(mPhoto);

                }
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                mGridView.setColumnWidth(imageWidth);

                final ArrayList<String> imgUrl = new ArrayList<String>();
                for (int i = 0 ; i < photos.size() ; i++){
                    imgUrl.add(photos.get(i).getImage_path());
                }
                final GridImageAdapter gridImageAdapter = new GridImageAdapter(ProfileActivity.this,R.layout.layout_grid_imageview,"",imgUrl);
                mGridView.setAdapter(gridImageAdapter);
                gridImageAdapter.notifyDataSetChanged();

                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        photo = photos.get(position);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(getString(R.string.photo), photo);
                        Intent intent = new Intent(ProfileActivity.this, ViewPostActivity.class);
                        intent.putExtra(getString(R.string.selected_view_post_image),bundle);
                        startActivity(intent);
                        finish();
                    }
                });

                mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        photo = photos.get(position);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure delete image ?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing but close the dialog
                                mRef.child(getString(R.string.dbname_user_photos))
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(photo.getPhoto_id())
                                        .removeValue();
                                mRef.child(getString(R.string.dbname_photos))
                                        .child(photo.getPhoto_id())
                                        .removeValue();
                                photos.remove(position);
                                imgUrl.remove(position);
                                gridImageAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void Button() {
        profilephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE){
            try {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                Intent intent = new Intent(ProfileActivity.this, AccountSettingActivity.class);
                intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile));
                startActivity(intent);
                finish();
            }catch (NullPointerException e){

            }
        }
    }


    private void moveEditProfile(){
        txtEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtEditProfile.setBackgroundColor(color);
                Intent intent = new Intent(ProfileActivity.this,AccountSettingActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
                finish();
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

    private void setProfileWidgets(UserSetting userSetting){
        User user = userSetting.getUser();
        UserAccountSetting setting = userSetting.getUserAccountSetting();
        UniversalImageLoader.setImage(setting.getProfile_photo(),profilephoto,null,"");

        mDisplayName.setText(setting.getDisplay_name());
        mUserName.setText(setting.getUsername());
        mWebsite.setText(setting.getWebsite());
        mDescription.setText(setting.getDescription());

        final String Url = setting.getWebsite();
        mWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Url));
                startActivity(intent);
            }
        });
    }


    private void setupToolBar(){
        setSupportActionBar(mToolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this,AccountSettingActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    /* ------------------------- Fire Base ----------------------------*/

    private void setupFirebaseAuth() {

        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.show();
        txtEditProfile.setVisibility(View.GONE);
        txtPost.setVisibility(View.GONE);
        txtFollower.setVisibility(View.GONE);
        txtFollowing.setVisibility(View.GONE);
        tvPost.setVisibility(View.GONE);
        tvFollower.setVisibility(View.GONE);
        tvFollowing.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
        mUserName.setVisibility(View.VISIBLE);
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

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve user information from the database
                mProgressBar.setVisibility(View.GONE);
                mProgressBar.hide();
                txtEditProfile.setVisibility(View.VISIBLE);
                txtPost.setVisibility(View.VISIBLE);
                txtFollower.setVisibility(View.VISIBLE);
                txtFollowing.setVisibility(View.VISIBLE);
                tvPost.setVisibility(View.VISIBLE);
                tvFollower.setVisibility(View.VISIBLE);
                tvFollowing.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                setProfileWidgets(mFirebaseMethods.getUserSetting(dataSnapshot));
                //retrieve image for the user in question
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressBar.setVisibility(View.GONE);
                mProgressBar.hide();
            }
        });
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
