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
package de.fhdo.terminologie.helper;

/**
 *
 * @author Philipp Urbauer
 */


public class EscCharCheckQuot {
    
    public static String checkAttribute(String input){
        String output;
        if(input == null)
            input = "";
        
        
        output = input.replaceAll("\"", "'");
        output = output.replace("&", "_AMP_"); // Das geht afoch net!
        output = output.replace("<", "_LT_");  // Das geht afoch net!
        output = output.replace(">", "_GT_");  // Das geht afoch net!
        
      
        return output;
    }
    
    public static String check(String input){
        String output;
        if(input == null)
            input = "";
        
        
        output = input.replaceAll("\"", "'");
        //output = output.replace("'", "&apos;");
        //output = output.replace("<", "&lt;");
        //output = output.replace(">", "&gt;");
        //output = output.replace("&", "&amp;");
        
        return output;
    }
}
