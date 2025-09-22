/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.uima.util.impl;

import android.util.Log;

import org.apache.uima.internal.util.I18nUtil;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * Android 兼容 Logger实现, 替代原 UIMA 的 Log4jLogger_impl
 * 直接映射到 Android Logcat，不依赖 org.apache.log4j
 */
public class Log4jLogger_impl implements Logger {

    private static final String EXCEPTION_MESSAGE = "Exception occurred";

    private String tag = "UIMA";
    private ResourceManager mResourceManager = null;

    private Log4jLogger_impl(Class<?> component) {
        if (component != null) {
            tag = component.getSimpleName();
        }
    }

    private Log4jLogger_impl() {
    }

    public static synchronized Logger getInstance(Class<?> component) {
        return new Log4jLogger_impl(component);
    }

    public static synchronized Logger getInstance() {
        return new Log4jLogger_impl();
    }

    @Override
    public void log(String aMessage) {
        log(Level.INFO, aMessage);
    }

    @Override
    public void log(String aResourceBundleName, String aMessageKey, Object[] aArguments) {
        String msg = I18nUtil.localizeMessage(aResourceBundleName, aMessageKey, aArguments, getExtensionClassLoader());
        log(Level.INFO, msg);
    }

    @Override
    public void logException(Exception aException) {
        log(Level.INFO, EXCEPTION_MESSAGE, aException);
    }

    @Override
    public void setOutputStream(OutputStream out) {
        // Android Logcat 无需此功能
        throw new UnsupportedOperationException("setOutputStream not supported on Android");
    }

    @Override
    public void setOutputStream(PrintStream out) {
        throw new UnsupportedOperationException("setOutputStream not supported on Android");
    }

    @Override
    public boolean isLoggable(Level level) {
        return true; // Android Log 默认总是允许输出
    }

    @Override
    public void setLevel(Level level) {
        // Android Logcat 不维护 level 状态，通过 isLoggable 控制
    }

    @Override
    public void log(Level level, String aMessage) {
        if (aMessage == null || aMessage.isEmpty()) return;
        switch (level.toInteger()) {
            case Level.SEVERE_INT:
                Log.e(tag, aMessage);
                break;
            case Level.WARNING_INT:
                Log.w(tag, aMessage);
                break;
            case Level.INFO_INT:
            case Level.CONFIG_INT:
                Log.i(tag, aMessage);
                break;
            case Level.FINE_INT:
                Log.d(tag, aMessage);
                break;
            default:
                Log.v(tag, aMessage);
        }
    }

    @Override
    public void log(Level level, String aMessage, Object param1) {
        String msg = MessageFormat.format(aMessage, param1);
        log(level, msg);
    }

    @Override
    public void log(Level level, String aMessage, Object[] params) {
        String msg = MessageFormat.format(aMessage, params);
        log(level, msg);
    }

    @Override
    public void log(Level level, String aMessage, Throwable thrown) {
        if (aMessage == null) aMessage = "";
        switch (level.toInteger()) {
            case Level.SEVERE_INT:
                Log.e(tag, aMessage, thrown);
                break;
            case Level.WARNING_INT:
                Log.w(tag, aMessage, thrown);
                break;
            case Level.INFO_INT:
            case Level.CONFIG_INT:
                Log.i(tag, aMessage, thrown);
                break;
            case Level.FINE_INT:
                Log.d(tag, aMessage, thrown);
                break;
            default:
                Log.v(tag, aMessage, thrown);
        }
    }

    @Override
    public void logrb(Level level, String sourceClass, String sourceMethod,
                      String bundleName, String msgKey, Object param1) {
        logrb(level, sourceClass, sourceMethod, bundleName, msgKey, new Object[]{param1});
    }

    @Override
    public void logrb(Level level, String sourceClass, String sourceMethod,
                      String bundleName, String msgKey, Object[] params) {
        String msg = I18nUtil.localizeMessage(bundleName, msgKey, params, getExtensionClassLoader());
        log(level, (sourceClass != null ? sourceClass + "." : "") +
                (sourceMethod != null ? sourceMethod + ": " : "") + msg);
    }

    @Override
    public void logrb(Level level, String sourceClass, String sourceMethod,
                      String bundleName, String msgKey, Throwable thrown) {
        String msg = I18nUtil.localizeMessage(bundleName, msgKey, null, getExtensionClassLoader());
        log(level, (sourceClass != null ? sourceClass + "." : "") +
                (sourceMethod != null ? sourceMethod + ": " : "") + msg, thrown);
    }

    @Override
    public void logrb(Level level, String sourceClass, String sourceMethod,
                      String bundleName, String msgKey) {
        String msg = I18nUtil.localizeMessage(bundleName, msgKey, null, getExtensionClassLoader());
        log(level, (sourceClass != null ? sourceClass + "." : "") +
                (sourceMethod != null ? sourceMethod + ": " : "") + msg);
    }

    @Override
    public void log(String wrapperFQCN, Level level, String message, Throwable thrown) {
        log(level, message, thrown); // wrapperFQCN 在 Android 版里忽略
    }

    @Override
    public void setResourceManager(ResourceManager resourceManager) {
        this.mResourceManager = resourceManager;
    }

    private ClassLoader getExtensionClassLoader() {
        return (mResourceManager != null) ? mResourceManager.getExtensionClassLoader() : null;
    }
}