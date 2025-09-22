package com.example.heideltime_android

import android.app.Application
import android.util.Log
import javax.xml.parsers.SAXParserFactory

class MyApp : Application() {
    override fun onCreate() {


        try {
            // 强制使用 Xerces（无论是 UIMA 自带的还是手动引入的新版）
            System.setProperty(
                "javax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.jaxp.SAXParserFactoryImpl"
            )
            System.setProperty(
                "javax.xml.parsers.DocumentBuilderFactory",
                "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl"
            )
            Log.i("MyApp", "已使用 UIMA 自带的 Xerces 解析器")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onCreate()
    }

}