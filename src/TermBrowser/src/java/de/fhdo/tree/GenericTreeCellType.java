/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.tree;

/**
 *
 * @author Robert Mützner
 */
public class GenericTreeCellType
{
  private Object data;
  private boolean showLabel;
  private String label;
  private String style;

  public GenericTreeCellType(Object data, boolean showLabel, String label)
  {
    this.data = data;
    this.showLabel = showLabel;
    this.label = label;
  }
  
  public GenericTreeCellType(Object data, boolean showLabel, String label, String style)
  {
    this.data = data;
    this.showLabel = showLabel;
    this.label = label;
    this.style = style;
  }
  
  public Object getDisplayData()
  {
    if(showLabel)
      return label;
    else return data;
  }

  
  /**
   * @return the data
   */
  public Object getData()
  {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(Object data)
  {
    this.data = data;
  }

  /**
   * @return the showLabel
   */
  public boolean isShowLabel()
  {
    return showLabel;
  }

  /**
   * @param showLabel the showLabel to set
   */
  public void setShowLabel(boolean showLabel)
  {
    this.showLabel = showLabel;
  }

  /**
   * @return the label
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label)
  {
    this.label = label;
  }

  /**
   * @return the style
   */
  public String getStyle()
  {
    return style;
  }

  /**
   * @param style the style to set
   */
  public void setStyle(String style)
  {
    this.style = style;
  }
}
