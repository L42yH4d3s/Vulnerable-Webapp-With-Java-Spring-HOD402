package com.example.webapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.SerializablePermission;
import java.security.Permission;

class CustomSecurityManager extends SecurityManager
{
  @Override
  public void checkPermission(Permission perm)
  {
    if (perm instanceof SerializablePermission)
    {
      if ("enableSubclassImplementation".equals(perm.getName())) {throw new SecurityException("Deserialization subclass implementation blocked");}
    }
    super.checkPermission(perm);
  }

  @Override
  public void checkRead(String file)
  {
    if (isDeserializationContext()) {throw new SecurityException("File read during deserialization blocked");}
    super.checkRead(file);
  }

  private boolean isDeserializationContext()
  {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    for (StackTraceElement element : stack)
    {
      if (element.getClassName().equals("java.io.ObjectInputStream") && element.getMethodName().equals("readObject")) {return true;}
    }
    return false;
  }
}

public class SecureObjectInputStream extends ObjectInputStream
{
  public SecureObjectInputStream(InputStream inputStream) throws IOException {super(inputStream);}

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
  {
    SecurityManager originalSM = System.getSecurityManager();
    System.setSecurityManager(new CustomSecurityManager());

    try
    {
      String className = desc.getName();
      if (isWhitelisted(className)) {return super.resolveClass(desc);}
      throw new SecurityException("Class not whitelisted: " + className);
    }
    finally {System.setSecurityManager(originalSM);}
  }

  private boolean isWhitelisted(String className)
  {
    String[] allowedClasses = {
        "java.lang.String",
        "java.lang.Integer",
        "java.util.ArrayList",
        "com.mycompany.SafeClass"
    };

    for (String allowedClass : allowedClasses)
    {
      if (className.equals(allowedClass)) {return true;}
    }
    return false;
  }
}