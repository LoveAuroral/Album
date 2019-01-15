package com.auroral.album;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import auroral.widget.view.album.utils.AlbumUtils;

import java.util.ArrayList;
import java.util.Arrays;

import auroral.widget.view.camera.util.CameraUtils;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rv_picture;
    private PictureAdapter mPictureAdapter;
    private CameraUtils mCameraUtils;

    private ArrayList<String> mPictures;
    private AlbumUtils mAlbumUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initDataView();
    }

    private void initData() {
        mPictures = new ArrayList<>();
        mCameraUtils = CameraUtils.getInstance(this);
        mCameraUtils.setCameraListener(new CameraUtils.CameraListener() {
            @Override
            public void errorResult(String msg, CameraUtils.ErrorType type) {
                Log.i("AuroralLog", "msg:" + msg);
            }

            @Override
            public void cameraResult(String path, CameraUtils.MediaType type) {
                mPictures.add(path);
                mPictureAdapter.notifyDataSetChanged();
            }
        });
        mAlbumUtils = AlbumUtils.getInstance(this);
        mAlbumUtils.setAlbumListener(new AlbumUtils.AlbumListener() {
            @Override
            public void errorResult(String msg, AlbumUtils.ErrorType type) {
                Log.i("AuroralLog", "msg:" + msg);
            }

            @Override
            public void albumResult(String[] paths, AlbumUtils.MediaType type) {
                mPictures.clear();
                if (paths != null && paths.length > 0) {
                    mPictures.addAll(Arrays.asList(paths));
                }
                mPictureAdapter.notifyDataSetChanged();
            }
        });
        mPictureAdapter = new PictureAdapter(this);
        mPictureAdapter.setOnCameraListener(new PictureAdapter.OnCameraListener() {
            @Override
            public void camera() {
//                mCameraUtils.startCamera();
                mAlbumUtils.bindSelectImages(mPictures);
                mAlbumUtils.startAlbum();
            }
        });
        mPictureAdapter.setPictures(mPictures);
    }

    private void initView() {
        rv_picture = findViewById(R.id.rv_picture);
    }

    private void initDataView() {
        rv_picture.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        rv_picture.setAdapter(mPictureAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCameraUtils.activityResult(requestCode, resultCode, data);
        mAlbumUtils.activityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mCameraUtils.requestPermissionsResult(requestCode, grantResults);
        mAlbumUtils.requestPermissionsResult(requestCode, grantResults);
    }
}
