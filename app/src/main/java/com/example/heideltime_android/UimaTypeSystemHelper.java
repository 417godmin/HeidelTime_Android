package com.example.heideltime_android;

import org.apache.uima.UIMAFramework;
import org.apache.uima.ResourceSpecifierFactory;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl;
import org.apache.uima.cas.CAS;

import java.lang.reflect.Field;
import java.util.Map;

public class UimaTypeSystemHelper {

    @SuppressWarnings("unchecked")
    public static TypeSystemDescription createBaseUimaTypeSystem() {
        // 先补 mInterfaceToClassMap
        try {
            ResourceSpecifierFactory factory = UIMAFramework.getResourceSpecifierFactory();
            Field mapField = factory.getClass().getDeclaredField("mInterfaceToClassMap");
            mapField.setAccessible(true);
            Map<Class<?>, Class<?>> map = (Map<Class<?>, Class<?>>) mapField.get(factory);

            // 使用反射注册映射，避免 import 不存在的类
            putMapping(map,
                    "org.apache.uima.resource.metadata.TypeDescription",
                    "org.apache.uima.resource.metadata.impl.TypeDescription_impl");
            putMapping(map,
                    "org.apache.uima.resource.metadata.FeatureDescription",
                    "org.apache.uima.resource.metadata.impl.FeatureDescription_impl");
            putMapping(map,
                    "org.apache.uima.resource.metadata.TypeSystemDescription",
                    "org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl");
            putMapping(map,
                    "org.apache.uima.resource.metadata.FsIndexDescription",
                    "org.apache.uima.resource.metadata.impl.FsIndexDescription_impl");
            putMapping(map,
                    "org.apache.uima.resource.metadata.FsIndexCollection",
                    "org.apache.uima.resource.metadata.impl.FsIndexCollection_impl");
            putMapping(map,
                    "org.apache.uima.resource.metadata.FsIndexKeyDescription",
                    "org.apache.uima.resource.metadata.impl.FsIndexKeyDescription_impl");
            putMapping(map,
                    "org.apache.uima.resource.metadata.AllowedValue",
                    "org.apache.uima.resource.metadata.impl.AllowedValue_impl");

            // 如果你的版本有其他需要的 mapping，继续用 putMapping(...) 添加
            System.out.println("Has FsIndexCollection? " + map.containsKey(
                    Class.forName("org.apache.uima.resource.metadata.FsIndexCollection")));
            System.out.println("Impl: " + map.get(
                    Class.forName("org.apache.uima.resource.metadata.FsIndexCollection")));
            //查看是否添加好了映射

            System.out.println("[UIMA FIX] Patched mInterfaceToClassMap reflectively, size=" + map.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ===== 下面是你原来的类型系统构造 =====
        TypeSystemDescription ts = new TypeSystemDescription_impl();

        ts.addType(CAS.TYPE_NAME_TOP, "Top type for all CAS types",CAS.TYPE_NAME_TOP );
        ts.addType(CAS.TYPE_NAME_INTEGER, "Integer type", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_FLOAT, "Float type", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_STRING, "String type", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_BOOLEAN, "Boolean type", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_BYTE, "Byte type", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_SHORT, "Short type", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_LONG, "Long type", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_DOUBLE, "Double type", CAS.TYPE_NAME_TOP);

        ts.addType(CAS.TYPE_NAME_ARRAY_BASE, "Base type for all arrays", CAS.TYPE_NAME_TOP);
        ts.addType(CAS.TYPE_NAME_FS_ARRAY, "Array of feature structures", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_INTEGER_ARRAY, "Array of integers", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_FLOAT_ARRAY, "Array of floats", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_STRING_ARRAY, "Array of strings", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_BOOLEAN_ARRAY, "Array of booleans", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_BYTE_ARRAY, "Array of bytes", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_SHORT_ARRAY, "Array of shorts", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_LONG_ARRAY, "Array of longs", CAS.TYPE_NAME_ARRAY_BASE);
        ts.addType(CAS.TYPE_NAME_DOUBLE_ARRAY, "Array of doubles", CAS.TYPE_NAME_ARRAY_BASE);

        // Sofa
        var sofa = ts.addType(CAS.TYPE_NAME_SOFA, "Subject of Analysis", CAS.TYPE_NAME_TOP);
        sofa.addFeature("sofaNum", "Internal ID", CAS.TYPE_NAME_INTEGER);
        sofa.addFeature("sofaID", "Sofa ID", CAS.TYPE_NAME_STRING);
        sofa.addFeature("mimeType", "MIME type", CAS.TYPE_NAME_STRING);
        sofa.addFeature("sofaArray", "Sofa data array", CAS.TYPE_NAME_TOP);
        sofa.addFeature("sofaString", "Sofa data string", CAS.TYPE_NAME_STRING);
        sofa.addFeature("sofaURI", "Sofa external URI", CAS.TYPE_NAME_STRING);

        // AnnotationBase
        var annotationBase = ts.addType(CAS.TYPE_NAME_ANNOTATION_BASE, "Base for all annotations", CAS.TYPE_NAME_TOP);
        annotationBase.addFeature("sofa", "Associated Sofa", CAS.TYPE_NAME_SOFA);

        // Annotation
        var annotation = ts.addType(CAS.TYPE_NAME_ANNOTATION, "Generic text span annotation", CAS.TYPE_NAME_ANNOTATION_BASE);
        annotation.addFeature("begin", "Start position", CAS.TYPE_NAME_INTEGER);
        annotation.addFeature("end", "End position", CAS.TYPE_NAME_INTEGER);

        // DocumentAnnotation
        var documentAnnotation = ts.addType(CAS.TYPE_NAME_DOCUMENT_ANNOTATION, "Document-level metadata", CAS.TYPE_NAME_ANNOTATION);
        documentAnnotation.addFeature("language", "Document language", CAS.TYPE_NAME_STRING);

        return ts;
    }

    private static void putMapping(Map<Class<?>, Class<?>> map, String interfaceName, String implName) {
        try {
            Class<?> intf = Class.forName(interfaceName);
            Class<?> impl = Class.forName(implName);
            map.put(intf, impl);
        } catch (ClassNotFoundException e) {
            // 某个类在你版本中不存在就跳过
        }
    }
}