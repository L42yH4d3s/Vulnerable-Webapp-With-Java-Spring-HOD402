package com.example.webapp.util;
public class SantitizeXml {

  public static String removeDoctype(String maliciousXml) {

    String regex = "<!DOCTYPE[^>]*>|<!ENTITY[^>]*>";
    return maliciousXml.replaceAll(regex, "");
  }
}
