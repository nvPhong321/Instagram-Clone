package com.example.phong.instagram.Dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phong.instagram.R;

/**
 * Created by phong on 8/28/2017.
 */

public class ConfirmPasswordDialog extends DialogFragment {

    private static final String TAG = "ConfirmPasswordDialog";
    private TextView txtConfirm,txtCancel;
    private EditText edtConfirmPassword;
    final int color = Color.parseColor("#bfbfbf");

    public interface OnConfirmPasswordListener{
        public void onConfirmPassword(String password);
    }

    OnConfirmPasswordListener mOnConfirmPasswordListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password,container,false);

        edtConfirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
        txtConfirm = (TextView) view.findViewById(R.id.dialogConfirm);
        txtCancel = (TextView) view.findViewById(R.id.dialogCancel);

        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String password = edtConfirmPassword.getText().toString();
                if(!password.equals("")) {
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    txtConfirm.setTextColor(color);
                    getDialog().dismiss();
                }else {
                    Toast.makeText(getActivity(),"you must enter a password",Toast.LENGTH_SHORT).show();
                }

            }
        });

        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtCancel.setTextColor(color);
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.e(TAG,"classcastexception" + e.getMessage());
        }
    }
}
