package com.lupy.compile;

import com.cylan.annotation.MessageCylan;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @author Lupy
 * @since 2019/3/2
 */
@AutoService(Processor.class)
public class MsgProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Types typeUtils;
    private String modelName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
        Map<String, String> options = processingEnvironment.getOptions();
        if (options == null || options.isEmpty()) {
            throw new IllegalStateException("please configure MODEL_NAME");
        } else {
            modelName = options.get("MODEL_NAME");
            if (modelName == null || modelName.length() == 0) {
                throw new IllegalStateException("please configure MODEL_NAME");
            }
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> support = new HashSet<>();
        support.add(MessageCylan.class.getCanonicalName());
        return support;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(MessageCylan.class);
        if (elementsAnnotatedWith != null && elementsAnnotatedWith.size() != 0) {
            try {
                parseElement(elementsAnnotatedWith);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "generoter error:" + e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    private void parseElement(Set<? extends Element> elements) throws IOException {
        String first = String.valueOf(modelName.charAt(0)).toUpperCase();
        StringBuffer modleTrans = new StringBuffer("Of");
        modleTrans.append(first);
        if (modelName.length() > 1) {
            modleTrans.append(modelName.substring(1));
        }
        modelName = modleTrans.toString();

        //param type
        TypeName paramter = ParameterizedTypeName.get(ClassName.get(HashMap.class), ClassName.get(Integer.class), ClassName.get(Class.class));
        ClassName loaderName = ClassName.get(Constant.PACKAGE_NAME, Constant.CLASS_NAME + modelName);

        //add a map collection
        FieldSpec map = FieldSpec.builder(paramter, Constant.CONTAINER_FIELD_NAME).build();
        FieldSpec signaltonField = FieldSpec.builder(loaderName, Constant.SINGLETON_FIELD)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();

        //add a private constroctor
        MethodSpec constroctor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("$L()", Constant.INIT_METHOD_NAME)
                .build();

        //add a singletonmethod
        MethodSpec signalton = MethodSpec.methodBuilder(Constant.SINGLETON_INIT_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(loaderName)
                .addStatement("if($L == null){\n" +
                        " synchronized ($L.class){\n" +
                        "if($L == null){\n" +
                        "$L = new $L();" +
                        "}" +
                        "}" +
                        "} \n return $L", Constant.SINGLETON_FIELD, Constant.CLASS_NAME + modelName, Constant.SINGLETON_FIELD, Constant.SINGLETON_FIELD, Constant.CLASS_NAME + modelName, Constant.SINGLETON_FIELD)
                .build();

        //add a init method
        MethodSpec.Builder builder = MethodSpec.methodBuilder(Constant.INIT_METHOD_NAME);
        builder.addModifiers(Modifier.PRIVATE);
        builder.addStatement("if($L == null){\n$L = new HashMap<$T,$T>();\n}",
                Constant.CONTAINER_FIELD_NAME,
                Constant.CONTAINER_FIELD_NAME,
                ClassName.get(Integer.class), ClassName.get(Class.class));
        for (Element element : elements) {
            if (!element.getKind().isClass()) {
                throw new IllegalStateException("MessageCylan only be used for class ");
            }

            MessageCylan annotation = element.getAnnotation(MessageCylan.class);
            int messageId = annotation.messageId();
            ClassName elementName = ClassName.get((TypeElement) element);

            if (messageId == -1) {
                throw new IllegalStateException("please configure messageId for " + elementName.simpleName());
            }

            builder.addStatement("$L.put($L,$T.class);", Constant.CONTAINER_FIELD_NAME, messageId, elementName);
        }


        //add getContainerMethod
        MethodSpec getContainer = MethodSpec.methodBuilder(Constant.GET_CONTAINER)
                .addModifiers(Modifier.PUBLIC)
                .returns(paramter)
                .addStatement("return $L",Constant.CONTAINER_FIELD_NAME)
                .build();


        TypeSpec typeSpec = TypeSpec.classBuilder(Constant.CLASS_NAME + modelName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constroctor)
                .addMethod(signalton)
                .addMethod(builder.build())
                .addMethod(getContainer)
                .addField(signaltonField)
                .addField(map)
                .addJavadoc("do not edit")
                .build();

        JavaFile javaFile = JavaFile.builder(Constant.PACKAGE_NAME, typeSpec)
                .build();
        javaFile.writeTo(filer);
    }
}
