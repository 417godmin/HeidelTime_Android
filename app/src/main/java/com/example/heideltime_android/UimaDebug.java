package com.example.heideltime_android;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.XMLInputSource;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;

public class UimaDebug {

    public static void printJCasTypeFieldInfo() {
        try {
            // ===== 反射检测（原有代码） =====
            Class<?> clazz1 = Class.forName("org.apache.uima.jcas.tcas.Annotation_Type");
            Field field1 = clazz1.getDeclaredField("jcasType");
            System.out.println("[Reflection] Class: " + clazz1.getName()
                    + " | Field type: " + field1.getType().getName());

            Class<?> clazz2 = Class.forName("org.apache.uima.jcas.cas.AnnotationBase_Type");
            Field field2 = clazz2.getDeclaredField("jcasType");
            System.out.println("[Reflection] Class: " + clazz2.getName()
                    + " | Field type: " + field2.getType().getName());

            // ===== 新增: 从手机内部存储加载 HeidelTime_TypeSystem.xml =====
            String tsPath = "/data/data/com.example.heideltime_android/files/desc/type/HeidelTime_TypeSystem.xml";
            File tsFile = new File(tsPath);
            if (!tsFile.exists()) {
                System.out.println("[TypeSystem] ❌ 文件不存在: " + tsPath);
                return;
            }

            System.out.println("[TypeSystem] 尝试加载: " + tsPath);

            FileInputStream fis = new FileInputStream(tsFile);
            XMLInputSource tsSource = new XMLInputSource(fis, tsFile);
            TypeSystemDescription tsDesc = UIMAFramework
                    .getXMLParser()
                    .parseTypeSystemDescription(tsSource);

            CAS cas = CasCreationUtils.createCas(tsDesc, null, null);

            // 检查 Dct 类型
            String dctTypeName = "de.unihd.dbs.uima.types.heideltime.Dct";
            Type dctType = cas.getTypeSystem().getType(dctTypeName);
            if (dctType == null) {
                System.out.println("[TypeSystem] ❌ 未找到类型: " + dctTypeName);
            } else {
                System.out.println("[TypeSystem] ✅ 找到类型: " + dctType.getName());
                boolean hasValueFeat = false;
                for (Feature f : dctType.getFeatures()) {
                    System.out.println("[TypeSystem]   Feature: " + f.getShortName()
                            + " (" + f.getRange().getName() + ")");
                    if ("value".equals(f.getShortName())) {
                        hasValueFeat = true;
                    }
                }
                if (hasValueFeat) {
                    System.out.println("[TypeSystem] ✅ 已加载 Dct.value 特征");
                } else {
                    System.out.println("[TypeSystem] ⚠ 缺少 Dct.value 特征");
                }
            }

        } catch (ClassNotFoundException e) {
            System.out.println("[Reflection] Class not found: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            System.out.println("[Reflection] Field not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[TypeSystem] 检查时报错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}