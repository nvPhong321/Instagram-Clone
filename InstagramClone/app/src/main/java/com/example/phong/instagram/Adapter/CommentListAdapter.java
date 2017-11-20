package com.example.phong.instagram.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.Heart;
import com.example.phong.instagram.model.Comment;
import com.example.phong.instagram.model.UserAccountSetting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by phong on 10/8/2017.
 */

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater layoutInflater;
    private Context mContext;
    private int layoutResource;

    public CommentListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private class ViewHolder{
        TextView txtComment,txtTimeTamp,txtLike,txtReply;
        CircleImageView profileImage;
        ImageView like,dislike;
        Heart mHeart;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null){
            convertView = layoutInflater.inflate(layoutResource,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.txtComment = (TextView) convertView.findViewById(R.id.txtComment);
            viewHolder.txtLike = (TextView) convertView.findViewById(R.id.txtLikeComment);
            viewHolder.txtReply = (TextView) convertView.findViewById(R.id.txtReplyComment);
            viewHolder.txtTimeTamp = (TextView) convertView.findViewById(R.id.txtTimeComment);
            viewHolder.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_profile_image);
            viewHolder.like = (ImageView) convertView.findViewById(R.id.imgLikeComment);
            viewHolder.dislike = (ImageView) convertView.findViewById(R.id.imgDislikeComment);

            viewHolder.mHeart = new Heart(viewHolder.dislike,viewHolder.like);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //set time comment
        String timesTampDifference = getTimestampDifference(getItem(position));
        if(!timesTampDifference.equals("0")){
            viewHolder.txtTimeTamp.setText(timesTampDifference + " d");
        }else {
            viewHolder.txtTimeTamp.setText("today");
        }
        //set profile image and username and comment
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    String UserName = singleSnapshot.getValue(UserAccountSetting.class).getUsername();
                    String Comment = getItem(position).getComment() ;

                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    SpannableString str1= new SpannableString(UserName.toString() + " ");
                    str1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str1.length(), 0);
                    str1.setSpan(new StyleSpan(Typeface.BOLD),0,str1.length(),0);
                    builder.append(str1);

                    if(Comment.toString().startsWith("#") || Comment.toString().startsWith("@")) {
                        SpannableString str2 = new SpannableString(Comment.toString());
                        str2.setSpan(new ForegroundColorSpan(Color.parseColor("#285480")), 0, str2.length(), 0);
                        builder.append(str2);
                    }else {
                        SpannableString str2 = new SpannableString(Comment.toString());
                        str2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str2.length(), 0);
                        builder.append(str2);
                    }

                    viewHolder.txtComment.setText(builder, TextView.BufferType.SPANNABLE);

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSetting.class).getProfile_photo(),
                            viewHolder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            if (position == 0){
                viewHolder.like.setVisibility(View.GONE);
                viewHolder.dislike.setVisibility(View.GONE);
                viewHolder.txtReply.setVisibility(View.GONE);
                viewHolder.txtLike.setVisibility(View.GONE);
            }
        }catch (NullPointerException e){

        }

        viewHolder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.imgLikeComment:
                        viewHolder.mHeart.toggleLike();
                        break;
                    default:
                        break;
                }
            }
        });

        viewHolder.dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.imgDislikeComment:
                        viewHolder.mHeart.toggleLike();
                        break;
                    default:
                        break;
                }
            }
        });


        return convertView;
    }

    private String getTimestampDifference(Comment comment){
        Log.d(TAG,"getTimestampDifference");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd GGG hh:mm aaa", Locale.getDefault());
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        }catch (ParseException e){
            difference = "0";
        }
        return difference;
    }
}
