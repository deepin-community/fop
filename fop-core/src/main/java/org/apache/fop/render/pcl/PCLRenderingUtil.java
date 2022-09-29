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

/* $Id: PCLRenderingUtil.java 1756387 2016-08-15 14:15:27Z ssteiner $ */

package org.apache.fop.render.pcl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.UnitConv;

import org.apache.fop.apps.FOUserAgent;

/**
 * Utility class for handling all sorts of peripheral tasks around PCL generation.
 */
public class PCLRenderingUtil {

    /** logging instance */
    private static Log log = LogFactory.getLog(PCLRenderingUtil.class);

    private FOUserAgent userAgent;

    /**
     * Controls whether appearance is more important than speed. "SPEED" can cause some FO feature
     * to be ignored (like the advanced borders).
     */
    private PCLRenderingMode renderingMode = PCLRenderingMode.SPEED;

    /** Controls the dithering quality when rendering gray or color images. */
    private float ditheringQuality = 0.5f;

    private boolean useColor;
    private boolean optimizeResources;

    /**
     * Controls whether the generation of PJL commands gets disabled.
     */
    private boolean disabledPJL;

    /**
     * Controls whether all text should be painted as text. This is a fallback setting in case the mixture of native and
     * bitmapped text does not provide the necessary quality.
     */
    private boolean allTextAsBitmaps;

    PCLRenderingUtil(FOUserAgent userAgent) {
        this.userAgent = userAgent;
        initialize();
    }

    private void initialize() {
    }

    /**
     * Returns the user agent.
     * @return the user agent
     */
    public FOUserAgent getUserAgent() {
        return this.userAgent;
    }

    /**
     * Configures the renderer to trade speed for quality if desired. One example here is the way
     * that borders are rendered.
     * @param mode one of the {@link PCLRenderingMode}.* constants
     */
    public void setRenderingMode(PCLRenderingMode mode) {
        this.renderingMode = mode;
        this.ditheringQuality = mode.getDefaultDitheringQuality();
    }

    /**
     * Returns the selected rendering mode.
     * @return the rendering mode
     */
    public PCLRenderingMode getRenderingMode() {
        return this.renderingMode;
    }

    /**
     * Returns the dithering quality to be used when encoding gray or color images.
     * @return the quality (0.0f..1.0f)
     */
    public float getDitheringQuality() {
        return this.ditheringQuality;
    }

    /**
     * Controls whether PJL commands shall be generated by the PCL renderer.
     * @param disable true to disable PJL commands
     */
    public void setPJLDisabled(boolean disable) {
        this.disabledPJL = disable;
    }

    /**
     * Indicates whether PJL generation is disabled.
     * @return true if PJL generation is disabled.
     */
    public boolean isPJLDisabled() {
        return this.disabledPJL;
    }

    /**
     * Controls whether all text should be generated as bitmaps or only text for which there's no native font.
     * @param allTextAsBitmaps true if all text should be painted as bitmaps
     */
    public void setAllTextAsBitmaps(boolean allTextAsBitmaps) {
        this.allTextAsBitmaps = allTextAsBitmaps;
    }

    /**
     * Indicates whether all text shall be painted as bitmaps.
     * @return true if all text shall be painted as bitmaps
     */
    public boolean isAllTextAsBitmaps() {
        return this.allTextAsBitmaps;
    }

    public void setColorEnabled(boolean useColor) {
        this.useColor = useColor;
    }

    public boolean isColorEnabled() {
        return useColor;
    }

    /**
     * Determines the print direction based on the given transformation matrix. This method
     * only detects right angles (0, 90, 180, 270). If any other angle is determined, 0 is
     * returned.
     * @param transform the transformation matrix
     * @return the angle in degrees of the print direction.
     */
    public static int determinePrintDirection(AffineTransform transform) {
        int newDir;
        if (transform.getScaleX() == 0 && transform.getScaleY() == 0
                && transform.getShearX() == 1 && transform.getShearY() == -1) {
            newDir = 90;
        } else if (transform.getScaleX() == -1 && transform.getScaleY() == -1
                && transform.getShearX() == 0 && transform.getShearY() == 0) {
            newDir = 180;
        } else if (transform.getScaleX() == 0 && transform.getScaleY() == 0
                && transform.getShearX() == -1 && transform.getShearY() == 1) {
            newDir = 270;
        } else {
            newDir = 0;
        }
        return newDir;
    }

    /**
     * Returns a coordinate in PCL's coordinate system when given a coordinate in the user
     * coordinate system.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param transform the currently valid transformation matrix
     * @param pageDefinition the currently valid page definition
     * @param printDirection the currently valid print direction
     * @return the transformed point
     */
    public static Point2D transformedPoint(int x, int y, AffineTransform transform,
            PCLPageDefinition pageDefinition, int printDirection) {
        if (log.isTraceEnabled()) {
            log.trace("Current transform: " + transform);
        }
        Point2D.Float orgPoint = new Point2D.Float(x, y);
        Point2D.Float transPoint = new Point2D.Float();
        transform.transform(orgPoint, transPoint);
        //At this point we have the absolute position in FOP's coordinate system

        //Now get PCL coordinates taking the current print direction and the logical page
        //into account.
        Dimension pageSize = pageDefinition.getPhysicalPageSize();
        Rectangle logRect = pageDefinition.getLogicalPageRect();
        switch (printDirection) {
        case 0:
            transPoint.x -= logRect.x;
            transPoint.y -= logRect.y;
            break;
        case 90:
            float ty = transPoint.x;
            transPoint.x = pageSize.height - transPoint.y;
            transPoint.y = ty;
            transPoint.x -= logRect.y;
            transPoint.y -= logRect.x;
            break;
        case 180:
            transPoint.x = pageSize.width - transPoint.x;
            transPoint.y = pageSize.height - transPoint.y;
            transPoint.x -= pageSize.width - logRect.x - logRect.width;
            transPoint.y -= pageSize.height - logRect.y - logRect.height;
            //The next line is odd and is probably necessary due to the default value of the
            //Text Length command: "1/2 inch less than maximum text length"
            //I wonder why this isn't necessary for the 90 degree rotation. *shrug*
            transPoint.y -= UnitConv.in2mpt(0.5);
            break;
        case 270:
            float tx = transPoint.y;
            transPoint.y = pageSize.width - transPoint.x;
            transPoint.x = tx;
            transPoint.x -= pageSize.height - logRect.y - logRect.height;
            transPoint.y -= pageSize.width - logRect.x - logRect.width;
            break;
        default:
            throw new IllegalStateException("Illegal print direction: " + printDirection);
        }
        return transPoint;
    }

    public boolean isOptimizeResources() {
        return optimizeResources;
    }

    public void setOptimizeResources(boolean b) {
        optimizeResources = b;
    }

}