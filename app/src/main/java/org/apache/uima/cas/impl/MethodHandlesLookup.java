/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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

package org.apache.uima.cas.impl;

//Android原生不支持invoke


// 🚨 删除 java.lang.invoke 相关引用，让 class 在 Android 上可加载
public class MethodHandlesLookup {

  private MethodHandlesLookup() {
    // no-op constructor
  }

  public static Object getMethodHandlesLookup() {
    // 返回 null 或者一个假的对象，FSClassRegistry 会检测到并走反射慢路径
    return null;
  }
}