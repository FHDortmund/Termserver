/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.tree;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner
 */
public class GenericTreeRowType
{
  private Object data;
  private GenericTreeCellType []cells;
  private String color;
  private boolean enableDrop;
  private boolean enableDrag;
  private boolean selected;
  
  private List<GenericTreeRowType> childRows;
  private GenericTreeRowType parent;
  
  public GenericTreeRowType(GenericTreeRowType _parent)
  {
    parent = _parent;
    childRows = new LinkedList<GenericTreeRowType>();
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
   * @return the cells
   */
  public GenericTreeCellType[] getCells()
  {
    return cells;
  }

  /**
   * @param cells the cells to set
   */
  public void setCells(GenericTreeCellType[] cells)
  {
    this.cells = cells;
  }

  /*public int compareTo(Object o)
  {
    if(o instanceof GenericListRowType)
    {
      GenericListRowType o2 = (GenericListRowType)o;
      
      
    }
    
    return 0;
  }*/

  /**
   * @return the color
   */
  public String getColor()
  {
    return color;
  }

  /**
   * @param color the color to set
   */
  public void setColor(String color)
  {
    this.color = color;
  }

  /**
   * @return the childRows
   */
  public List<GenericTreeRowType> getChildRows()
  {
    return childRows;
  }

  /**
   * @param childRows the childRows to set
   */
  public void setChildRows(List<GenericTreeRowType> childRows)
  {
    this.childRows = childRows;
  }

  /**
   * @return the enableDrop
   */
  public boolean isEnableDrop()
  {
    return enableDrop;
  }

  /**
   * @param enableDrop the enableDrop to set
   */
  public void setEnableDrop(boolean enableDrop)
  {
    this.enableDrop = enableDrop;
  }

  /**
   * @return the parent
   */
  public GenericTreeRowType getParent()
  {
    return parent;
  }

  /**
   * @param parent the parent to set
   */
  public void setParent(GenericTreeRowType parent)
  {
    this.parent = parent;
  }

  /**
   * @return the selected
   */
  public boolean isSelected()
  {
    return selected;
  }

  /**
   * @param selected the selected to set
   */
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }

  /**
   * @return the enableDrag
   */
  public boolean isEnableDrag()
  {
    return enableDrag;
  }

  /**
   * @param enableDrag the enableDrag to set
   */
  public void setEnableDrag(boolean enableDrag)
  {
    this.enableDrag = enableDrag;
  }
}
