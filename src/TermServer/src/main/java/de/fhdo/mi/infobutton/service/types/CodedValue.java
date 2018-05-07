package de.fhdo.mi.infobutton.service.types;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class CodedValue {
  private String code;
  private String codeDisplay;
  private String value;
  
  private CodeSystem codeSystem;
  private Unit unit;

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
   * @return the value
   */
  public String getValue()
  {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value)
  {
    this.value = value;
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

  /**
   * @return the unit
   */
  public Unit getUnit()
  {
    return unit;
  }

  /**
   * @param unit the unit to set
   */
  public void setUnit(Unit unit)
  {
    this.unit = unit;
  }
  
  
  
}
