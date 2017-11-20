package com.example.phong.instagram.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.phong.instagram.Profile.ProfileActivity;
import com.example.phong.instagram.Profile.ViewCommentActivity;
import com.example.phong.instagram.Profile.ViewProfileActivity;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.Heart;
import com.example.phong.instagram.Utils.SquareImageView;
import com.example.phong.instagram.model.Like;
import com.example.phong.instagram.model.Photo;
import com.example.phong.instagram.model.User;
import com.example.phong.instagram.model.UserAccountSetting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by phong on 10/28/2017.
 */

public class MainFeedAdapter extends RecyclerView.Adapter<MainFeedAdapter.RecyclerViewHolder> {


    private static final String TAG = "MainFeedAdapter";

    private DatabaseReference mReference;
    private FirebaseDatabase mFirebaseDatabase;
    private User currentUser;

    private ArrayList<Photo> listData = new ArrayList<>();
    private Activity mContext;

    public MainFeedAdapter(ArrayList<Photo> listData, Activity context) {
        this.listData = listData;
        this.mContext = context;
    }

    @Override
    public MainFeedAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemview = inflater.inflate(R.layout.layout_item_mainfeed_recycleview, parent, false);
        return new RecyclerViewHolder(itemview);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {

        holder.heart = new Heart(holder.imgLike, holder.imgUnlike);
        holder.imgLike.setVisibility(View.GONE);
        holder.photo = getItem(position);
        holder.mGestureDetector = new GestureDetector(mContext, new SingleTapConfirm(holder));
        holder.mUser = new StringBuilder();


        //set current user
        getCurrentUser();
        //set string
        getLikesString(holder);

        //set view comment

        if(holder.photo.getComments().size() > 0){
            holder.txtViewComment.setText("View all " + holder.photo.getComments().size() + " comment");
        }else {
            holder.txtViewComment.setText("");
            RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.txtViewCaption_main);
            params.setMargins(0,15,0,0);
            holder.txtDate.setLayoutParams(params);
        }

