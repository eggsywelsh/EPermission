package com.eggsy.epermission;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by eggsy on 16-12-29.
 */

public class PermissionFragmentActivity extends FragmentActivity {

    PermissionFragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment_permission);

        initView();
    }

    private void initView() {
        FragmentManager fm = getSupportFragmentManager();
        if(fm != null){
            FragmentTransaction aboutTrans = fm.beginTransaction();
            mFragment = new PermissionFragment() ;
            mFragment.setArguments(getIntent().getExtras());
            aboutTrans.add(R.id.fl_container, mFragment,PermissionFragment.class.getName()) ;
            aboutTrans.commit() ;
        }
    }
}
