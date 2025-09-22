

## 项目简介

### **项目名称**  
Android 端 HeidelTime 时间标签抽取工具  

### **项目概述**  
本项目旨在在 **Android 端侧** 部署并运行 **HeidelTime**（版本 2.2.1），实现对文本中的时间表达式进行自动抽取与标注。  
在实现过程中，针对 HeidelTime 依赖的 **UIMA 框架（uimaj-core 2.2.1）** 进行了大量 **Android 平台适配与裁剪优化**，成功将该桌面端 NLP 工具迁移至 Android 环境运行。  

适配后的版本已验证可支持 **7 种语言**的时间标签抽取：  
`Dutch（荷兰语）、French（法语）、German（德语）、Italian（意大利语）、Portuguese（葡萄牙语）、Russian（俄语）、Spanish（西班牙语）`  
（默认内置英文规则，其他语言需手动配置规则文件）

---

### **功能特点**
- 基于 HeidelTime 时间抽取算法，在 Android 端实时识别文本中的时间表达式，输出标准化时间标签（TimeML 格式等）。
- 深度裁剪 UIMA 框架，解决 Android 环境下的类加载、XML 解析器兼容、资源加载路径等问题，确保运行稳定性。
- 多语言支持，规则可按需扩展，灵活适配不同文本语种。
- 适合在离线场景（无网络）直接运行时间信息解析。

---

### **使用须知**
1. **必需文件**需在 Android 应用的私有目录下提供：  
   路径示例：  
   ```
   /data/data/com.example.heideltime_android/files
   ├─ desc              （来自 HeidelTime 原项目，包含 TypeSystem 等 XML 描述符）
   ├─ english           （默认英文规则文件夹，可替换为其他语言版本）
   ├─ config.props      （HeidelTime 配置文件）
   ```
   > ⚠ 注意：如果需要支持非英文语言，请从 HeidelTime 项目中拷贝对应语言的正则规则文件夹，放置到此目录下。

2. **默认配置**  
   - 项目 resources 中已提供英文等7种语言示例文件，直接运行即可抽取英文时间标签。
   - 如需支持其他语言，请替换/增加对应的规则文件夹，并修改 `config.props` 配置。

3. **文件完整性**  
   - resources 文件夹内所需资源必须保持完整  
   - 无需额外配置路径，系统将按默认规则加载 `files/` 目录中的资源

---

### **目录结构示例**
```
app/src/main/
├─ java/com/example/heideltime_android   (Java/Kotlin 源码)
├─ res                                   (Android 资源文件)
└─ resources                             (示例配置与规则文件)
     ├─ files
        ├─ desc
        ├─ english
        ├─ config.props
     ├─resourceSpecifierSchema.xsd      （uima源文件）
     ├─uima.ecore                       （uima源文件）
     ├─META-INF                         （uima源文件）
     └─org                              （uima源文件内资源文件）
     
```

---

### **成果亮点**
- 成功将 **HeidelTime + UIMA 框架**从 PC/Linux 移植至 Android 环境运行  
- 解决了 UIMA 对标准 JDK XML 解析器的依赖问题，使旧版 Xerces 在 Android 平台可用  
- 支持 7 种语言规则（默认英文），便于扩展到更多语种  
- 在移动端实现对长文本的时间信息提取，为新闻、医疗、法律等领域提供离线时间信息抽取能力  
