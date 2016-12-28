package com.eggsy.epermission;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eggsy.permission.EPermission;
import com.eggsy.permission.annotation.PermissionDeny;
import com.eggsy.permission.annotation.PermissionGrant;
import com.eggsy.permission.annotation.PermissionRationale;

/**
 * Created by eggsy on 16-12-27.
 */

public class PermissionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "permission";

    private static final int REQUEST_SINGLE_PERMISSON = 1;
    private static final int REQUEST_MULTI_PERMISSON = 2;

    Button mBtnRequestSinglePermission;
    Button mBtnRequestMultiPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        initView();
    }

    private void initView() {
        mBtnRequestSinglePermission = (Button) findViewById(R.id.btn_request_single_permission);
        mBtnRequestMultiPermission = (Button) findViewById(R.id.btn_request_multi_permission);

        mBtnRequestSinglePermission.setOnClickListener(this);
        mBtnRequestMultiPermission.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_multi_permission:
                EPermission.requestPermissions(this, REQUEST_MULTI_PERMISSON, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE);
                break;
            case R.id.btn_request_single_permission:
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

    @PermissionGrant(requestCode = REQUEST_SINGLE_PERMISSON, requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void grantSdcardPermission() {
        Log.d(TAG, "read external storage grant");
        Toast.makeText(this, "read external storage grant", Toast.LENGTH_LONG).show();
    }

    @PermissionDeny(requestCode = REQUEST_SINGLE_PERMISSON, requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void denyGrantSdcardPermission() {
        Log.d(TAG, "read external storage deny");
        Toast.makeText(this, "read external storage deny", Toast.LENGTH_LONG).show();
    }

    @PermissionRationale(requestCode = REQUEST_SINGLE_PERMISSON, requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void rationableSdcardPermission() {
        Log.d(TAG, "ask to rationable external storage permission");
        Toast.makeText(this, "ask to rationable external storage permission", Toast.LENGTH_LONG).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    @PermissionGrant(requestPermission = Manifest.permission.CAMERA)
    public void grantCameraPermission() {
        Log.d(TAG, "read external storage grant");
        Toast.makeText(this, "read external storage grant", Toast.LENGTH_LONG).show();
    }

    @PermissionDeny(requestPermission = Manifest.permission.CAMERA)
    public void denyGrantCameraPermission() {
        Log.d(TAG, "read external storage deny");
        Toast.makeText(this, "read external storage deny", Toast.LENGTH_LONG).show();
    }

    @PermissionRationale(requestPermission = Manifest.permission.CAMERA)
    public void rationableCameraPermission() {
        Log.d(TAG, "ask to rationable external storage permission");
        Toast.makeText(this, "ask to rationable external storage permission", Toast.LENGTH_LONG).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.CAMERA);
    }

}
