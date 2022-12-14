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

/* $Id: StructureHierarchyMember.java 1805173 2017-08-16 10:50:04Z ssteiner $ */

package org.apache.fop.pdf;

/**
 * An element in the document's structure tree. This can be either the structure tree root
 * or a structure element.
 *
 * "Section 10.6, Logical Structure of the PDF Reference, 4th edition (PDF 1.5)"
 */
public abstract class StructureHierarchyMember extends PDFDictionary {

    /**
     * Adds the given object to the array of kids.
     *
     * @param kid an object to be added to the K entry
     */
    public abstract void addKid(PDFObject kid);

}
