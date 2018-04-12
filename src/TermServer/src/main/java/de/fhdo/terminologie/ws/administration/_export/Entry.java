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
package de.fhdo.terminologie.ws.administration._export;

import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;

/**
 *
 * @author Philipp Urbauer
 */

public class Entry {
    
    int level;
    CodeSystemEntityVersion csev;
    CodeSystemEntityVersion csevParent;
    CodeSystemVersion   csv;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public CodeSystemEntityVersion getCsev() {
        return csev;
    }

    public void setCsev(CodeSystemEntityVersion csev) {
        this.csev = csev;
    }

    public CodeSystemEntityVersion getCsevParent() {
        return csevParent;
    }

    public void setCsevParent(CodeSystemEntityVersion csevParent) {
        this.csevParent = csevParent;
    }

    public CodeSystemVersion getCsv() {
        return csv;
    }

    public void setCsv(CodeSystemVersion csv) {
        this.csv = csv;
    }
}
