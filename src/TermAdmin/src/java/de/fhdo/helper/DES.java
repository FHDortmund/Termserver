/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2014 FH Dortmund: Peter Haas, Robert Muetzner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhdo.helper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.binary.Base64;


/**
 *
 * @author Robert Mützner
 */
public class DES
{

  public static String encrypt(String Text)
  {
    try
    {
      DESKeySpec keySpec = new DESKeySpec("schluessel_stdrepository15".getBytes("UTF8"));
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey key = keyFactory.generateSecret(keySpec);

      // ENCODE plainTextPassword String
      byte[] cleartext = Text.getBytes("UTF8");
      Cipher cipher = Cipher.getInstance("DES");
      // cipher is not thread safe
      cipher.init(Cipher.ENCRYPT_MODE, key);
      return Base64.encodeBase64URLSafeString(cipher.doFinal(cleartext));

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }

  public static String decrypt(String Text)
  {
    try
    {
      DESKeySpec keySpec = new DESKeySpec("schluessel_stdrepository15".getBytes("UTF8"));
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey key = keyFactory.generateSecret(keySpec);

      byte[] encrypedPwdBytes = Base64.decodeBase64(Text);
      Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
      cipher.init(Cipher.DECRYPT_MODE, key);

      byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));

      return new String(plainTextPwdBytes);

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }
}
