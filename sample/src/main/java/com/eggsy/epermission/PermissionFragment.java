package com.eggsy.epermission;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.eggsy.permission.EPermission;
import com.permission.annotation.PermissionDeny;
import com.permission.annotation.PermissionGrant;
import com.permission.annotation.PermissionRationale;

/**
 * Created by eggsy on 16-12-29.
 */

public class PermissionFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "permission";

    private static final int REQUEST_SINGLE_PERMISSON = 1;
    private static final int REQUEST_MULTI_PERMISSON = 2;

    private View view;

    Button mBtnRequestSinglePermission;
    Button mBtnRequestMultiPermission;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_permission, container, false);
        initView();
        return view;
    }

    private void initView() {
        mBtnRequestSinglePermission = (Button) view.findViewById(R.id.btn_request_single_permission);
        mBtnRequestMultiPermission = (Button) view.findViewById(R.id.btn_request_multi_permission);

        mBtnRequestSinglePermission.setOnClickListener(this);
        mBtnRequestMultiPermission.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_multi_permission:
                EPermission.requestPermissions(this, REQUEST_MULTI_PERMISSON, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS);
                break;
            case R.id.btn_request_single_permission:
                // judge the request permission should be show the rational explain to the user
                if (!EPermission.shouldShowRequestPermissionRationale(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * below content is process READ_EXTERNAL_STORAGE permission callback
     */

    @PermissionGrant(requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void grantSdcardPermission() {
        Log.d(TAG, "permission READ_EXTERNAL_STORAGE grant");
        Toast.makeText(getActivity(), "permission READ_EXTERNAL_STORAGE grant", Toast.LENGTH_SHORT).show();
    }

    @PermissionDeny(requestCode = REQUEST_SINGLE_PERMISSON, requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void denyGrantSdcardPermission() {
        Log.d(TAG, "permission READ_EXTERNAL_STORAGE deny");
        Toast.makeText(getActivity(), "permission READ_EXTERNAL_STORAGE deny", Toast.LENGTH_SHORT).show();
    }

    @PermissionRationale(requestCode = REQUEST_SINGLE_PERMISSON, requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void rationableSdcardPermission() {
        Log.d(TAG, "ask to rationable READ_EXTERNAL_STORAGE permission");
        Toast.makeText(getActivity(), "ask to rationable READ_EXTERNAL_STORAGE permission", Toast.LENGTH_SHORT).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    /**
     * below content is process CAMERA permission callback
     */

    @PermissionGrant(requestPermission = Manifest.permission.CAMERA)
    public void grantCameraPermission() {
        Log.d(TAG, "permission CAMERA grant");
        Toast.makeText(getActivity(), "permission CAMERA grant", Toast.LENGTH_SHORT).show();
    }

    @PermissionDeny(requestPermission = Manifest.permission.CAMERA)
    public void denyGrantCameraPermission() {
        Log.d(TAG, "permission CAMERA deny");
        Toast.makeText(getActivity(), "permission CAMERA deny", Toast.LENGTH_SHORT).show();
    }


    @PermissionRationale(requestPermission = Manifest.permission.CAMERA)
    public void rationableCameraPermission() {
        Log.d(TAG, "ask to rationable CAMERA permission");
        Toast.makeText(getActivity(), "ask to rationable CAMERA permission", Toast.LENGTH_SHORT).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.CAMERA);
    }


    /**
     * below content is process READ_CONTACTS permission callback
     */

    @PermissionGrant(requestCode = REQUEST_MULTI_PERMISSON, requestPermission = Manifest.permission.READ_CONTACTS)
    public void grantContactPermission() {
        Log.d(TAG, "permission READ_CONTACTS grant");
        Toast.makeText(getActivity(), "permission READ_CONTACTS grant", Toast.LENGTH_SHORT).show();
    }

    @PermissionDeny(requestCode = REQUEST_MULTI_PERMISSON, requestPermission = Manifest.permission.READ_CONTACTS)
    public void denyGrantContactPermission() {
        Log.d(TAG, "permission READ_CONTACTS deny");
        Toast.makeText(getActivity(), "permission READ_CONTACTS deny", Toast.LENGTH_SHORT).show();
    }

    @PermissionRationale(requestCode = REQUEST_MULTI_PERMISSON, requestPermission = Manifest.permission.READ_CONTACTS)
    public void rationableContactPermission() {
        Log.d(TAG, "ask to rationable READ_CONTACTS permission");
        Toast.makeText(getActivity(), "ask to rationable READ_CONTACTS permission", Toast.LENGTH_SHORT).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_CONTACTS);
    }
}
