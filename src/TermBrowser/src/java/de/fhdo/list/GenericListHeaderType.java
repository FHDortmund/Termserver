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
package de.fhdo.list;

/**

 @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class GenericListHeaderType
{
  private int index;
  private String name;
  private int width;
  private String image;
  private boolean showFilter;
  private Object datatype;
  private boolean showDefault;
  private boolean allowSorting;
  
  private boolean allowInlineEditing;
  private boolean component;

  /**
   * Erstellt eine Spalte
   * 
   * @param name Spaltenname (Überschrift)
   * @param width Breite der Spalte in Pixel (0 für variable Größe)
   * @param image Bild neben der Überschrift, z.B. /rsc/img/symbols/xyz.png
   * @param showFilter true, wenn ein Filter in der Überschrift gezeigt werden soll (abhängig vom Datentyp)
   * @param datatype Datentyp der Spalte, mögliche Werte: "String", "Bool", "Date", "DateTime", String[]
   * @param showDefault 
   * @param allowSorting true, wenn nach Spalte sortiert werden darf
   * @param allowInlineEditing true, wenn Zeileninhalte geändert werden dürfen
   * @param component true, falls die Inhalte keine Label, sondern Compenents sein sollen
   */
  public GenericListHeaderType(String name, int width, String image, boolean showFilter, Object datatype, boolean showDefault, boolean allowSorting, boolean allowInlineEditing, boolean component)
  {
    this.name = name;
    this.width = width;
    this.image = image;
    this.showFilter = showFilter;
    this.datatype = datatype;
    this.showDefault = showDefault;
    this.allowSorting = allowSorting;
    this.allowInlineEditing = allowInlineEditing;
    this.component = component;
  }
  

  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @return the width
   */
  public int getWidth()
  {
    return width;
  }

  /**
   * @param width the width to set
   */
  public void setWidth(int width)
  {
    this.width = width;
  }

  /**
   * @return the image
   */
  public String getImage()
  {
    return image;
  }

  /**
   * @param image the image to set
   */
  public void setImage(String image)
  {
    this.image = image;
  }

  /**
   * @return the showFilter
   */
  public boolean isShowFilter()
  {
    return showFilter;
  }

  /**
   * @param showFilter the showFilter to set
   */
  public void setShowFilter(boolean showFilter)
  {
    this.showFilter = showFilter;
  }

  /**
   * @return the datatype
   */
  public Object getDatatype()
  {
    return datatype;
  }

  /**
   * @param datatype the datatype to set
   */
  public void setDatatype(Object datatype)
  {
    this.datatype = datatype;
  }

  /**
   * @return the showDefault
   */
  public boolean isShowDefault()
  {
    return showDefault;
  }

  /**
   * @param showDefault the showDefault to set
   */
  public void setShowDefault(boolean showDefault)
  {
    this.showDefault = showDefault;
  }

  /**
   * @return the allowInlineEditing
   */
  public boolean isAllowInlineEditing()
  {
    return allowInlineEditing;
  }

  /**
   * @param allowInlineEditing the allowInlineEditing to set
   */
  public void setAllowInlineEditing(boolean allowInlineEditing)
  {
    this.allowInlineEditing = allowInlineEditing;
  }

  /**
   * @return the component
   */
  public boolean isComponent()
  {
    return component;
  }

  /**
   * @param component the component to set
   */
  public void setComponent(boolean component)
  {
    this.component = component;
  }

  /**
   * @return the index
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(int index)
  {
    this.index = index;
  }

  /**
   * @return the allowSorting
   */
  public boolean isAllowSorting()
  {
    return allowSorting;
  }

  /**
   * @param allowSorting the allowSorting to set
   */
  public void setAllowSorting(boolean allowSorting)
  {
    this.allowSorting = allowSorting;
  }
  
}
