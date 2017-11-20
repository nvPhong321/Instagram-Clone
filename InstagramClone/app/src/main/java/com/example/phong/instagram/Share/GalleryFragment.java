package com.example.phong.instagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.phong.instagram.Adapter.GridImageAdapter;
import com.example.phong.instagram.Profile.AccountSettingActivity;
import com.example.phong.instagram.R;
import com.example.phong.instagram.Utils.FilePaths;
import com.example.phong.instagram.Utils.FileSearch;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by phong on 8/12/2017.
 */

public class GalleryFragment extends Fragment {

    final int color = Color.parseColor("#FFFFFF");
    private GridView gridViewGallery;
    private ImageView back;
    private ImageViewTouch imgGallery;
    private RotateLoading progressbar;
    private Spinner directorySpinner;
    private TextView txtNext;
    private static final int NUM_GRID_COLUMNS = 4;
    private String mAppend = "file:/";

    private ArrayList<String> directories;
    private ArrayList<String> directories1;
    private ArrayList<String> directories2;
    private String selectedImage;
    private int selectedPosition = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        directories = new ArrayList<>();
        directories1 = new ArrayList<>();
        directories2 = new ArrayList<>();
        imgGallery = (ImageViewTouch) view.findViewById(R.id.galleryImageView);
        gridViewGallery = (GridView) view.findViewById(R.id.gridViewGallery);
        progressbar = (RotateLoading) view.findViewById(R.id.rotateloadinggallery);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        back = (ImageView) view.findViewById(R.id.backGallery);
        txtNext = (TextView) view.findViewById(R.id.tvNext);
        imgGallery.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        progressbar.stop();

        Button();
        Init();
        return view;
    }

    private void Button() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        txtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRootTask()) {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), selectedImage);
                    startActivity(intent);
                    getActivity().finish();
                }else {
                    Intent intent = new Intent(getActivity(), AccountSettingActivity.class);
                    intent.putExtra(getString(R.string.selected_image), selectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile));
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
    }

    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void Init() {
        FilePaths filePaths = new FilePaths();
        ArrayList<String> directoriesName = new ArrayList<>();
        directories1 = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        directories = FileSearch.getDirectoryPaths(filePaths.CAMERA);
        directories2.addAll(directories1);
        directories2.addAll(directories);
        for (int i = 0; i < directories2.size(); i++) {
            int index = directories2.get(i).lastIndexOf("/");
            String string = directories2.get(i).substring(index);
            directoriesName.add(string);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, directoriesName);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(arrayAdapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setupGridView(directories2.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setupGridView(String selectDirectory) {

        final ArrayList<String> imgUrl = FileSearch.getFilePaths(selectDirectory);

        //set the grid column width
        final int gridWidth = getResources().getDisplayMetrics().widthPixels;
        final int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridViewGallery.setColumnWidth(imageWidth);
        gridViewGallery.setSelector(new ColorDrawable(Color.TRANSPARENT));
        //use the grid adapter to adapter the image to gridview
        final GridImageAdapter gridImageAdapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imgUrl);
        gridViewGallery.setAdapter(gridImageAdapter);
        try {
            setImage(imgUrl.get(0), imgGallery, mAppend);
            selectedImage = imgUrl.get(0);
        }catch (ArrayIndexOutOfBoundsException e){

        }
        gridViewGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setImage(imgUrl.get(position), imgGallery, mAppend);
                selectedImage = imgUrl.get(position);
            }
        });
    }

    private void setImage(String imgURL, ImageView image, String append) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressbar.start();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressbar.stop();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressbar.stop();
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }
}
