/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: AFPInvokeMediumMap.java 1778105 2017-01-10 10:33:41Z ssteiner $ */

package org.apache.fop.render.afp.extensions;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is the pass-through value object for the AFP extension.
 */
public class AFPInvokeMediumMap extends AFPExtensionAttachment {

    private static final long serialVersionUID = -7493160084509249309L;

    /**
     * Default constructor.
     */
    public AFPInvokeMediumMap() {
        super(AFPElementMapping.INVOKE_MEDIUM_MAP);
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (name != null && name.length() > 0) {
            atts.addAttribute("", ATT_NAME, ATT_NAME, "CDATA", name);
        }
        handler.startElement(CATEGORY, elementName, elementName, atts);
        handler.endElement(CATEGORY, elementName, elementName);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AFPInvokeMediumMap(name=" + getName() + ")";
    }
}
