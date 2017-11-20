package com.example.phong.instagram.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.core.deps.guava.collect.Ordering;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.phong.instagram.Adapter.MainFeedAdapter;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.BottomNavigationViewHelper;
import com.example.phong.instagram.model.Comment;
import com.example.phong.instagram.model.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.example.phong.instagram.Home.HomeActivity.mProgressBar;
import static com.example.phong.instagram.Home.HomeActivity.viewPager;

/**
 * Created by phong on 8/12/2017.
 */

public class HomeFragment extends Fragment {
    public TabLayout tabLayout;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private static final int ACTIVITY_NUM = 0;

    private ArrayList<Photo> mPhoto;
    private ArrayList<String> mFollowing;
    private RecyclerView rcMainFeed;
    private MainFeedAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        mPhoto = new ArrayList<>();
        mFollowing = new ArrayList<>();

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);

        rcMainFeed = (RecyclerView) view.findViewById(R.id.rcMainFeed);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcMainFeed.setLayoutManager(layoutManager);

        getFollowing();

        setupBottomNavigationView();
        setupViewPaper();

        return view;
    }

    private void getFollowing(){

        DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();
        Query jquery = dbreference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        jquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mFollowing.add(ds.child(getString(R.string.field_user_id)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                mProgressBar.setVisibility(View.GONE);
                mProgressBar.hide();
                //get photo
                getPhoto();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPhoto(){

        DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();

        for (int i = 0 ; i < mFollowing.size() ; i++) {
            final int count = i;
            Query jquery = dbreference.child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            jquery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Photo mPhotos = new Photo();
                        Map<String, Object> objectsMap = (HashMap<String, Object>) snapshot.getValue();
                        mPhotos.setCaption(objectsMap.get(getString(R.string.field_caption)).toString());
                        mPhotos.setTag(objectsMap.get(getString(R.string.field_tags)).toString());
                        mPhotos.setPhoto_id(objectsMap.get(getString(R.string.field_photo_id)).toString());
                        mPhotos.setUser_id(objectsMap.get(getString(R.string.field_user_id)).toString());
                        mPhotos.setDate_created(objectsMap.get(getString(R.string.field_date_created)).toString());
                        mPhotos.setImage_path(objectsMap.get(getString(R.string.field_image_path)).toString());

                        ArrayList<Comment> mComments = new ArrayList<Comment>();

                        for (DataSnapshot ds : snapshot
                                .child(getActivity().getString(R.string.field_comment)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(ds.getValue(Comment.class).getUser_id());
                            comment.setComment(ds.getValue(Comment.class).getComment());
                            comment.setDate_created(ds.getValue(Comment.class).getDate_created());
                            comment.setComment_id(ds.getValue(Comment.class).getComment_id());
                            mComments.add(comment);
                        }

                        mPhotos.setComments(mComments);
                        mPhoto.add(mPhotos);
                    }
                    if(count >= mFollowing.size() - 1){
                        //display our photo
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayPhotos(){
        if(mPhoto != null){
            Collections.sort(mPhoto, new Ordering<Photo>() {
                @Override
                public int compare(Photo o1,Photo o2) {
                    return o2.getDate_created().compareTo(o1.getDate_created());
                }
            });
            adapter = new MainFeedAdapter(mPhoto,getActivity());
            rcMainFeed.setAdapter(adapter);
        }
    }
    private void setupViewPaper(){

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_instagram);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_send);
    }

    private void setupBottomNavigationView() {

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(),bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
