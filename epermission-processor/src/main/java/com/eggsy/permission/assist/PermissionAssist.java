package com.eggsy.permission.assist;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by eggsy on 16-12-9.
 */

public class PermissionAssist {

    private String packageName;
    private String proxyClassSimpleName;
    private TypeElement typeElement;

    /**
     * 存放requestCode与原始方法名的对应关系
     */
    private HashMap<String, HashMap<Integer, ProxyMethodInfo>> grantMethodMap = new HashMap<>();
    private HashMap<String, HashMap<Integer, ProxyMethodInfo>> denyMethodMap = new HashMap<>();
    private HashMap<String, HashMap<Integer, ProxyMethodInfo>> rationaleMap = new HashMap<>();

    private static final int DEFAULT_REQUEST_CODE = Integer.MIN_VALUE;

    private final static String SUFFIX = "Proxy";
    private final static String GRANT_METHOD = "grant";
    private final static String DENY_METHOD = "deny";
    private final static String RATIONALE_METHOD = "rationale";
    private final static String CHECK_RATIONALE_METHOD = "needShowRationale";

    private class ProxyMethodInfo {
        String requestPermission;

        int requestCode;

        String methodName;

        String getRequestSign() {
            return requestPermission + "_" + requestCode;
        }
    }

    public PermissionAssist(Elements elementUtils, TypeElement classElement) {
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        //classname
        classElement.getSimpleName();
        String className = classElement.getSimpleName().toString(); // ClassValidator.getClassName(classElement, packageName);
        this.packageName = packageName;
        this.proxyClassSimpleName = className + "_" + SUFFIX;
    }

    public void putGrantMethod(int requestCode, String requestPermission, String methodName) {
        ProxyMethodInfo proxyMethodInfo = new ProxyMethodInfo();
        proxyMethodInfo.requestPermission = requestPermission;
        proxyMethodInfo.requestCode = requestCode;
        proxyMethodInfo.methodName = methodName;
        HashMap<Integer, ProxyMethodInfo> proxyMethodMap = grantMethodMap.get(requestPermission);
        if (proxyMethodMap == null) {
            proxyMethodMap = new HashMap<>();
        }
        proxyMethodMap.put(requestCode, proxyMethodInfo);
        grantMethodMap.put(proxyMethodInfo.requestPermission, proxyMethodMap);
    }

    public void putDenyMethod(int requestCode, String requestPermission, String methodName) {
        ProxyMethodInfo proxyMethodInfo = new ProxyMethodInfo();
        proxyMethodInfo.requestPermission = requestPermission;
        proxyMethodInfo.requestCode = requestCode;
        proxyMethodInfo.methodName = methodName;
        HashMap<Integer, ProxyMethodInfo> proxyMethodMap = denyMethodMap.get(requestPermission);
        if (proxyMethodMap == null) {
            proxyMethodMap = new HashMap<>();
        }
        proxyMethodMap.put(requestCode, proxyMethodInfo);
        denyMethodMap.put(proxyMethodInfo.requestPermission, proxyMethodMap);
    }

    public void putRationalMethod(int requestCode, String requestPermission, String methodName) {
        ProxyMethodInfo proxyMethodInfo = new ProxyMethodInfo();
        proxyMethodInfo.requestPermission = requestPermission;
        proxyMethodInfo.requestCode = requestCode;
        proxyMethodInfo.methodName = methodName;
        HashMap<Integer, ProxyMethodInfo> proxyMethodMap = rationaleMap.get(requestPermission);
        if (proxyMethodMap == null) {
            proxyMethodMap = new HashMap<>();
        }
        proxyMethodMap.put(requestCode, proxyMethodInfo);
        rationaleMap.put(proxyMethodInfo.requestPermission, proxyMethodMap);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public String getProxyClassFullName() {
        return packageName + "." + proxyClassSimpleName;
    }

    /**
     * 生成类对象
     *
     * @return
     */
    public TypeSpec generateJavaCode() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(proxyClassSimpleName);

        classBuilder
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(
                        ParameterizedTypeName.get(ClassName.bestGuess("com.eggsy.permission.internal.PermissionProxy"),
                                TypeVariableName.get(typeElement.getSimpleName().toString())));

        /**
         * 生成grant方法
         */
        MethodSpec grantMs = generateGrantMethod();
        MethodSpec denyMs = generateDenyMethod();
        MethodSpec rationalMs = generateRationaleMethod();
        MethodSpec needRationalMs = generateNeedRationaleMethod();

        classBuilder.addMethod(grantMs);
        classBuilder.addMethod(denyMs);
        classBuilder.addMethod(rationalMs);
        classBuilder.addMethod(needRationalMs);

        return classBuilder.build();
    }

