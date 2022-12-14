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

/* $Id: PDFImageElementBridge.java 1805173 2017-08-16 10:50:04Z ssteiner $ */

package org.apache.fop.svg;

import org.apache.xmlgraphics.image.loader.ImageFlavor;

import org.apache.fop.image.loader.batik.BatikImageFlavors;

/**
 * PDF Image Element Bridge class for the &lt;image&gt; element when jpeg images.
 *
 * This work was originally authored by <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFImageElementBridge extends AbstractFOPImageElementBridge {

    /**
     * Constructs a new bridge for the &lt;image&gt; element.
     */
    public PDFImageElementBridge() { }

    private final ImageFlavor[] supportedFlavors = new ImageFlavor[]
                                               {ImageFlavor.RAW_JPEG,
                                                ImageFlavor.RAW_CCITTFAX,
                                                ImageFlavor.GRAPHICS2D,
                                                BatikImageFlavors.SVG_DOM};

    /** {@inheritDoc} */
    protected ImageFlavor[] getSupportedFlavours() {
        return supportedFlavors;
    }

}
