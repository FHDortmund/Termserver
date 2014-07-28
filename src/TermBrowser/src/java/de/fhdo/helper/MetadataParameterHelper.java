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

import de.fhdo.models.comparators.ComparatorStrings;
import de.fhdo.terminologie.ws.search.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.ListMetadataParameterResponse;
import de.fhdo.terminologie.ws.search.OverallErrorCategory;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.MetadataParameter;

/**
 *
 * @author Philipp Urbauer
 */
public class MetadataParameterHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static HashMap<String, String> paramNames = null;
  private static List<MetadataParameter> mpList = null;

  public static Long getMetadataParameterIdByName(String paramName)
  {
    checkForNull();

    if (paramName == null || paramName.trim().isEmpty())
      return Long.valueOf((long) -1);

    for (String key : MetadataParameterHelper.getMetadataParameterNameTable().keySet())
    {
      if (MetadataParameterHelper.getMetadataParameterNameTable().get(key).compareToIgnoreCase(paramName) == 0)
        return Long.valueOf(key);
    }
    return Long.valueOf((long) -1);
  }

  public static HashMap<String, String> getMetadataParameterNameTable()
  {
    checkForNull();

    return paramNames;
  }

  public static List<MetadataParameter> getMetadataParameterList()
  {
    checkForNull();

    return mpList;
  }

  public static ListModelList getListModelList()
  {
    checkForNull();

    List listMetadataParameter = new ArrayList<String>();
    for (String paramName : MetadataParameterHelper.getMetadataParameterNameTable().values())
    {

      if (!listMetadataParameter.contains(paramName))
        listMetadataParameter.add(paramName);
    }
    ListModelList lm2 = new ListModelList(listMetadataParameter);
    Comparator comparator = new ComparatorStrings();
    lm2.sort(comparator, true);
    return lm2;
  }

  public static String getMetadataParameterNameById(Long paramNameId)
  {
    checkForNull();

    String res = paramNames.get(String.valueOf(paramNameId));
    if (res != null)
    {
      return res;
    }
    else
    {
      return "";
    }
  }

  private static void checkForNull()
  {
    if (paramNames == null)
      createMetadataParameterTables();
  }

  private static void createMetadataParameterTables()
  {
    paramNames = new HashMap<String, String>();

    ListMetadataParameterRequestType parameter = new ListMetadataParameterRequestType();

    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

    ListMetadataParameterResponse.Return response = WebServiceHelper.listMetadataParameter(parameter);

    if (response != null && response.getReturnInfos().getStatus() == Status.OK)
    {
      if (response.getReturnInfos().getOverallErrorCategory() != OverallErrorCategory.INFO)
      {
        try
        {
          Messagebox.show(Labels.getLabel("metadataParameterHelper.loadMetadataParameterFailed") + "\n\n" + response.getReturnInfos().getMessage());
        }
        catch (Exception ex)
        {
          logger.error("metadataParameterHelper.java Error loading MetadataParameter: " + ex);
        }
      }
      else
      {
        Iterator<MetadataParameter> it = response.getMetadataParameter().iterator();
        int x = 0;
        while (it.hasNext())
        {
          MetadataParameter mp = it.next();
          paramNames.put(String.valueOf(x++), mp.getParamName());
        }
        mpList = response.getMetadataParameter();
      }
    }
  }
}
