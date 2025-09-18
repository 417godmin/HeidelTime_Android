package org.apache.uima.internal.util;

import java.io.*;
import java.util.*;

import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.CASSerializer;

public final class SerializationUtils {

  private static final Set<Class<?>> CAS_MGR_SERIALIZER_SAFE_CLASSES;
  private static final Set<Class<?>> CAS_SERIALIZER_SAFE_CLASSES;
  private static final Set<Class<?>> CAS_COMPLETE_SERIALIZER_SAFE_CLASSES;

  static {
    Set<Class<?>> tmp = new HashSet<>();
    tmp.add(CASMgrSerializer.class);
    tmp.add(String.class);
    CAS_MGR_SERIALIZER_SAFE_CLASSES = Collections.unmodifiableSet(tmp);

    tmp = new HashSet<>();
    tmp.add(CASSerializer.class);
    tmp.add(String.class);
    CAS_SERIALIZER_SAFE_CLASSES = Collections.unmodifiableSet(tmp);

    tmp = new HashSet<>();
    tmp.add(CASCompleteSerializer.class);
    tmp.add(String.class);
    tmp.add(CASMgrSerializer.class);
    tmp.add(CASSerializer.class);
    CAS_COMPLETE_SERIALIZER_SAFE_CLASSES = Collections.unmodifiableSet(tmp);
  }

  private SerializationUtils() {}

  public static byte[] serialize(Serializable aObject) throws IOException {
    if (aObject == null) return null;
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
    objStream.writeObject(aObject);
    objStream.flush();
    return byteStream.toByteArray();
  }

  public static Object deserialize(byte[] aBytes) throws IOException, ClassNotFoundException {
    Object object = deserialize(aBytes, unionSets(CAS_SERIALIZER_SAFE_CLASSES,
            CAS_COMPLETE_SERIALIZER_SAFE_CLASSES,
            CAS_MGR_SERIALIZER_SAFE_CLASSES));
    if (object != null &&
            !(object instanceof CASMgrSerializer
                    || object instanceof CASSerializer
                    || object instanceof CASCompleteSerializer)) {
      throw new IOException("Unexpected object type: [" + object.getClass().getName() + "]");
    }
    return object;
  }

  public static CASCompleteSerializer deserializeCASCompleteSerializer(byte[] aBytes) throws IOException {
    Object object = deserialize(aBytes, CAS_COMPLETE_SERIALIZER_SAFE_CLASSES);
    if (object != null && !(object instanceof CASCompleteSerializer)) {
      throw new IOException("Unexpected object type: [" + object.getClass().getName() + "]");
    }
    return (CASCompleteSerializer) object;
  }

  // 其他方法类似，调用deserialize(byte[], Set)即可

  private static <T> T deserialize(byte[] aBytes, Set<Class<?>> allowList) throws IOException {
    if (aBytes == null) return null;
    ByteArrayInputStream is = new ByteArrayInputStream(aBytes);
    return deserialize(is, allowList);
  }

  @SuppressWarnings("unchecked")
  private static <T> T deserialize(InputStream aIs, Set<Class<?>> allowList) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(aIs);
    try {
      Object obj = ois.readObject(); // 去掉 ObjectInputFilter
      if (!allowList.contains(obj.getClass())) {
        throw new IOException("Deserialization of class " + obj.getClass().getName() + " not allowed");
      }
      return (T) obj;
    } catch (ClassNotFoundException e) {
      throw new IOException("Unexpected deserialization error", e);
    }
  }

  @SafeVarargs
  private static Set<Class<?>> unionSets(Set<Class<?>>... sets) {
    Set<Class<?>> result = new HashSet<>();
    for (Set<Class<?>> s : sets) result.addAll(s);
    return Collections.unmodifiableSet(result);
  }
  /**
   * 反序列化 CASSerializer 或 CASCompleteSerializer（Android 兼容版）
   */
  public static Object deserializeCASSerializerOrCASCompleteSerializer(InputStream aIs)
          throws IOException {
    // 合并允许的类型集合
    Set<Class<?>> allowed = new HashSet<>();
    allowed.addAll(CAS_SERIALIZER_SAFE_CLASSES);
    allowed.addAll(CAS_COMPLETE_SERIALIZER_SAFE_CLASSES);

    Object object = deserialize(aIs, allowed);

    if (object != null &&
            !(object instanceof CASSerializer || object instanceof CASCompleteSerializer)) {
      throw new IOException("Unexpected object type: [" + object.getClass().getName() + "]");
    }

    return object;
  }
  /**
   * 从输入流反序列化 CASMgrSerializer（Android 兼容版）
   */
  public static CASMgrSerializer deserializeCASMgrSerializer(InputStream aIs) throws IOException {
    Object object = deserialize(aIs, CAS_MGR_SERIALIZER_SAFE_CLASSES);
    if (object != null && !(object instanceof CASMgrSerializer)) {
      throw new IOException("Unexpected object type: [" + object.getClass().getName() + "]");
    }
    return (CASMgrSerializer) object;
  }
}