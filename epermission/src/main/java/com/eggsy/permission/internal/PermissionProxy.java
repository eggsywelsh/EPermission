package com.eggsy.permission.internal;

/**
 * Created by eggsy on 16-12-9.
 */

public interface PermissionProxy<T> {

    void grant(T source, int requestCode, String requestPermission);

    void deny(T source, int requestCode, String requestPermission);

    void rationale(T source, int requestCode, String requestPermission);

    boolean needShowRationale(int requestCode, String requestPermission);

}
