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

import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.ws.search.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.ListDomainValuesResponse;
import de.fhdo.terminologie.ws.search.OverallErrorCategory;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

/**
 *
 * @author Becker
 */
public class LanguageHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static HashMap<String, String> languages;
  private static HashMap<String, String> languageCodes;

  public static HashMap<String, String> getLanguageTable()
  {
    if (languages == null)
      createLanguageTable();

    return languages;
  }
  
  public static HashMap<String, String> getLanguageCodes()
  {
    if (languageCodes == null)
      createLanguageTable();

    return languageCodes;
  }

  public static Long getLanguageIdByName(String language)
  {
    if (language == null || language.trim().isEmpty())
      return Long.valueOf((long) -1);

    for (String key : LanguageHelper.getLanguageTable().keySet())
    {
      if (LanguageHelper.getLanguageTable().get(key).compareToIgnoreCase(language) == 0)
        return Long.valueOf(key);
    }
    return Long.valueOf((long) -1);
  }

  public static ListModelList getListModelList()
  {
    List listLanguage = new ArrayList<String>();
    for (String language : LanguageHelper.getLanguageTable().values())
    {
      listLanguage.add(language);
    }
    ListModelList lm2 = new ListModelList(listLanguage);
    Comparator comparator = new ComparatorStrings();
    lm2.sort(comparator, true);
    return lm2;
  }

  private static void createLanguageTable()
  {
    logger.debug("createLanguageTable()");
    
    languages = new HashMap<String, String>();
    languageCodes = new HashMap<String, String>();

    
    List<DomainValue> dvList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_ISO_639_1_LANGUACECODES);

    for(DomainValue dv : dvList)
    {
      languages.put(String.valueOf(dv.getDomainValueId()), dv.getDomainDisplay());
      languageCodes.put(dv.getDomainCode(), dv.getDomainDisplay());
    }
    
    /*ListDomainValuesResponse.Return response = WebServiceHelper.listDomainValues(parameter);
    logger.debug("WS-response: " + response.getReturnInfos().getMessage());

    if (response != null && response.getReturnInfos().getStatus() == Status.OK)
    {
      if (response.getReturnInfos().getOverallErrorCategory() != OverallErrorCategory.INFO)
      {
        try
        {
          Messagebox.show(Labels.getLabel("languageHelper.loadLanguageFailed") + "\n\n" + response.getReturnInfos().getMessage());
        }
        catch (Exception ex)
        {
          Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      else
      {
        Iterator<DomainValue> it = response.getDomainValues().iterator();
        while (it.hasNext())
        {
          DomainValue dv = it.next();
          languages.put(String.valueOf(dv.getDomainValueId()), dv.getDomainDisplay());
          languageCodes.put(dv.getDomainCode(), dv.getDomainDisplay());
          
          //logger.debug("adding language to map with code: " + dv.getDomainCode());
        }
      }
    }*/
  }
  
  public static List<String> getLanguagesFromString(String str)
  {
    List<String> list = new LinkedList<String>();
    
    if(str != null && str.length() > 0)
    {
      String []s = str.split(";");
      for(String _s : s)
      {
        list.add(_s.trim());
      }
    }
    
    return list;
  }
  
  public static String getLanguageNameFromCode(String code)
  {
    Map<String, String> map = getLanguageCodes();
    
    if(map.containsKey(code))
      return map.get(code);
    
    return "";
  }
}
