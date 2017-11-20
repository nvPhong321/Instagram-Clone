package com.example.phong.instagram.Register;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * Created by phong on 8/22/2017.
 */

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;

    private String append = "";
    private static final String TAG = "LoginActivity";
    private FirebaseMethods firebaseMethods;

    String email,password,username;
    TextView txtWait;
    AVLoadingIndicatorView avLoadingIndicatorView;
    AppCompatButton btnRegister;
    EditText edtEmail,edtPassword,edtFullName;
    final int color = Color.parseColor("#bfbfbf");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseMethods = new FirebaseMethods(this);
        btnRegister = (AppCompatButton) findViewById(R.id.btn_register);
        edtEmail = (EditText) findViewById(R.id.input_email);
        edtPassword = (EditText) findViewById(R.id.input_password);
        edtFullName = (EditText) findViewById(R.id.input_username);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.rotateloading);
        txtWait = (TextView) findViewById(R.id.txtPLZ);

        txtWait.setVisibility(View.GONE);
        avLoadingIndicatorView.hide();

        Register();
        setupFirebaseAuth();
    }

    private void Register(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = edtEmail.getText().toString();
                password = edtPassword.getText().toString();
                username = edtFullName.getText().toString();
                if(checkInput(email,password,username)) {
                    txtWait.setVisibility(View.VISIBLE);
                    avLoadingIndicatorView.show();
                    btnRegister.setBackgroundResource(R.drawable.border_login_click);
                    firebaseMethods.registerNewEmail(email, password, username);
               }
            }
        });
    }

    private boolean checkInput(String email, String password, String username){
        if (email.equals("") || password.equals("") || username.equals("")){
            return false;
        }
        return true;
    }


    private boolean isStringNull(String string) {
        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /* ------------------------- Fire Base ----------------------------*/
    private void checkIfUserNameExist(String userName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query= reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(userName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        append = mRef.push().getKey().substring(3,10);
                    }
                }

                String mUserName = "";
                mUserName = username + append;

                firebaseMethods.addNewUser(email,mUserName,"","","");

                mAuth.signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void setupFirebaseAuth() {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            checkIfUserNameExist(username);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
