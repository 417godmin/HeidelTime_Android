package org.apache.uima.pear.tools;

import java.io.File;
import java.net.URL;

/**
 * Android 兼容版 PackageSelector，无 Swing 依赖
 * 原版用于弹出 Swing 文件选择器，在 Android 中不可用
 */
public class PackageSelectorGUI implements InstallationController.PackageSelector {

  public PackageSelectorGUI() {
    // Android 兼容版不需要 JFrame
  }

  @Override
  public synchronized File selectPackageDirectory(String componentId) {
    // 在 Android 中无法弹出对话框，这里直接返回 null 或预设路径
    return null;
  }

  @Override
  public synchronized File selectPackageFile(String componentId) {
    // 在 Android 中无法弹出对话框，这里直接返回 null 或预设路径
    return null;
  }

  @Override
  public URL selectPackageUrl(String componentId) {
    // 原方法未实现，这里也返回 null
    return null;
  }
}