        holder.txtViewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(mContext.getString(R.string.photo), holder.photo);
                Intent intent = new Intent(mContext,ViewCommentActivity.class);
                intent.putExtra(mContext.getString(R.string.selected_view_comment),bundle);
                mContext.startActivity(intent);
            }
        });

        holder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(mContext.getString(R.string.photo), holder.photo);
                Intent intent = new Intent(mContext,ViewCommentActivity.class);
                intent.putExtra(mContext.getString(R.string.selected_view_comment),bundle);
                mContext.startActivity(intent);
            }
        });

        //set Time

        String timestampDiffrefence = getTimestampDifference(getItem(position));

        if(!timestampDiffrefence.equals("0")){
            holder.txtDate.setText(timestampDiffrefence + " Day ago");
        }else {
            holder.txtDate.setText("To day");
        }

        //get image photo
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(),holder.imgPhoto);

        //get image profile and user name

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    String Caption = holder.photo.getCaption().toString();
                    String UserName = singleSnapshot.getValue(UserAccountSetting.class).getUsername();

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

                    holder.txtCaption.setText(builder, TextView.BufferType.SPANNABLE);

                    holder.txtUserName.setText(singleSnapshot.getValue(UserAccountSetting.class).getUsername());

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSetting.class).getProfile_photo(),holder.imgProfile);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get user object
        Query querys = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        querys.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    holder.user =  ds.getValue(User.class);

                    holder.txtUserName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String s = holder.photo.getUser_id();

                            if (s.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(mContext.getString(R.string.intent_user), holder.user);
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra(mContext.getString(R.string.search_activity), bundle);
                                Log.d(TAG,"intent data" + intent);
                                mContext.startActivity(intent);
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(mContext.getString(R.string.intent_user), holder.user);
                                Intent intent = new Intent(mContext, ViewProfileActivity.class);
                                intent.putExtra(mContext.getString(R.string.search_activity), bundle);
                                Log.d(TAG,"intent data" + intent);
                                mContext.startActivity(intent);
                            }
                        }
                    });

                    holder.imgProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String s = holder.user.getUser_id();

                            if (s.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(mContext.getString(R.string.intent_user), holder.user);
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra(mContext.getString(R.string.search_activity), bundle);
                                Log.d(TAG,"intent data" + intent);
                                mContext.startActivity(intent);
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(mContext.getString(R.string.intent_user), holder.user);
                                Intent intent = new Intent(mContext, ViewProfileActivity.class);
                                intent.putExtra(mContext.getString(R.string.search_activity), bundle);
                                Log.d(TAG,"intent data" + intent);
                                mContext.startActivity(intent);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Photo getItem(int position) {
        return listData.get(position);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgProfile;
        String likesString;
        TextView txtUserName, txtLike, txtCaption, txtDate, txtViewComment;
        SquareImageView imgPhoto;
        ImageView imgLike, imgUnlike, imgComment;

        UserAccountSetting settings = new UserAccountSetting();
        User user = new User();
        StringBuilder mUser;
        String mLikeString;
        boolean likebyCurrentUser;
        Heart heart;
        GestureDetector mGestureDetector;
        Photo photo;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            imgProfile = (CircleImageView) itemView.findViewById(R.id.profile_photo_main);
            imgPhoto = (SquareImageView) itemView.findViewById(R.id.post_image_main);
            imgLike = (ImageView) itemView.findViewById(R.id.like_main);
            imgUnlike = (ImageView) itemView.findViewById(R.id.dislike_main);
            imgComment = (ImageView) itemView.findViewById(R.id.comment_main);

            txtUserName = (TextView) itemView.findViewById(R.id.user_name_main);
            txtCaption = (TextView) itemView.findViewById(R.id.txtViewCaption_main);
            txtLike = (TextView) itemView.findViewById(R.id.txtViewLike_main);
            txtDate = (TextView) itemView.findViewById(R.id.txtViewTimePost_main);
            txtViewComment = (TextView) itemView.findViewById(R.id.txtViewComment_main);
        }
    }

    public class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        RecyclerViewHolder mHolder;

        public SingleTapConfirm(RecyclerViewHolder holder) {
            this.mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Like(mHolder);
            return true;
        }

    }



    private void Like(final RecyclerViewHolder mHolder){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(mHolder.photo.getUser_id())
                .child(mHolder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                    mReference = mFirebaseDatabase.getReference();

                    String keyID = singleSnapshot.getKey();
                    //case 1: then user already liked photo
                    if (mHolder.likebyCurrentUser &&
                            singleSnapshot.getValue(Like.class).getUser_id()
                                    .equals(mHolder.photo.getUser_id())) {
                        mReference.child(mContext.getString(R.string.dbname_photos))
                                .child(mHolder.photo.getPhoto_id())
                                .child(mContext.getString(R.string.field_likes))
                                .child(keyID)
                                .removeValue();

                        mReference.child(mContext.getString(R.string.dbname_user_photos))
                                .child(mHolder.photo.getUser_id())
                                .child(mHolder.photo.getPhoto_id())
                                .child(mContext.getString(R.string.field_likes))
                                .child(keyID)
                                .removeValue();

                        mHolder.heart.toggleLike();
                        getLikesString(mHolder);
                    }
                    //case 2: then user has not liked photo
                    else if (!mHolder.likebyCurrentUser) {
                        //add new like
                        addNewLike(mHolder);
                        break;
                    }
                }
                if (!dataSnapshot.exists()) {
                    //add new like
                    addNewLike(mHolder);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addNewLike(RecyclerViewHolder holder) {

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mReference = mFirebaseDatabase.getReference();

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(holder.photo.getUser_id());
        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getLikesString(final RecyclerViewHolder viewholder) {

        try {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_user_photos))
                    .child(viewholder.photo.getUser_id())
                    .child(viewholder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    viewholder.mUser = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                viewholder.mUser = new StringBuilder();
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    viewholder.mUser.append(singleSnapshot.getValue(User.class).getUsername());
                                    viewholder.mUser.append(",");
                                }

                                String[] spilitUser = viewholder.mUser.toString().split(",");

                                if (viewholder.mUser.toString().contains(currentUser.getUsername() + ",")) {
                                    viewholder.likebyCurrentUser = true;
                                } else {
                                    viewholder.likebyCurrentUser = false;
                                }

                                int length = spilitUser.length;
                                if(length == 0){
                                    viewholder.likesString = "0 Likes";
                                }else if (length == 1) {
                                    viewholder.likesString = "Likes by " + spilitUser[0];
                                } else if (length == 2) {
                                    viewholder.likesString = "Likes by " + spilitUser[0]
                                            + ", " + spilitUser[0];
                                } else if (length == 3) {
                                    viewholder.likesString = "Likes by " + spilitUser[0]
                                            + ", " + spilitUser[1]
                                            + " and " + spilitUser[2];

                                } else if (length == 4) {
                                    viewholder.likesString = "Likes by " + spilitUser[0]
                                            + ", " + spilitUser[1]
                                            + ", " + spilitUser[2]
                                            + " and " + spilitUser[3];

                                } else if (length > 4) {
                                    viewholder.likesString = "Likes by " + spilitUser[0]
                                            + ", " + spilitUser[1]
                                            + ", " + spilitUser[2]
                                            + " and " + (spilitUser.length - 3) + " others";

                                }
                                //setup like string
                                setupLikeString(viewholder);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        viewholder.likebyCurrentUser = false;
                        //setup like string
                        setupLikeString(viewholder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            viewholder.likebyCurrentUser = false;
            //setup like string
            setupLikeString(viewholder);
        }
    }

    private void setupLikeString(final RecyclerViewHolder holder){
        if(holder.likebyCurrentUser){
            holder.imgUnlike.setVisibility(View.GONE);
            holder.imgLike.setVisibility(View.VISIBLE);
            holder.imgLike.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.mGestureDetector.onTouchEvent(event) ;
                }
            });
        }else {
            holder.imgUnlike.setVisibility(View.VISIBLE);
            holder.imgLike.setVisibility(View.GONE);
            holder.imgUnlike.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.mGestureDetector.onTouchEvent(event) ;
                }
            });
        }

        holder.txtLike.setText(holder.likesString);

    }

    private void getCurrentUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUser = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTimestampDifference(Photo photo){
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
}
