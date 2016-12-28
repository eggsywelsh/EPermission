package com.eggsy.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.eggsy.permission.internal.PermissionProxy;
import com.eggsy.permission.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eggsy on 16-12-9.
 */

public class EPermission {

    private static final String SUFFIX = "_Proxy";

    public static void requestPermissions(Activity object, int requestCode, String... permissions) {
        _requestPermissions(object, requestCode, permissions);
    }

    public static void requestPermissions(Fragment object, int requestCode, String... permissions) {
        _requestPermissions(object, requestCode, permissions);
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, int requestCode, String permission) {
        PermissionProxy proxy = findPermissionProxy(activity);
        if (!proxy.needShowRationale(requestCode, permission)) return false;
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                permission)) {
            proxy.rationale(activity, requestCode, permission);
            return true;
        }
        return false;
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private static void _requestPermissions(Object object, int requestCode, String... permissions) {
        if (!Utils.isOverMarshmallow()) {
            doExecuteSuccess(object, requestCode, permissions);
            return;
        }
        List<String> deniedPermissions = Utils.findDenyPermissions(Utils.getActivity(object), permissions);
        List<String> grantPermissions = Utils.findGrantPermissions(Utils.getActivity(object), permissions);

        if (grantPermissions != null && grantPermissions.size() > 0) {
            doExecuteSuccess(object, requestCode, grantPermissions.toArray(new String[grantPermissions.size()]));
        }

        if (deniedPermissions != null && deniedPermissions.size() > 0) {
            if (object instanceof Activity) {
                ((Activity) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else if (object instanceof Fragment) {
                ((Fragment) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported!");
            }
        }
    }


    private static PermissionProxy findPermissionProxy(Object activity) {
        try {
            Class clazz = activity.getClass();
            Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
            return (PermissionProxy) injectorClazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.", activity.getClass().getSimpleName() + SUFFIX));
    }


    private static void doExecuteSuccess(Object activity, int requestCode, String... permissions) {
        if (permissions != null && permissions.length > 0) {
            for (String permission : permissions) {
                findPermissionProxy(activity).grant(activity, requestCode, permission);
            }
        }
    }

    private static void doExecuteFail(Object activity, int requestCode, String... permissions) {
        if (permissions != null && permissions.length > 0) {
            for (String permission : permissions) {
                findPermissionProxy(activity).deny(activity, requestCode, permission);
            }
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions,
                                                  int[] grantResults) {
        requestResult(activity, requestCode, permissions, grantResults);
    }

    public static void onRequestPermissionsResult(Fragment fragment, int requestCode, String[] permissions,
                                                  int[] grantResults) {
        requestResult(fragment, requestCode, permissions, grantResults);
    }

    private static void requestResult(Object obj, int requestCode, String[] permissions,
                                      int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        List<String> grantPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            } else {
                grantPermissions.add(permissions[i]);
            }
        }
        if (grantPermissions != null && grantPermissions.size() > 0) {
            doExecuteSuccess(obj, requestCode, grantPermissions.toArray(new String[deniedPermissions.size()]));
        }

        if (deniedPermissions != null && deniedPermissions.size() > 0) {
            doExecuteFail(obj, requestCode, deniedPermissions.toArray(new String[deniedPermissions.size()]));
        }
    }

    private static String getRequestSign(int requestCode, String requestPermission) {
        return requestPermission + "_" + requestCode;
    }

}
