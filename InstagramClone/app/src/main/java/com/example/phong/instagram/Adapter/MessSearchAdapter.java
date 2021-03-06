package com.example.phong.instagram.Adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.phong.instagram.R;
import com.example.phong.instagram.model.User;
import com.example.phong.instagram.model.UserAccountSetting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by phong on 10/20/2017.
 */

public class MessSearchAdapter extends ArrayAdapter<User>{

    private Context mContext;
    private LayoutInflater inflater;
    private int layoutID;
    private ArrayList<User> listData = new ArrayList<>();
    public MessSearchAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<User> objects) {
        super(context, resource, objects);
        this.mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutID = resource;
        this.listData = objects;
    }

    public static class ViewHolder{
        TextView txtUserName,txtDisplayName;
        CircleImageView imgProfile;
        User user;
    }

    public User getItem(int position){

        return listData.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(layoutID,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.user = getItem(position);
            viewHolder.txtUserName = (TextView) convertView.findViewById(R.id.txtUserNameSearch);
            viewHolder.txtDisplayName = (TextView) convertView.findViewById(R.id.txtDislayNameSearch);
            viewHolder.imgProfile =  (CircleImageView) convertView.findViewById(R.id.imgProfileSearch);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtUserName.setText(viewHolder.user.getUsername());
        viewHolder.txtDisplayName.setText(viewHolder.user.getEmail());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(viewHolder.user.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ImageLoader imgLoader = ImageLoader.getInstance();
                    imgLoader.displayImage(ds.getValue(UserAccountSetting.class).getProfile_photo(),
                            viewHolder.imgProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return convertView;
    }
}
