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
package de.fhdo;

/**
 *
 * @author Robert Mützner
 */
public class Definitions
{
  public final static String VERSION = "2.0.1";
  
  public final static String APP_KEY = "TERMSERVER";
  
  public final static String DOMAIN_ADMIN = "/gui/admin/admin.zul";
  public final static String DOMAIN_MAIN = "/gui/main/main.zul";
  
  public final static long DOMAINID_LANGUAGECODES = 1;
  public static final long DOMAINID_COUNTRY_CODES_ISO_639_1 = 1;
  public static final long DOMAINID_DISPLAY_ORDER = 3;
  public static final long DOMAINID_IMPORT_TYPES_CS = 4;
  public static final long DOMAINID_EXPORT_TYPES = 5;
  public static final long DOMAINID_CODESYSTEM_TYPE = 6;
  public static final long DOMAINID_METADATA_PARAMETER_TYPE = 7;
  public static final long DOMAINID_CODESYSTEM_TAXONOMY = 8;
  public static final long DOMAINID_CODESYSTEMVERSION_VALIDITYRANGE = 9;
  public static final long DOMAINID_IMPORT_TYPES_VS = 12;
  public static final long DOMAINID_STATUS_CONCEPT_VISIBILITY = 13;
  public static final long DOMAINID_STATUS_CONCEPT_DEACTIVATED = 14;
  public static final long DOMAINID_STATUS = 15;
  
  
  public static final int STATUS_VISIBILITY_INVISIBLE = 0;
  public static final int STATUS_VISIBILITY_VISIBLE = 1;
  
  public static final int STATUS_DEACTIVATED_DEACTIVE = 0;
  public static final int STATUS_DEACTIVATED_ACTIVE = 1;
  public static final int STATUS_DEACTIVATED_DELETED = 2;
  public static final int STATUS_DEACTIVATED_DEPRECATED = 3;
  
  
  
  public static enum ASSOCIATION_KIND
  {
    ONTOLOGY(1), TAXONOMY(2), CROSS_MAPPING(3), LINK(4);
    private int code = -1;
    private String img = "";

    private ASSOCIATION_KIND(int c)
    {
      code = c;
    }

    public int getCode()
    {
      return code;
    }

    /**
     * @return the img
     */
    public String getImg()
    {
      return getImg("16");
    }
    
    public String getImg(String scale)
    {
      if(code == ASSOCIATION_KIND.ONTOLOGY.code)
      {
        return "/rsc/img/design/ontology_" + scale + "x" + scale +".png";
      }
      else if(code == ASSOCIATION_KIND.TAXONOMY.code)
      {
        return "/rsc/img/design/hierarchical_" + scale + "x" + scale +".png";
      }
      else if(code == ASSOCIATION_KIND.CROSS_MAPPING.code)
      {
        return "/rsc/img/design/cross_" + scale + "x" + scale +".png";
      }
      else if(code == ASSOCIATION_KIND.LINK.code)
      {
        return "/rsc/img/design/link_" + scale + "x" + scale +".png";
      }
      return "";
    }
  }

  public static boolean isAssociationKindValid(Integer kind)
  {
    ASSOCIATION_KIND[] kinds = ASSOCIATION_KIND.values();

    for (int i = 0; i < kinds.length; ++i)
    {
      if (kinds[i].getCode() == kind)
        return true;
    }
    return false;
  }

  public static String readAssociationKinds()
  {
    String s = "";
    ASSOCIATION_KIND[] kinds = ASSOCIATION_KIND.values();

    for (int i = 0; i < kinds.length; ++i)
    {
      s += "\n" + kinds[i].name() + " (" + kinds[i].getCode() + ")";
    }
    return s;
  }
  
  /*public static enum STATUS_CODES
  {
    INACTIVE(0), ACTIVE(1), DELETED(2);
    private int code;

    private STATUS_CODES(int c)
    {
      code = c;
    }

    public int getCode()
    {
      return code;
    }

    public static boolean isStatusCodeValid(Integer StatusCode)
    {
      STATUS_CODES[] codes = STATUS_CODES.values();

      for (int i = 0; i < codes.length; ++i)
      {
        if (codes[i].getCode() == StatusCode)
          return true;
      }
      return false;
    }

    public static String readStatusCodes()
    {
      String s = "";
      STATUS_CODES[] codes = STATUS_CODES.values();

      for (int i = 0; i < codes.length; ++i)
      {
        s += "\n" + codes[i].name() + " (" + codes[i].getCode() + ")";
      }
      return s;
    }
  }*/
  

  //public final static String STD_REPOSITORY_ADRESS = "http://193.25.22.68:8080/StandardsRepository/";
}
