package com.eggsy.permission;

import com.eggsy.permission.annotation.PermissionDeny;
import com.eggsy.permission.annotation.PermissionGrant;
import com.eggsy.permission.annotation.PermissionRationale;
import com.eggsy.permission.assist.PermissionAssist;
import com.eggsy.permission.util.ClassValidator;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by eggsy on 16-12-9.
 */
public class PermissionProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private Map<String, PermissionAssist> mPermissionMap = new HashMap<>();

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(PermissionDeny.class.getCanonicalName());
        annotataions.add(PermissionGrant.class.getCanonicalName());
        annotataions.add(PermissionRationale.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        mPermissionMap.clear();
        messager.printMessage(Diagnostic.Kind.NOTE, "process permission annotations...");

        if (!processAnnotations(roundEnv, PermissionGrant.class)) return false;
        if (!processAnnotations(roundEnv, PermissionDeny.class)) return false;
        if (!processAnnotations(roundEnv, PermissionRationale.class)) return false;

        if (mPermissionMap != null && mPermissionMap.size() > 0) {
            for (String key : mPermissionMap.keySet()) {
                PermissionAssist proxyInfo = mPermissionMap.get(key);
                try {
                    JavaFile javaFile = JavaFile.builder(proxyInfo.getPackageName(), proxyInfo.generateJavaCode()).build();
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    error(e.getMessage());
                }
            }
        }

        return true;
    }

    private boolean processAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> clazz) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(clazz)) {

            if (!checkMethodValid(annotatedElement, clazz)) return false;

            ExecutableElement annotatedMethod = (ExecutableElement) annotatedElement;
            //class type
            TypeElement classElement = (TypeElement) annotatedMethod.getEnclosingElement();
            //full class name
            String fqClassName = classElement.getQualifiedName().toString();

            PermissionAssist proxyInfo = mPermissionMap.get(fqClassName);
            if (proxyInfo == null) {
                proxyInfo = new PermissionAssist(elementUtils, classElement);
                mPermissionMap.put(fqClassName, proxyInfo);
                proxyInfo.setTypeElement(classElement);
            }

            Annotation annotation = annotatedMethod.getAnnotation(clazz);
            if (annotation instanceof PermissionGrant) {
                int requestCode = ((PermissionGrant) annotation).requestCode();
                String requestPermission = ((PermissionGrant) annotation).requestPermission();
                proxyInfo.putGrantMethod(requestCode, requestPermission, annotatedMethod.getSimpleName().toString());
            } else if (annotation instanceof PermissionDeny) {
                int requestCode = ((PermissionDeny) annotation).requestCode();
                String requestPermission = ((PermissionDeny) annotation).requestPermission();
                proxyInfo.putDenyMethod(requestCode, requestPermission, annotatedMethod.getSimpleName().toString());
            } else if (annotation instanceof PermissionRationale) {
                int requestCode = ((PermissionRationale) annotation).requestCode();
                String requestPermission = ((PermissionRationale) annotation).requestPermission();
                proxyInfo.putRationalMethod(requestCode, requestPermission, annotatedMethod.getSimpleName().toString());
            } else {
                error(annotatedElement, "%s not support .", clazz.getSimpleName());
                return false;
            }
        }

        return true;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void error(String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args));
    }

    private boolean checkMethodValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.METHOD) {
            error(annotatedElement, "%s must be declared on method.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement) || ClassValidator.isAbstract(annotatedElement)) {
            error(annotatedElement, "%s() must can not be abstract or private.", annotatedElement.getSimpleName());
            return false;
        }

        return true;
    }
}
