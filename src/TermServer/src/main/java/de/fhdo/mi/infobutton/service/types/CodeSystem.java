package de.fhdo.mi.infobutton.service.types;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class CodeSystem {
  private String oid;
  private String display;

  public CodeSystem()
  {
  }

  
  public CodeSystem(String oid)
  {
    this.oid = oid;
  }

  public CodeSystem(String oid, String display)
  {
    this.oid = oid;
    this.display = display;
  }
  
  

  /**
   * @return the oid
   */
  public String getOid()
  {
    return oid;
  }

  /**
   * @param oid the oid to set
   */
  public void setOid(String oid)
  {
    this.oid = oid;
  }

  /**
   * @return the display
   */
  public String getDisplay()
  {
    return display;
  }

  /**
   * @param display the display to set
   */
  public void setDisplay(String display)
  {
    this.display = display;
  }
  
}
