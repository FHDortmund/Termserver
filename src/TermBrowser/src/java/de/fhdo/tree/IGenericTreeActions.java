/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.tree;

/**
 *
 * @author Robert Mützner
 */
public interface IGenericTreeActions
{
  void onTreeNewClicked(String id, Object data);
  void onTreeEditClicked(String id, Object data);
  boolean onTreeDeleted(String id, Object data);
  void onTreeSelected(String id, Object data);
  void onTreeRefresh(String id);
}
