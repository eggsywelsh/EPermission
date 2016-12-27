package com.eggsy.permission.internal;

/**
 * Created by eggsy on 16-12-9.
 */

public interface PermissionProxy<T> {

    void grant(T source, String requestCode);

    void deny(T source, String requestCode);

    void rationale(T source, String requestCode);

    boolean needShowRationale(String requestCode);

}
