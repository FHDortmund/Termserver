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
package de.fhdo.gui.admin.modules.terminology.termimport;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.Status;
import ehd._001.KeytabsTyp;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ImportCS_KBV implements IImport
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  long formatId;
  types.termserver.fhdo.de.CodeSystem selectedCodeSystem;
  //types.termserver.fhdo.de.CodeSystemVersion csv;

  public ImportCS_KBV(long FormatId)
  {
    formatId = FormatId;
  }

  public void preview(GenericList genericList, byte[] bytes)
  {
    try
    {
      //String text = new String(bytes);
      //logger.debug(text);

      // Datei auswerten
      // XML-Datei laden
      JAXBContext jc = JAXBContext.newInstance("ehd._001");
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      //unmarshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");

      InputStream is = new ByteArrayInputStream(bytes);
      Object o = unmarshaller.unmarshal(is);

      JAXBElement<ehd._001.KeytabsTyp> doc = (JAXBElement<ehd._001.KeytabsTyp>) o;
      KeytabsTyp root = doc.getValue();
      ehd._001.KeytabTyp keytab = root.getKeytab().get(0);

      selectedCodeSystem = new types.termserver.fhdo.de.CodeSystem();
      selectedCodeSystem.setName(keytab.getSN());
      //selectedCodeSystem.setCodeSystemVersi(new HashSet<types.termserver.fhdo.de.CodeSystemVersion>());
      types.termserver.fhdo.de.CodeSystemVersion csv = new types.termserver.fhdo.de.CodeSystemVersion();
      csv.setName(keytab.getSV());
      csv.setOid(keytab.getS());
      selectedCodeSystem.getCodeSystemVersions().add(csv);

      logger.debug("name: " + selectedCodeSystem.getName());
      logger.debug("Version-name: " + csv.getName());
      logger.debug("oid: " + csv.getOid());

      /*for (int i = 0; i < keytab.getKey().size() && i < 3; ++i)
       {
       ((Label) getFellow("labelCode" + (i + 1))).setValue(keytab.getKey().get(i).getV());
       //((Label)getFellow("labelCode" + (i + 1))).setValue(keytab.getKey().get(i).getDN());
       //((Label)getFellow("labelWert" + (i + 1))).setValue(Encoding.GetEncoding("ISO-8859-1").GetString(keytab.getKey().get(i).getDN()));
       ((Label) getFellow("labelWert" + (i + 1))).setValue(keytab.getKey().get(i).getDN());
       //((Label)getFellow("labelWert" + (i + 1))).setValue(new String(keytab.getKey().get(i).getDN().getBytes("ISO-8859-1"), Charset.defaultCharset()));
       }*/
      //r.close();
      is.close();

      /*Textbox tb = (Textbox) getFellow("textboxDateiname");
      
       tb.setValue(media.getName());

       ((Label) getFellow("labelCodesystem")).setValue(keytab.getSN());
       ((Label) getFellow("labelVersion")).setValue(keytab.getSV());
       ((Label) getFellow("labelOID")).setValue(keytab.getS());*/
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType(Labels.getLabel("name"), 180, "", false, "String", true, true, false, false));
      header.add(new GenericListHeaderType(Labels.getLabel("value"), 0, "", false, "boolean", true, true, false, false));

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      
      dataList.add(createRow("Codesystem name:",selectedCodeSystem.getName()));
      dataList.add(createRow("Version name:",csv.getName()));
      dataList.add(createRow("OID:",csv.getOid()));
      
      genericList.setButton_new(false);
      genericList.setListHeader(header);
      genericList.setDataList(dataList);

      genericList.getParent().setVisible(true);

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);

    }
  }
  
  private GenericListRowType createRow(String key, String value)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(key, false, "");
    cells[1] = new GenericListCellType(value, false, "");

    row.setCells(cells);

    return row;
  }

  /**
   *
   * @param bytes
   * @param progress
   * @param labelInfo
   * @param codeSystem
   * @param valueSet
   * @return true, wenn asynchroner Aufruf
   */
  public boolean startImport(byte[] bytes, Progressmeter progress, Label labelInfo, CodeSystem codeSystem, ValueSet valueSet)
  {
    logger.debug("ImportCS_KBV - startImport()");

    progress.setVisible(true);

    // Login
    ImportCodeSystemRequestType request = new ImportCodeSystemRequestType();
    request.setLoginToken(SessionHelper.getSessionId());

    // Codesystem
    /*request.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
     request.getCodeSystem().setId(codeSystem.getId());

     types.termserver.fhdo.de.CodeSystemVersion csv = new CodeSystemVersion();

     if (codeSystem.getCodeSystemVersions() != null && codeSystem.getCodeSystemVersions().size() > 0)
     {
     csv.setVersionId(((de.fhdo.terminologie.db.hibernate.CodeSystemVersion) codeSystem.getCodeSystemVersions().toArray()[0]).getVersionId());
     csv.setName(((de.fhdo.terminologie.db.hibernate.CodeSystemVersion) codeSystem.getCodeSystemVersions().toArray()[0]).getName());
     }

     request.getCodeSystem().getCodeSystemVersions().add(csv);*/
    request.setCodeSystem(selectedCodeSystem);

    // Claml-Datei
    request.setImportInfos(new ImportType());
    request.getImportInfos().setFormatId(formatId); // CSV_ID
    request.getImportInfos().setFilecontent(bytes);

    ImportCodeSystemResponse.Return response = WebServiceHelper.importCodeSystem(request);

    String msg = response.getReturnInfos().getMessage();
    logger.debug("Return: " + msg);

    //CodeSystemVersion
    if (response.getReturnInfos().getStatus().equals(Status.OK))
    {

    }

    labelInfo.setValue(msg);
    progress.setVisible(false);

    return false;
  }

  public void cancelImport()
  {
  }

  public boolean supportsFormat(String format)
  {
    if (format != null)
    {
      if (format.equalsIgnoreCase("xml"))
        return true;
    }
    return false;
  }

  public boolean mustSpecifyCodesystem()
  {
    return false;
  }

  public boolean mustSpecifyCodesystemVersion()
  {
    return false;
  }

}
