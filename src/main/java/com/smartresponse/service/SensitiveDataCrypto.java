package com.smartresponse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

@Component
public class SensitiveDataCrypto {
 private final SecretKey key;
 public SensitiveDataCrypto(@Value("${app.data-encryption-key:local-development-key-change-me}") String secret) {
  try { key=new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)),"AES"); }
  catch(Exception e) { throw new IllegalStateException(e); }
 }
 public byte[] encrypt(String value) {
  try { byte[] iv=new byte[12];new SecureRandom().nextBytes(iv);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,iv));byte[] encrypted=cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));byte[] result=new byte[iv.length+encrypted.length];System.arraycopy(iv,0,result,0,iv.length);System.arraycopy(encrypted,0,result,iv.length,encrypted.length);return result; }
  catch(Exception e) { throw new IllegalStateException("Unable to encrypt sensitive data",e); }
 }
 public String decrypt(byte[] value) {
  try { if (value == null || value.length < 13) return null; byte[] iv=java.util.Arrays.copyOfRange(value,0,12);byte[] encrypted=java.util.Arrays.copyOfRange(value,12,value.length);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,iv));return new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8); }
  catch(Exception e) { throw new IllegalStateException("Unable to decrypt sensitive data",e); }
 }
}
