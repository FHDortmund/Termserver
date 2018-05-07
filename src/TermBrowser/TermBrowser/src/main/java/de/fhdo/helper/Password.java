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

/**
 *
 * @author Robert Mützner
 */
public class Password
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  public static final int it_count = 1000;

  public Password()
  {

  }

  public static String getSaltedPassword(String Password, String Salt, String Username)
  {
    String md5 = MD5.getMD5(Password + Salt + Username);
    
    for(int i=0;i<it_count;++i)
    {
      md5 = MD5.getMD5(md5);
    }

    return md5;
  }
  public static String getMD5Password(String Password)
  {
    return getMD5Password(Password, 0);
  }
  public static String getMD5Password(String Password, int IterationCount)
  {
    String md5 = MD5.getMD5(Password);
    
    for(int i=0;i<IterationCount;++i)
    {
      md5 = MD5.getMD5(md5);
    }

    return md5;
  }

  public static String generateRandomSalt()
  {
    return generateRandomPassword(12);
  }

  public static String generateRandomPassword(int Length)
  {
    char[] pw = new char[Length];
    int c = 'A';
    int r1 = 0;
    for (int i = 0; i < Length; i++)
    {
      r1 = (int) (Math.random() * 3);
      switch (r1)
      {
        case 0:
          c = '0' + (int) (Math.random() * 10);
          break;
        case 1:
          c = 'a' + (int) (Math.random() * 26);
          break;
        case 2:
          c = 'A' + (int) (Math.random() * 26);
          break;
      }
      pw[i] = (char) c;
    }

    return new String(pw);
  }



}
