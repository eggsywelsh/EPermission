
### EPermission
----------------------------------------------
Base on Java Annotation Processor technology,generate the permission helper code when compile the code to simplfy the permission apply process

#### Import
----------------------------------------------
in you module's build.gradle,add below dependencies
```
dependencies {
    annotationProcessor 'com.eggsy:epermission-processor:1.0.4'
    compile 'com.eggsy:epermission:1.0.4'
    compile 'com.eggsy:epermission-annotations:1.0.4'
}
```

#### How to use
----------------------------------------------
To determine whether the authority has been applied for, return true that applied for, return false that did not get the permission
```
ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
```

To determine whether the last application permission was denied by the user
```
EPermission.shouldShowRequestPermissionRationale(context, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE)
```

Apply for a single permission
```
EPermission.requestPermissions(context, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE);
```

Apply for multiple permissions at the same time
```
EPermission.requestPermissions(context, REQUEST_MULTI_PERMISSON, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS);
```

Handles the permissions callback
```
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
```

Whether the pop-up explanation is needed
```
if (!EPermission.shouldShowRequestPermissionRationale(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE);
}
```
If the user needs to explain the reasons for the application permissions, will automatically callback `@PermissionRationale` annotation corresponding to the` requestCode` and `requestPermission` method

Authorization success and failure callbacks are annotated with `@PermissionGrant` and `@PermissionDeny`, as shown in the example below or sample in the project.

#### Example
----------------------------------------------
We can use EPermission in the Activity or Fragment,such as

**Use in Activity**
```
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
        Toast.makeText(this, "permission READ_EXTERNAL_STORAGE grant", Toast.LENGTH_SHORT).show();
    }

    @PermissionDeny(requestCode = REQUEST_SINGLE_PERMISSON, requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void denyGrantSdcardPermission() {
        Log.d(TAG, "permission READ_EXTERNAL_STORAGE deny");
        Toast.makeText(this, "permission READ_EXTERNAL_STORAGE deny", Toast.LENGTH_SHORT).show();
    }

    @PermissionRationale(requestCode = REQUEST_SINGLE_PERMISSON, requestPermission = Manifest.permission.READ_EXTERNAL_STORAGE)
    public void rationableSdcardPermission() {
        Log.d(TAG, "ask to rationable READ_EXTERNAL_STORAGE permission");
        Toast.makeText(this, "ask to rationable READ_EXTERNAL_STORAGE permission", Toast.LENGTH_SHORT).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    /**
     * below content is process CAMERA permission callback
     */

    @PermissionGrant(requestPermission = Manifest.permission.CAMERA)
    public void grantCameraPermission() {
        Log.d(TAG, "permission CAMERA grant");
        Toast.makeText(this, "permission CAMERA grant", Toast.LENGTH_SHORT).show();
    }

    @PermissionDeny(requestPermission = Manifest.permission.CAMERA)
    public void denyGrantCameraPermission() {
        Log.d(TAG, "permission CAMERA deny");
        Toast.makeText(this, "permission CAMERA deny", Toast.LENGTH_SHORT).show();
    }


    @PermissionRationale(requestPermission = Manifest.permission.CAMERA)
    public void rationableCameraPermission() {
        Log.d(TAG, "ask to rationable CAMERA permission");
        Toast.makeText(this, "ask to rationable CAMERA permission", Toast.LENGTH_SHORT).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.CAMERA);
    }


    /**
     * below content is process READ_CONTACTS permission callback
     */

    @PermissionGrant(requestCode = REQUEST_MULTI_PERMISSON, requestPermission = Manifest.permission.READ_CONTACTS)
    public void grantContactPermission() {
        Log.d(TAG, "permission READ_CONTACTS grant");
        Toast.makeText(this, "permission READ_CONTACTS grant", Toast.LENGTH_SHORT).show();
    }

    @PermissionDeny(requestCode = REQUEST_MULTI_PERMISSON, requestPermission = Manifest.permission.READ_CONTACTS)
    public void denyGrantContactPermission() {
        Log.d(TAG, "permission READ_CONTACTS deny");
        Toast.makeText(this, "permission READ_CONTACTS deny", Toast.LENGTH_SHORT).show();
    }

    @PermissionRationale(requestCode = REQUEST_MULTI_PERMISSON, requestPermission = Manifest.permission.READ_CONTACTS)
    public void rationableContactPermission() {
        Log.d(TAG, "ask to rationable READ_CONTACTS permission");
        Toast.makeText(this, "ask to rationable READ_CONTACTS permission", Toast.LENGTH_SHORT).show();
        EPermission.requestPermissions(this, REQUEST_SINGLE_PERMISSON, Manifest.permission.READ_CONTACTS);
    }

}
```



**Use in fragment**
```
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

```

#### Explain
----------------------------------------------
Annotation of  `@PermissionGrant`,`@PermissionDeny` and `@PermissionRationale` has defaule `requestCode` property,if you don't set the requestCode,the annotation corresponding to method can be handle most requestCode of permission,just like below:
```
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
```

no matter which requestCode you used to request permission CAMERA,the granted callback will invode grantCameraPermission()

#### Proguard
----------------------------------------------
```
-dontwarn com.eggsy.permission.**
-keep class com.eggsy.permission.** {*;}
-keep interface com.eggsy.permission.** { *; }
-keep class **_Proxy { *; }
```
