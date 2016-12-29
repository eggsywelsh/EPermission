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
     * save request permission and request code's relationship
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

    /**
     * proxy method info
     */
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
        String className = classElement.getSimpleName().toString();
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
     * create Class Object
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
        return generateCommonMethod(GRANT_METHOD,grantMethodMap);
    }

    private MethodSpec generateDenyMethod() {
        return generateCommonMethod(DENY_METHOD,denyMethodMap);
    }

    private MethodSpec generateRationaleMethod() {
        return generateCommonMethod(RATIONALE_METHOD,rationaleMap);
    }

    private MethodSpec generateNeedRationaleMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(CHECK_RATIONALE_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(ParameterSpec.builder(TypeVariableName.get("int"), "requestCode").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"), "requestPermission").build())
        ;
        if (rationaleMap != null && rationaleMap.size() > 0) {
            builder.addStatement("boolean result = false");
            builder.beginControlFlow("switch(requestPermission)");

            for (Map.Entry<String, HashMap<Integer, ProxyMethodInfo>> grantMethod : rationaleMap.entrySet()) {
                HashMap<Integer, ProxyMethodInfo> proxyMethods = grantMethod.getValue();
                if (proxyMethods != null && proxyMethods.size() > 0) {
                    String requestPermission = grantMethod.getKey();

                    builder.beginControlFlow("case \"" + requestPermission + "\":");

                    ProxyMethodInfo defaultProxyMethodInfo = null;

                    boolean isFirst = true;
                    for (Map.Entry<Integer, ProxyMethodInfo> entry : proxyMethods.entrySet()) {
                        ProxyMethodInfo proxyMethodInfo = entry.getValue();

                        if(proxyMethodInfo.requestCode == DEFAULT_REQUEST_CODE) {
                            defaultProxyMethodInfo = proxyMethodInfo;
                        }else{
                            generateJudgeRationaleRequestCode(builder,proxyMethodInfo,false,isFirst);
                            isFirst = false;
                        }
                    }
                    if(defaultProxyMethodInfo!=null){
                        generateJudgeRationaleRequestCode(builder,defaultProxyMethodInfo,true,isFirst);
                    }

                    builder.addStatement("break");
                    builder.endControlFlow();
                }
            }

            builder.endControlFlow();
        }
        builder.addStatement("return result");
        return builder.build();
    }

    private MethodSpec generateCommonMethod(String methodName,HashMap<String, HashMap<Integer, ProxyMethodInfo>> methodMap){
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeVariableName.get(typeElement.getSimpleName().toString()), "source").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("int"), "requestCode").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"), "requestPermission").build())
                .returns(void.class);

        if (methodMap != null && methodMap.size() > 0) {
            builder.beginControlFlow("switch(requestPermission)");

            for (Map.Entry<String, HashMap<Integer, ProxyMethodInfo>> grantMethod : methodMap.entrySet()) {
                HashMap<Integer, ProxyMethodInfo> proxyMethods = grantMethod.getValue();
                if (proxyMethods != null && proxyMethods.size() > 0) {
                    String requestPermission = grantMethod.getKey();
                    builder.beginControlFlow("case \"" + requestPermission + "\":");

                    ProxyMethodInfo defaultProxyMethodInfo = null;

                    boolean isFirst = true;
                    for (Map.Entry<Integer, ProxyMethodInfo> entry : proxyMethods.entrySet()) {
                        ProxyMethodInfo proxyMethodInfo = entry.getValue();

                        if(proxyMethodInfo.requestCode == DEFAULT_REQUEST_CODE){
                            defaultProxyMethodInfo = proxyMethodInfo;
                        }else{
                            generateJudgeRequestCode(builder,proxyMethodInfo,false,isFirst);
                            isFirst = false;
                        }
                    }
                    if(defaultProxyMethodInfo!=null){
                        generateJudgeRequestCode(builder,defaultProxyMethodInfo,true,isFirst);
                    }
                    builder.addStatement("break");
                    builder.endControlFlow();
                }
            }

            builder.endControlFlow();
        }

        return builder.build();
    }

    private void generateJudgeRequestCode(MethodSpec.Builder builder,ProxyMethodInfo proxyMethodInfo,boolean isDefault,boolean isFirst){
        if(isDefault){
            if(isFirst){
                builder.addStatement("source." + proxyMethodInfo.methodName + "()");
            }else{
                builder.beginControlFlow("else");
                builder.addStatement("source." + proxyMethodInfo.methodName + "()");
                builder.endControlFlow();
            }
        }else{
            builder.beginControlFlow((isFirst ? "" : "else ") + "if(requestCode==" + proxyMethodInfo.requestCode + ")");
            builder.addStatement("source." + proxyMethodInfo.methodName + "()");
            builder.endControlFlow();
        }
    }

    private void generateJudgeRationaleRequestCode(MethodSpec.Builder builder,ProxyMethodInfo proxyMethodInfo,boolean isDefault,boolean isFirst){
        if(isDefault){
            if(isFirst){
                builder.addStatement("result = true");
            }else{
                builder.beginControlFlow("else");
                builder.addStatement("result = true");
                builder.endControlFlow();
            }
        }else{
            builder.beginControlFlow((isFirst ? "" : "else ") + "if(requestCode==" + proxyMethodInfo.requestCode + ")");
            builder.addStatement("result = true");
            builder.endControlFlow();
        }
    }

}
