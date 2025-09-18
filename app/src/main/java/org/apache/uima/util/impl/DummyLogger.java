package org.apache.uima.util.impl;

import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.slf4j.Marker;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;

/**
 * 一个空实现 Logger，屏蔽所有日志调用
 * 在 Android 上避免 UIMA 初始化时空指针
 */
public class DummyLogger implements Logger {

    public static Logger getInstance() {
        return new DummyLogger();
    }

    public static Logger getInstance(Class<?> clazz) {
        return new DummyLogger();
    }

    // ---------- UIMA Logger 接口实现 ----------
    @Override public void log(String aMessage) {}
    @Override public void log(String aResourceBundleName, String aMessageKey, Object[] aArguments) {}
    @Override public void logException(Exception aException) {}
    @Override public void setOutputStream(PrintStream aStream) {}
    @Override public void setOutputStream(OutputStream aStream) {}
    @Override public void log(Level level, String aMessage) {}
    @Override public void log(Level level, String aMessage, Object param1) {}
    @Override public void log(Level level, String aMessage, Object[] params) {}
    @Override public void log(Level level, String aMessage, Throwable thrown) {}
    @Override public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey) {}
    @Override public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey, Object param1) {}
    @Override public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey, Object[] params) {}
    @Override public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey, Throwable thrown) {}
    @Override public void log(String wrapperFQCN, Level level, String message, Throwable thrown) {}
    @Override public boolean isLoggable(Level level) { return false; }
    @Override public boolean isLoggable(Level level, Marker marker) { return false; }
    @Override public void setLevel(Level level) {}
    @Override public void setResourceManager(ResourceManager resourceManager) {}
    @Override public String rb(String resourceBundle, String key, Object... params) { return key; }
    @Override public boolean isAnnotatorLogger() { return false; }

    // ---------- 新增的 Supplier API ----------
    @Override public void debug(Supplier<String> msgSupplier) {}
    @Override public void debug(Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void debug(Marker marker, String message, Supplier<?>... paramSuppliers) {}
    @Override public void debug(String message, Supplier<?>... paramSuppliers) {}
    @Override public void debug(Marker marker, Supplier<String> msgSupplier) {}
    @Override public void debug(Marker marker, Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void error(Supplier<String> msgSupplier) {}
    @Override public void error(Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void error(Marker marker, String message, Supplier<?>... paramSuppliers) {}
    @Override public void error(String message, Supplier<?>... paramSuppliers) {}
    @Override public void error(Marker marker, Supplier<String> msgSupplier) {}
    @Override public void error(Marker marker, Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void info(Supplier<String> msgSupplier) {}
    @Override public void info(Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void info(Marker marker, String message, Supplier<?>... paramSuppliers) {}
    @Override public void info(Marker marker, Supplier<String> msgSupplier) {}
    @Override public void info(Marker marker, Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void trace(Supplier<String> msgSupplier) {}
    @Override public void trace(Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void trace(Marker marker, String message, Supplier<?>... paramSuppliers) {}
    @Override public void trace(String message, Supplier<?>... paramSuppliers) {}
    @Override public void trace(Marker marker, Supplier<String> msgSupplier) {}
    @Override public void trace(Marker marker, Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void warn(Supplier<String> msgSupplier) {}
    @Override public void warn(Supplier<String> msgSupplier, Throwable throwable) {}
    @Override public void warn(Marker marker, String message, Supplier<?>... paramSuppliers) {}
    @Override public void warn(String message, Supplier<?>... paramSuppliers) {}
    @Override public void warn(Marker marker, Supplier<String> msgSupplier) {}
    @Override public void warn(Marker marker, Supplier<String> msgSupplier, Throwable throwable) {}

    // ---------- org.slf4j.Logger 接口一堆方法的空实现 ----------
    // 为了简洁，这里直接返回 false 或空实现
    @Override public String getName() { return "DummyLogger"; }
    @Override public boolean isTraceEnabled() { return false; }
    @Override public void trace(String msg) {}
    @Override public void trace(String format, Object arg) {}
    @Override public void trace(String format, Object arg1, Object arg2) {}
    @Override public void trace(String format, Object... arguments) {}
    @Override public void trace(String msg, Throwable t) {}
    @Override public boolean isTraceEnabled(Marker marker) { return false; }
    @Override public void trace(Marker marker, String msg) {}
    @Override public void trace(Marker marker, String format, Object arg) {}
    @Override public void trace(Marker marker, String format, Object arg1, Object arg2) {}
    @Override public void trace(Marker marker, String format, Object... argArray) {}
    @Override public void trace(Marker marker, String msg, Throwable t) {}
    @Override public boolean isDebugEnabled() { return false; }
    @Override public void debug(String msg) {}
    @Override public void debug(String format, Object arg) {}
    @Override public void debug(String format, Object arg1, Object arg2) {}
    @Override public void debug(String format, Object... arguments) {}
    @Override public void debug(String msg, Throwable t) {}
    @Override public boolean isDebugEnabled(Marker marker) { return false; }
    @Override public void debug(Marker marker, String msg) {}
    @Override public void debug(Marker marker, String format, Object arg) {}
    @Override public void debug(Marker marker, String format, Object arg1, Object arg2) {}
    @Override public void debug(Marker marker, String format, Object... arguments) {}
    @Override public void debug(Marker marker, String msg, Throwable t) {}
    @Override public boolean isInfoEnabled() { return false; }
    @Override public void info(String msg) {}
    @Override public void info(String format, Object arg) {}
    @Override public void info(String format, Object arg1, Object arg2) {}
    @Override public void info(String format, Object... arguments) {}
    @Override public void info(String msg, Throwable t) {}
    @Override public boolean isInfoEnabled(Marker marker) { return false; }
    @Override public void info(Marker marker, String msg) {}
    @Override public void info(Marker marker, String format, Object arg) {}
    @Override public void info(Marker marker, String format, Object arg1, Object arg2) {}
    @Override public void info(Marker marker, String format, Object... arguments) {}
    @Override public void info(Marker marker, String msg, Throwable t) {}
    @Override public boolean isWarnEnabled() { return false; }
    @Override public void warn(String msg) {}
    @Override public void warn(String format, Object arg) {}
    @Override public void warn(String format, Object... arguments) {}
    @Override public void warn(String format, Object arg1, Object arg2) {}
    @Override public void warn(String msg, Throwable t) {}
    @Override public boolean isWarnEnabled(Marker marker) { return false; }
    @Override public void warn(Marker marker, String msg) {}
    @Override public void warn(Marker marker, String format, Object arg) {}
    @Override public void warn(Marker marker, String format, Object arg1, Object arg2) {}
    @Override public void warn(Marker marker, String format, Object... arguments) {}
    @Override public void warn(Marker marker, String msg, Throwable t) {}
    @Override public boolean isErrorEnabled() { return false; }
    @Override public void error(String msg) {}
    @Override public void error(String format, Object arg) {}
    @Override public void error(String format, Object arg1, Object arg2) {}
    @Override public void error(String format, Object... arguments) {}
    @Override public void error(String msg, Throwable t) {}
    @Override public boolean isErrorEnabled(Marker marker) { return false; }
    @Override public void error(Marker marker, String msg) {}
    @Override public void error(Marker marker, String format, Object arg) {}
    @Override public void error(Marker marker, String format, Object arg1, Object arg2) {}
    @Override public void error(Marker marker, String format, Object... arguments) {}
    @Override public void error(Marker marker, String msg, Throwable t) {}
}