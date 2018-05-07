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
package de.fhdo.helper;

/**
 *
 * @author Becker
 */
public class DeepLinkHelper {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    static public String getConvertedString(String s, boolean toLowerCase){        
        s = s.replaceAll("Ä", "Ae");
        s = s.replaceAll("Ö", "Oe");
        s = s.replaceAll("Ü", "Ue");
        
        s = s.replaceAll("ä", "ae");
        s = s.replaceAll("ö", "oe");
        s = s.replaceAll("ü", "ue");
        
        s = s.replaceAll("ß", "ss");
        
        if(toLowerCase)
            s = s.toLowerCase();
        
        return s;
    }
}
