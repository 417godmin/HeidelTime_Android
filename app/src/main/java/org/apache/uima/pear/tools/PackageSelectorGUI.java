package org.apache.uima.pear.tools;

import java.io.File;
import java.net.URL;

public class PackageSelectorGUI implements InstallationController.PackageSelector {

  @Override
  public File selectPackageDirectory(String componentId) {
    // 在 Android 不实现，返回 null 或调用 Android 文件选择器
    return null;
  }

  @Override
  public File selectPackageFile(String componentId) {
    // 在 Android 不实现，返回 null 或调用 Android SAF (Storage Access Framework)
    return null;
  }

  @Override
  public URL selectPackageUrl(String componentId) {
    return null;
  }
}