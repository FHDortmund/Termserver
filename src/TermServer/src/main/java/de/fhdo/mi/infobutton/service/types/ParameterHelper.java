package de.fhdo.mi.infobutton.service.types;

import org.hl7.TextType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ParameterHelper
{
  public static TextType createText(String text)
  {
    TextType obj = new TextType();
    obj.setType("text");
    obj.setValue(text);
    return obj;
  }
}
