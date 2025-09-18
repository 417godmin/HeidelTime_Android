## 一阶段预研完毕：问题总结

1. **新版 UIMA 源码在 Android 端的兼容性问题**  
   - 解决内容：`NPE`、缺失类裁剪等问题已彻底解决。

2. **UIMA 2.8.1 版本下 HeidelTime 项目的本地 Eclipse 部署**  
   - 实现功能实时校验。

3. **HeidelTime 针对 Android 环境的兼容性修改**  
   - 去除 `args4j`  
   - 接口替换（去掉命令行调用方式）

4. **主要问题**  
   - UIMA 2.8.1 与新版 UIMA 的非兼容特性导致 HeidelTime 无法正常运行。
