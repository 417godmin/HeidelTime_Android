package com.example.heideltime_android

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.unihd.dbs.heideltime.standalone.HeidelTimeEntry
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.Enumeration


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 调用反射检查
        UimaDebug.printJCasTypeFieldInfo();

        val inputText = findViewById<EditText>(R.id.inputText)
        val btnProcess = findViewById<Button>(R.id.btnProcess)
        val outputText = findViewById<TextView>(R.id.outputText)

        btnProcess.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isEmpty()) {
                outputText.text = "⚠ 请输入一段文本"
                return@setOnClickListener
            }

            Thread {
                try {
                    // === 调试检查文件路径 ===
                    debugCheckHeidelTimeFiles(filesDir)
                    try {
                        val e: Enumeration<URL?> = Thread.currentThread()
                            .getContextClassLoader()
                            .getResources("META-INF/services/javax.xml.parsers.SAXParserFactory")
                        while (e.hasMoreElements()) {
                            println("Found service file: " + e.nextElement())
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                    // 调用 HeidelTime
                    val result = HeidelTimeEntry.processText(
                        text,
                        "ENGLISH",     // 语言
                        "NARRATIVES",  // 文档类型
                        "TIMEML"       // 输出类型
                    )

                    runOnUiThread { outputText.text = result }
                    Log.i("HTResult", result)
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { outputText.text = "❌ 错误: ${e.message}" }
                }
            }.start()
        }
    }

    /**
     * 检查 HeidelTime 运行必须的关键文件是否存在
     */
    private fun debugCheckHeidelTimeFiles(baseDir: File) {
        val importantFiles = listOf(
            "config.props",
            "desc/type/HeidelTime_TypeSystem.xml",
        )

        Log.d("HTDebug", "=== Checking HeidelTime required files ===")
        Log.d("HTDebug", "BaseDir: ${baseDir.absolutePath}")

        for (relPath in importantFiles) {
            val file = File(baseDir, relPath)
            Log.d("HTDebug", "Check: ${file.absolutePath} exists? ${file.exists()}")
        }
        Log.d("HTDebug", "=========================================")
    }
}