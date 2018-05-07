package de.fhdo.mi.infobutton.service.types;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class Unit {
  private String code;
  private String codeDisplay;
  private CodeSystem codeSystem;

  /**
   * @return the code
   */
  public String getCode()
  {
    return code;
  }

  /**
   * @param code the code to set
   */
  public void setCode(String code)
  {
    this.code = code;
  }

  /**
   * @return the codeDisplay
   */
  public String getCodeDisplay()
  {
    return codeDisplay;
  }

  /**
   * @param codeDisplay the codeDisplay to set
   */
  public void setCodeDisplay(String codeDisplay)
  {
    this.codeDisplay = codeDisplay;
  }

  /**
   * @return the codeSystem
   */
  public CodeSystem getCodeSystem()
  {
    return codeSystem;
  }

  /**
   * @param codeSystem the codeSystem to set
   */
  public void setCodeSystem(CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
  }
  
}
