package org.apache.uima.internal.util;

import java.util.UUID;

/**
 * Utility class for generating UUIDs (Android-safe).
 * Replaces java.rmi.VMID + UID with java.util.UUID.
 */
public abstract class UUIDGenerator {

  /**
   * Generates a UUID string.
   *
   * @return the UUID string.
   */
  public static String generate() {
    return UUID.randomUUID().toString();
  }
}