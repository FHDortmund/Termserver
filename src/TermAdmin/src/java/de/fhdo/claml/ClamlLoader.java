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
package de.fhdo.claml;

import clamlXSD.ClaML;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Michael
 */
public class ClamlLoader
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private ClaML claml = null;

  public ClamlLoader(byte[] bytes)
  {
    String packagename = ClaML.class.getPackage().getName();
    logger.debug("Package: " + packagename);
    JAXBContext jc;
    Unmarshaller u;
    try
    {
      jc = JAXBContext.newInstance(packagename);
      u = jc.createUnmarshaller();

      //File file = new File("D:\\Temp\\ops2012syst_claml_20111103.xml");
      //File file = new File("temp_import.xml");
      OutputStream out = new FileOutputStream("temp_import.xml");
      out.write(bytes);
      out.close();

      //ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      //bis.reset();
      this.claml = (ClaML) u.unmarshal(new File("temp_import.xml"));
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Parsen des ClaML-Dokuments: " + ex.getMessage());
      ex.printStackTrace();
    }

  }

  /**
   * @return the claml
   */
  public ClaML getClaml()
  {
    return claml;
  }

  /**
   * @param claml the claml to set
   */
  public void setClaml(ClaML claml)
  {
    this.claml = claml;
  }
}
