/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.internal.util;

import java.util.UUID;

/**
 * Android 与 Java SE 兼容的 UUID 生成工具
 * 原实现依赖 java.rmi.dgc.VMID 和 java.rmi.server.UID
 * 已改为使用 java.util.UUID
 */
public abstract class UUIDGenerator {

  /**
   * 生成唯一 ID
   * @return 唯一字符串
   */
  public static String generate() {
    // 类似原先的 mHostId + UID 结构，这里用 UUID 替代
    return mHostId + UUID.randomUUID().toString().replace("-", "_");
  }

  private static String mHostId;

  static {
    /*
     * 原版 mHostId 从 VMID (包含主机地址) 里取，这里无法获取 VMID，
     * 改为使用一个随机 UUID 前缀模拟 host id，保证同一 JVM 生命周期 mHostId 固定
     */
    mHostId = UUID.randomUUID().toString().substring(0, 8) + ":";
  }
}