    private MethodSpec generateGrantMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(GRANT_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeVariableName.get(typeElement.getSimpleName().toString()), "source").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("int"), "requestCode").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"), "requestPermission").build())
                .returns(void.class);

        if (grantMethodMap != null && grantMethodMap.size() > 0) {
            builder.beginControlFlow("switch(requestPermission)");

            for (Map.Entry<String, HashMap<Integer, ProxyMethodInfo>> grantMethod : grantMethodMap.entrySet()) {
                HashMap<Integer, ProxyMethodInfo> proxyMethods = grantMethod.getValue();
                if (proxyMethods != null && proxyMethods.size() > 0) {
                    String requestPermission = grantMethod.getKey();
                    builder.beginControlFlow("case \"" + requestPermission + "\":");

                    ProxyMethodInfo proxy = null;

                    for (Map.Entry<Integer, ProxyMethodInfo> entry : proxyMethods.entrySet()) {
                        ProxyMethodInfo proxyMethodInfo = entry.getValue();

                        if (proxyMethodInfo.requestCode == DEFAULT_REQUEST_CODE) {
                            proxy = proxyMethodInfo;
                        }else{
                            builder.beginControlFlow("if(requestCode==" + proxyMethodInfo.requestCode + ")");
                            builder.addStatement("source." + proxyMethodInfo.methodName + "()");
                            builder.addStatement("break");
                            builder.endControlFlow();
                        }
                    }

                    if (proxy != null && proxy.requestCode == DEFAULT_REQUEST_CODE) {
                        builder.beginControlFlow("if(requestCode==" + proxy.requestCode + ")");
                        builder.addStatement("source." + proxy.methodName + "()");
                        builder.addStatement("break");
                        builder.endControlFlow();
                    }

                    builder.endControlFlow();
                }
            }

            builder.endControlFlow();
        }

        return builder.build();
    }

    private MethodSpec generateDenyMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(DENY_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeVariableName.get(typeElement.getSimpleName().toString()), "source").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("int"), "requestCode").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"), "requestPermission").build())
                .returns(void.class);

        if (denyMethodMap != null && denyMethodMap.size() > 0) {
            builder.beginControlFlow("switch(requestPermission)");

            for (Map.Entry<String, HashMap<Integer, ProxyMethodInfo>> grantMethod : denyMethodMap.entrySet()) {
                HashMap<Integer, ProxyMethodInfo> proxyMethods = grantMethod.getValue();
                if (proxyMethods != null && proxyMethods.size() > 0) {
                    String requestPermission = grantMethod.getKey();
                    builder.beginControlFlow("case \"" + requestPermission + "\":");

                    ProxyMethodInfo proxy = null;

                    for (Map.Entry<Integer, ProxyMethodInfo> entry : proxyMethods.entrySet()) {
                        ProxyMethodInfo proxyMethodInfo = entry.getValue();

                        if (proxyMethodInfo.requestCode == DEFAULT_REQUEST_CODE) {
                            proxy = proxyMethodInfo;
                        }else{
                            builder.beginControlFlow("if(requestCode==" + proxyMethodInfo.requestCode + ")");
                            builder.addStatement("source." + proxyMethodInfo.methodName + "()");
                            builder.addStatement("break");
                            builder.endControlFlow();
                        }
                    }

                    if (proxy != null && proxy.requestCode == DEFAULT_REQUEST_CODE) {
                        builder.beginControlFlow("if(requestCode==" + proxy.requestCode + ")");
                        builder.addStatement("source." + proxy.methodName + "()");
                        builder.addStatement("break");
                        builder.endControlFlow();
                    }

                    builder.endControlFlow();
                }
            }

            builder.endControlFlow();
        }

        return builder.build();
    }

    private MethodSpec generateRationaleMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(RATIONALE_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeVariableName.get(typeElement.getSimpleName().toString()), "source").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("int"), "requestCode").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"), "requestPermission").build())
                .returns(void.class);

        if (rationaleMap != null && rationaleMap.size() > 0) {
            builder.beginControlFlow("switch(requestPermission)");

            for (Map.Entry<String, HashMap<Integer, ProxyMethodInfo>> grantMethod : rationaleMap.entrySet()) {
                HashMap<Integer, ProxyMethodInfo> proxyMethods = grantMethod.getValue();
                if (proxyMethods != null && proxyMethods.size() > 0) {
                    String requestPermission = grantMethod.getKey();
                    builder.beginControlFlow("case \"" + requestPermission + "\":");

                    ProxyMethodInfo proxy = null;

                    for (Map.Entry<Integer, ProxyMethodInfo> entry : proxyMethods.entrySet()) {
                        ProxyMethodInfo proxyMethodInfo = entry.getValue();

                        if (proxyMethodInfo.requestCode == DEFAULT_REQUEST_CODE) {
                            proxy = proxyMethodInfo;
                        }else{
                            builder.beginControlFlow("if(requestCode==" + proxyMethodInfo.requestCode + ")");
                            builder.addStatement("source." + proxyMethodInfo.methodName + "()");
                            builder.addStatement("break");
                            builder.endControlFlow();
                        }
                    }

                    if (proxy != null && proxy.requestCode == DEFAULT_REQUEST_CODE) {
                        builder.beginControlFlow("if(requestCode==" + proxy.requestCode + ")");
                        builder.addStatement("source." + proxy.methodName + "()");
                        builder.addStatement("break");
                        builder.endControlFlow();
                    }

                    builder.endControlFlow();
                }
            }

            builder.endControlFlow();
        }

        return builder.build();

    }

    private MethodSpec generateNeedRationaleMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(CHECK_RATIONALE_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(ParameterSpec.builder(TypeVariableName.get("int"), "requestCode").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"), "requestPermission").build())
        ;
        if (rationaleMap != null && rationaleMap.size() > 0) {
            builder.beginControlFlow("switch(requestPermission)");

            for (Map.Entry<String, HashMap<Integer, ProxyMethodInfo>> grantMethod : rationaleMap.entrySet()) {
                HashMap<Integer, ProxyMethodInfo> proxyMethods = grantMethod.getValue();
                if (proxyMethods != null && proxyMethods.size() > 0) {
                    String requestPermission = grantMethod.getKey();
                    builder.beginControlFlow("case \"" + requestPermission + "\":");

                    ProxyMethodInfo proxy = null;

                    for (Map.Entry<Integer, ProxyMethodInfo> entry : proxyMethods.entrySet()) {
                        ProxyMethodInfo proxyMethodInfo = entry.getValue();

                        if (proxyMethodInfo.requestCode == DEFAULT_REQUEST_CODE) {
                            proxy = proxyMethodInfo;
                        }else{
                            builder.beginControlFlow("if(requestCode==" + proxyMethodInfo.requestCode + ")");
                            builder.addStatement("return true");
                            builder.endControlFlow();
                        }
                    }

                    if (proxy != null && proxy.requestCode == DEFAULT_REQUEST_CODE) {
                        builder.beginControlFlow("if(requestCode==" + proxy.requestCode + ")");
                        builder.addStatement("return true");
                        builder.endControlFlow();
                    }

                    builder.endControlFlow();
                }
            }

            builder.endControlFlow();
        }
        builder.addStatement("return false");
        return builder.build();
    }

}
