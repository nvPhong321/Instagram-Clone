package com.example.phong.instagram.Profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phong.instagram.Dialog.ConfirmPasswordDialog;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Share.ShareActivity;
import com.example.phong.instagram.Utils.FirebaseMethods;
import com.example.phong.instagram.Utils.UniversalImageLoader;
import com.example.phong.instagram.model.User;
import com.example.phong.instagram.model.UserAccountSetting;
import com.example.phong.instagram.model.UserSetting;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by phong on 8/12/2017.
 */

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener {

    private static final String TAG = "EditProfileFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;
    private Dialog dialog;
    private UserSetting mUserSetting;
    private Bitmap bmp;
    Intent intent;

    private EditText mDisplayName, mUserName, mWebsite, mDescription, mPhoneNumber, mEmail;
    private TextView mChangeProfilePhoto;
    private LinearLayout mGallery,mCamera;

    private CircleImageView profilePhoto;
    private ImageView back, saveChange;

    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUserName = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mEmail = (EditText) view.findViewById(R.id.email);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);

        profilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        saveChange = (ImageView) view.findViewById(R.id.saveChange);
        back = (ImageView) view.findViewById(R.id.backArrow);

        mFirebaseMethods = new FirebaseMethods(getActivity());
        setupFirebaseAuth();
        Button();
        return view;
    }

    private void Button() {

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settupDialog();
            }
        });

        saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileSetting();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private void settupDialog(){
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose_editprofile);
        dialog.getWindow().setBackgroundDrawableResource(R.color.trans);
        mGallery = (LinearLayout) dialog.findViewById(R.id.editgallery);
        mCamera  = (LinearLayout) dialog.findViewById(R.id.editcamera);
        dialog.show();

        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
        });

        mCamera.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(getActivity(), AccountSettingActivity.class);
                intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile));
                startActivity(intent);
                getActivity().finish();
            }catch (NullPointerException e){

            }
        }
    }

    private void saveProfileSetting() {
        final String displayName = mDisplayName.getText().toString();
        final String userName = mUserName.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final String phoneNumber = mPhoneNumber.getText().toString();

        if (!mUserSetting.getUser().getUsername().equals(userName)) {
            // check username
            checkIfUserNameExist(userName);
        }

        if (!mUserSetting.getUser().getEmail().equals(email)) {

            //check email
            //step 1: confirm the password and email
            ConfirmPasswordDialog confirmPasswordDialog = new ConfirmPasswordDialog();
            confirmPasswordDialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            confirmPasswordDialog.setTargetFragment(EditProfileFragment.this, 1);
            //step 2: check if email already is registered
            //step 3: change the email
        }
        if(!mUserSetting.getUserAccountSetting().getDescription().equals(description)){
            mFirebaseMethods.updateUserAccountSetting(description,null,null,null);
        }
        if(!mUserSetting.getUserAccountSetting().getDisplay_name().equals(displayName)){
            mFirebaseMethods.updateUserAccountSetting(null,null,displayName,null);
        }
        if(!mUserSetting.getUserAccountSetting().getWebsite().equals(website)){
            mFirebaseMethods.updateUserAccountSetting(null,website,null,null);
        }
        if(!mUserSetting.getUser().getPhone_number().equals(phoneNumber)){
            mFirebaseMethods.updateUserAccountSetting(null,null,null,phoneNumber);
        }
    }

    private void checkIfUserNameExist(final String username) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mFirebaseMethods.updateUserName(username);
                    Toast.makeText(getActivity(), "Save username", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Toast.makeText(getActivity(), "That usename already exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onConfirmPassword(String password) {

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");

                            mAuth.fetchProvidersForEmail(mEmail.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                                        if(task.isSuccessful()){
                                            try{
                                                if(task.getResult().getProviders().size() == 1){
                                                    Toast.makeText(getActivity(), "That email already use", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(), "Email update", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                                }
                                            }catch (NullPointerException e){
                                                Log.e(TAG,"NullPointerException"+e.getMessage());
                                            }
                                        }

                                    }
                                });

                        }else {
                            Log.d(TAG, "re-authenticated failed.");
                        }
                    }
                });
    }

    private void setProfileWidgets(UserSetting userSetting) {
        mUserSetting = userSetting;
        User user = userSetting.getUser();
        UserAccountSetting setting = userSetting.getUserAccountSetting();

        UniversalImageLoader.setImage(setting.getProfile_photo(), profilePhoto, null, "");
        intent = new Intent();
        if(intent.hasExtra(getString(R.string.selected_bitmaps))) {
            String imagePath = getActivity().getIntent().getStringExtra(getString(R.string.selected_bitmaps));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            bmp = BitmapFactory.decodeFile(imagePath);
            profilePhoto.setImageBitmap(bmp);
        }

        mDisplayName.setText(setting.getDisplay_name());
        mUserName.setText(setting.getUsername());
        mWebsite.setText(setting.getWebsite());
        mDescription.setText(setting.getDescription());
        mEmail.setText(user.getEmail());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));
    }

     /* ------------------------- Fire Base ----------------------------*/

    private void setupFirebaseAuth() {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();

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
                setProfileWidgets(mFirebaseMethods.getUserSetting(dataSnapshot));
                //retrieve image for the user in question
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
