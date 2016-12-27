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
    private HashMap<String, String> grantMethodMap = new HashMap<>();
    private HashMap<String, String> denyMethodMap = new HashMap<>();
    private HashMap<String, String> rationaleMap = new HashMap<>();

    private final static String SUFFIX = "Proxy";
    private final static String GRANT_METHOD = "grant";
    private final static String DENY_METHOD = "deny";
    private final static String RATIONALE_METHOD = "rationale";
    private final static String CHECK_RATIONALE_METHOD = "needShowRationale";

    public PermissionAssist(Elements elementUtils, TypeElement classElement)
    {
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        //classname
        classElement.getSimpleName();
        String className = classElement.getSimpleName().toString(); // ClassValidator.getClassName(classElement, packageName);
        this.packageName = packageName;
        this.proxyClassSimpleName = className + "_" + SUFFIX;
    }

    public void putGrantMethod(String requestSign, String methodName) {
        grantMethodMap.put(requestSign, methodName);
    }

    public void putDenyMethod(String requestSign, String methodName) {
        denyMethodMap.put(requestSign, methodName);
    }

    public void putRationalMethod(String requestSign, String methodName) {
        rationaleMap.put(requestSign, methodName);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public String getProxyClassFullName()
    {
        return packageName + "." + proxyClassSimpleName;
    }

    /**
     * 生成类对象
     * @return
     */
    public TypeSpec generateJavaCode(){
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(proxyClassSimpleName);

        classBuilder
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(
                ParameterizedTypeName.get(ClassName.bestGuess("com.eggsy.processor.internal.PermissionProxy"),
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

    private MethodSpec generateGrantMethod(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder(GRANT_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeVariableName.get(typeElement.getSimpleName().toString()),"source").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"),"requestCode").build())
                .returns(void.class)
                ;
        if(grantMethodMap!=null && grantMethodMap.size()>0){
            builder.beginControlFlow("switch(requestCode)");

            for(Map.Entry<String,String> grantMethod : grantMethodMap.entrySet()){
                String requestSign = grantMethod.getKey();
                String methodName = grantMethod.getValue();
                builder.beginControlFlow("case \""+requestSign+"\":")
                        .addStatement("source."+methodName+"()");
                builder.endControlFlow();
            }
            builder.endControlFlow();
        }

        return builder.build();
    }

    private MethodSpec generateDenyMethod(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder(DENY_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeVariableName.get(typeElement.getSimpleName().toString()),"source").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"),"requestCode").build())
                .returns(void.class)
        ;
        if(denyMethodMap!=null && denyMethodMap.size()>0){
            builder.beginControlFlow("switch(requestCode)");

            for(Map.Entry<String,String> grantMethod : denyMethodMap.entrySet()){
                String requestSign = grantMethod.getKey();
                String methodName = grantMethod.getValue();
                builder.beginControlFlow("case \""+requestSign+"\":")
                        .addStatement("source."+methodName+"()");
                builder.endControlFlow();
            }
            builder.endControlFlow();
        }

        return builder.build();
    }

    private MethodSpec generateRationaleMethod(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder(RATIONALE_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(TypeVariableName.get(typeElement.getSimpleName().toString()),"source").build())
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"),"requestCode").build())
                .returns(void.class)
        ;
        if(rationaleMap!=null && rationaleMap.size()>0){
            builder.beginControlFlow("switch(requestCode)");

            for(Map.Entry<String,String> grantMethod : rationaleMap.entrySet()){
                String requestSign = grantMethod.getKey();
                String methodName = grantMethod.getValue();
                builder.beginControlFlow("case \""+requestSign+"\":")
                        .addStatement("source."+methodName+"()");
                builder.endControlFlow();
            }
            builder.endControlFlow();
        }

        return builder.build();
    }

    private MethodSpec generateNeedRationaleMethod(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder(CHECK_RATIONALE_METHOD);
        builder.addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(ParameterSpec.builder(TypeVariableName.get("String"),"requestCode").build())
        ;
        if(rationaleMap!=null && rationaleMap.size()>0){
            builder.beginControlFlow("switch(requestCode)");

            for(Map.Entry<String,String> grantMethod : rationaleMap.entrySet()){
                String requestSign = grantMethod.getKey();
                builder.beginControlFlow("case \""+requestSign+"\":")
                        .addStatement("return true");
                builder.endControlFlow();
            }
            builder.endControlFlow();
        }
        builder.addStatement("return false");
        return builder.build();
    }

}
