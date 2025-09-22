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

package org.apache.uima.internal.util;


import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ManagementObject;
import org.apache.uima.util.Level;

/**
 * Utility class for registering MBeans with a JMX MBeanServer. This allows AnalysisEngine
 * performance stats to be monitored through JMX, for example.
 */
public class JmxMBeanAgent {
  /**
   * Register an MBean with the MBeanServer.
   * 
   * @param aMBean
   *          the MBean to register
   * @param aMBeanServerO
   *          server to register with. If null, the platform MBeanServer will be used if we are
   *          running under Java 1.5. Earlier versions of Java did not have a platform MBeanServer;
   *          in that case, this method will do nothing.
   */
  public static void registerMBean(ManagementObject aMBean, Object aMBeanServerO) {
    // Android 无 JMX，直接忽略
  }

  /**
   * Unregister an MBean from the MBeanServer.
   * 
   * @param aMBean
   *          the MBean to register
   * @param aMBeanServerO
   *          server to unregister from. If null, the platform MBeanServer will be used if we are
   *          running under Java 1.5. Earlier versions of Java did not have a platform MBeanServer;
   *          in that case, this method will do nothing.
   */
  public static void unregisterMBean(ManagementObject aMBean, Object aMBeanServerO) {
    // Android 无 JMX，直接忽略
  }

  /** Class and Method handles for reflection */
//  private static Class mbeanServerClass;
//
//  private static Class objectNameClass;

//  private static Constructor objectNameConstructor;
//
//  private static Method isRegistered;
//
//  private static Method registerMBean;
//
//  private static Method unregisterMBean;

  /**
   * Set to true if we can find the required JMX classes and methods
   */
  private static boolean jmxAvailable = true;

  /**
   * The platform MBean server
   * This is available since Java 1.5
   */
// Android 版本：JMX 不可用
  //private static MBeanServer platformMBeanServer = null;

  /** Get class/method handles */
  /*static {
    jmxAvailable = false; // JMX 不可用，不使用
    platformMBeanServer = null; // 没有 ManagementFactory
  }*/

  /**
   * resource bundle for log messages
   */
  private static final String LOG_RESOURCE_BUNDLE = "org.apache.uima.impl.log_messages";

}
