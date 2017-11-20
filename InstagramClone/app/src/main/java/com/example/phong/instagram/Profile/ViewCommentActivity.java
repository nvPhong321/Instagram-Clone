package com.example.phong.instagram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.phong.instagram.Adapter.CommentListAdapter;
import com.example.phong.instagram.R;
import com.example.phong.instagram.model.Comment;
import com.example.phong.instagram.model.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by phong on 8/12/2017.
 */

public class ViewCommentActivity extends AppCompatActivity {

    private static final String TAG = "ViewCommentActivity";

    private ImageView backArrow,inbox,mSendComment,imgDelete,imgCanCelDel;
    private EditText edtComment;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;

    private ListView mListView;

    private Intent intent;
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private CommentListAdapter adapter;
    private Comment comment;
    private RelativeLayout backViewComment,selectViewComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comment);
        backArrow = (ImageView) findViewById(R.id.backComment);
        inbox = (ImageView) findViewById(R.id.img_sendmess);
        mSendComment = (ImageView) findViewById(R.id.img_sendComment);
        edtComment = (EditText) findViewById(R.id.edtComment);

        backViewComment = (RelativeLayout) findViewById(R.id.deselectComment);
        selectViewComment = (RelativeLayout) findViewById(R.id.selectComment);

        imgDelete = (ImageView) findViewById(R.id.delete);
        imgCanCelDel = (ImageView) findViewById(R.id.backDelComment);

        selectViewComment.setVisibility(View.GONE);

        mListView = (ListView) findViewById(R.id.listComment);
        mComments = new ArrayList<>();
        try{
            mPhoto = getPhoto();

        }catch (NullPointerException e){

        }

        setupFirebaseAuth();

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                mListView.setSelector(R.drawable.list_item_selector);
                comment = mComments.get(position);
                if (backViewComment!=null) {
                    backViewComment.setVisibility(View.GONE);
                    selectViewComment.setVisibility(View.VISIBLE);
                    imgDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mRef.child(getString(R.string.dbname_user_photos))
                                    .child(mPhoto.getUser_id())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_comment))
                                    .child(comment.getComment_id())
                                    .removeValue();
                            mRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_comment))
                                    .child(comment.getComment_id())
                                    .removeValue();
                            mComments.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });

                    imgCanCelDel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListView.setSelector(R.color.trans);
                            backViewComment.setVisibility(View.VISIBLE);
                            selectViewComment.setVisibility(View.GONE);
                        }
                    });

                }
                return false;
            }
        });

    }

    private void addNewComment(String newComment){
        String commentID = mRef.child(this.getString(R.string.field_comment)).push().getKey();
        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimeStamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        comment.setComment_id(commentID);

        mRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comment))
                .child(commentID)
                .setValue(comment);

        mRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comment))
                .child(commentID)
                .setValue(comment);

    }

    private String getTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd GGG hh:mm aaa", Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }

    private void setupWidgets(){
        adapter = new CommentListAdapter(ViewCommentActivity.this, R.layout.layout_item_comment, mComments);
        adapter.notifyDataSetChanged();
        mListView.setAdapter(adapter);

        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtComment.getText().toString().equals("")){
                    addNewComment(edtComment.getText().toString());
                    edtComment.setText("");
                    closeKeyboard();
                }else {
                    Toast.makeText(getApplication(),"you can't post blank comment",Toast.LENGTH_SHORT).show();
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void closeKeyboard(){
        View view = ViewCommentActivity.this.getCurrentFocus();
        if(view!=null){
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    @Nullable
    private Photo getPhoto(){
        intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getBundleExtra(getString(R.string.selected_view_comment));
            if (bundle != null) {
                return bundle.getParcelable(getString(R.string.photo));
            } else {
                return null;
            }
        }else {
            return null;
        }
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

        mComments.clear();
        Comment firstComment = new Comment();

        firstComment.setComment(mPhoto.getCaption());
        firstComment.setUser_id(mPhoto.getUser_id());
        firstComment.setDate_created(mPhoto.getDate_created());
        mComments.add(firstComment);
        mPhoto.setComments(mComments);

        mRef.child(this.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(this.getString(R.string.field_comment))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Query query = mRef
                                .child(getApplication().getString(R.string.dbname_photos))
                                .orderByChild(getApplication().getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                                    Photo photo = new Photo();
                                    Map<String, Object> objectsMap = (HashMap<String, Object>) singleSnapShot.getValue();

                                    photo.setCaption(objectsMap.get(getApplication().getString(R.string.field_caption)).toString());
                                    photo.setTag(objectsMap.get(getApplication().getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectsMap.get(getApplication().getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectsMap.get(getApplication().getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectsMap.get(getApplication().getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectsMap.get(getApplication().getString(R.string.field_image_path)).toString());

                                    mComments.clear();

                                    Comment firstComment = new Comment();

                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());

                                    mComments.add(firstComment);

                                    for (DataSnapshot ds : singleSnapShot
                                            .child(getApplication().getString(R.string.field_comment)).getChildren()) {
                                        Comment comment = new Comment();
                                        comment.setUser_id(ds.getValue(Comment.class).getUser_id());
                                        comment.setComment(ds.getValue(Comment.class).getComment());
                                        comment.setComment_id(ds.getValue(Comment.class).getComment_id());
                                        comment.setDate_created(ds.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                    }

                                    photo.setComments(mComments);

                                    mPhoto = photo;

                                    setupWidgets();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
