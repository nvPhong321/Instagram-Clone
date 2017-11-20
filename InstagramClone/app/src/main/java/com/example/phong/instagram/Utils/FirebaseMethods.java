package com.example.phong.instagram.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.phong.instagram.Home.HomeActivity;
import com.example.phong.instagram.Profile.ProfileActivity;
import com.example.phong.instagram.R;
import com.example.phong.instagram.model.Photo;
import com.example.phong.instagram.model.User;
import com.example.phong.instagram.model.UserAccountSetting;
import com.example.phong.instagram.model.UserSetting;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by phong on 8/24/2017.
 */

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethod";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private StorageReference mStorageReference;

    private double mPhotoUploadProgress = 0;
    private String userID;
    private Activity mContext;
    public static ProgressDialog dialog;
    public static SweetAlertDialog pDialog;

    public FirebaseMethods(Activity context){
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        dialog = ProgressDialog.show(mContext, "","Please Wait.." , true);
        pDialog = new SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Please wait");
        pDialog.setCancelable(false);
        pDialog.dismiss();

        dialog.dismiss();
        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for (DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count ++;
        }
        return count;
    }

    public void uploadNewPhoto(String photoType, final String caption, int count, final String imgUrl, Bitmap bm){
        FilePaths filePaths = new FilePaths();
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" +  (count + 1));
            if(bm == null) {
                bm = ImageManager.getBitMap(imgUrl);
            }
            byte [] bytes = ImageManager.getByteFromBitMap(bm,100);
            UploadTask uploadTask = null;
            uploadTask  = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseURI = taskSnapshot.getDownloadUrl();

                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);
                    mContext.finish();
                    pDialog.dismiss();
                    // add the new photo to "photos" node and  'userphotos' node
                    addPhotoToDatabase(caption,firebaseURI.toString());
                    // navigate to the main feed so the user can see their photo
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    pDialog.show();
                }
            });
        }else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");
            if(bm == null) {
                bm = ImageManager.getBitMap(imgUrl);
            }
            byte [] bytes = ImageManager.getByteFromBitMap(bm,100);
            UploadTask uploadTask = null;
            uploadTask  = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseURI = taskSnapshot.getDownloadUrl();

                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    mContext.startActivity(intent);
                    mContext.finish();
                    pDialog.dismiss();
                    // insert into profile photo
                    setProfilePhoto(firebaseURI.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(progress - 15 > mPhotoUploadProgress){
                        pDialog.show();
                    }
                }
            });
        }
    }

    private void setProfilePhoto(String url){
        mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private String getTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd GGG hh:mm aaa", Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }

    public void addPhotoToDatabase(String caption, String imgUrl){

        String tag = StringManupulation.getTags(caption);
        String newPhotoKey = mRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(imgUrl);
        photo.setTag(tag);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        mRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey)
                .setValue(photo);
        mRef.child(mContext.getString(R.string.dbname_photos))
                .child(newPhotoKey)
                .setValue(photo);
    }

    public void updateUserName(String username){
        mRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    public void updateEmail(String email){
        mRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }
    public void updateUserAccountSetting(String description, String website, String displayname, String phonenumber){
        if(displayname != null) {
            mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayname);
        }
        if(website != null) {
            mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }
        if(description != null) {
            mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }
        if(phonenumber != null) {
            mRef.child(mContext.getString(R.string.dbname_users))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phonenumber);
        }
    }

    public void registerNewEmail(final String email, final String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }else if (task.isSuccessful()){
                            sendVerificationEmail();
                            userID = mAuth.getCurrentUser().getUid();
                            Toast.makeText(mContext, R.string.auth_success,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void addNewUser(String email, String username, String description, String website, String profile_photo){
        User user = new User(userID,"1",email,StringManupulation.condenseUsername(username));

        mRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSetting setting = new UserAccountSetting(
                description,
                username,
                0,
                0,
                0,
                profile_photo ,
                StringManupulation.condenseUsername(username),
                website,
                userID
        );

        mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(setting);
    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                    }else {

                    }
                }
            });
        }
    }

    public UserSetting getUserSetting(DataSnapshot dataSnapshot){
        UserAccountSetting userAccountSetting = new UserAccountSetting();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()){
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                try {
                    userAccountSetting.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getDisplay_name()
                    );

                    userAccountSetting.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getUsername()
                    );

                    userAccountSetting.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getWebsite()
                    );

                    userAccountSetting.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getDescription()
                    );

                    userAccountSetting.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getProfile_photo()
                    );

                    userAccountSetting.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getPosts()
                    );

                    userAccountSetting.setFollowings(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getFollowings()
                    );

                    userAccountSetting.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getFollowers()
                    );
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSetting: NullPointerException" + e.getMessage());
                }
            }

            //user node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_users))){
                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );

                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );

                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number()
                );

                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
            }
        }

        return new UserSetting(user,userAccountSetting);
    }

}
