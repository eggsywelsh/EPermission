package com.eggsy.epermission;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button mBtnToActivity;
    Button mBtnToFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        mBtnToActivity = (Button)findViewById(R.id.btn_next_activity);
        mBtnToFragment = (Button)findViewById(R.id.btn_next_fragment);

        mBtnToActivity.setOnClickListener(this);
        mBtnToFragment.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_next_activity:
                Intent nextActivity = new Intent(MainActivity.this,PermissionActivity.class);
                startActivity(nextActivity);
                break;
            case R.id.btn_next_fragment:

                break;
            default:
                break;
        }
    }
}
