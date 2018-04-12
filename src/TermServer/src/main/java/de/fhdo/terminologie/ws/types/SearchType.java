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
package de.fhdo.terminologie.ws.types;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class SearchType
{
  private Boolean traverseConceptsToRoot;
  private Boolean wholeWords, caseSensitive, startsWith;

 
  /**
   * @return the wholeWords
   */
  public Boolean getWholeWords()
  {
    return wholeWords;
  }

  /**
   * @param wholeWords the wholeWords to set
   */
  public void setWholeWords(Boolean wholeWords)
  {
    this.wholeWords = wholeWords;
  }

  /**
   * @return the caseSensitive
   */
  public Boolean getCaseSensitive()
  {
    return caseSensitive;
  }

  /**
   * @param caseSensitive the caseSensitive to set
   */
  public void setCaseSensitive(Boolean caseSensitive)
  {
    this.caseSensitive = caseSensitive;
  }

  /**
   * @return the startsWith
   */
  public Boolean getStartsWith()
  {
    return startsWith;
  }

  /**
   * @param startsWith the startsWith to set
   */
  public void setStartsWith(Boolean startsWith)
  {
    this.startsWith = startsWith;
  }

  /**
   * @return the traverseConceptsToRoot
   */
  public Boolean getTraverseConceptsToRoot()
  {
    return traverseConceptsToRoot;
  }

  /**
   * @param traverseConceptsToRoot the traverseConceptsToRoot to set
   */
  public void setTraverseConceptsToRoot(Boolean traverseConceptsToRoot)
  {
    this.traverseConceptsToRoot = traverseConceptsToRoot;
  }
  
  
}
