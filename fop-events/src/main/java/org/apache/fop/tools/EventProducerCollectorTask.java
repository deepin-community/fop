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

/* $Id: EventProducerCollectorTask.java 1761021 2016-09-16 11:40:57Z ssteiner $ */

package org.apache.fop.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;

import org.apache.fop.events.model.EventModel;
import org.apache.fop.events.model.EventProducerModel;

/**
 * Ant task which inspects a file set for Java interfaces which extend the
 * {@link org.apache.fop.events.EventProducer} interface. For all such interfaces an event model
 * file and a translation file for the human-readable messages generated by the events is
 * created and/or updated.
 */
public class EventProducerCollectorTask extends Task {

    private List<FileSet> filesets = new java.util.ArrayList<FileSet>();
    private File destDir;
    private File translationFile;

    /** {@inheritDoc} */
    public void execute() throws BuildException {
        try {
            EventProducerCollector collector = new EventProducerCollector();
            long lastModified = processFileSets(collector);
            for (EventModel model : collector.getModels()) {
                File parentDir = getParentDir(model);
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw new BuildException(
                            "Could not create target directory for event model file: " + parentDir);
                }
                File modelFile = new File(parentDir, "event-model.xml");
                if (!modelFile.exists() || lastModified > modelFile.lastModified()) {
                    model.saveToXML(modelFile);
                    log("Event model written to " + modelFile);
                }
                if (getTranslationFile() != null) {
                    // TODO Remove translation file creation facility?
                    if (!getTranslationFile().exists()
                            || lastModified > getTranslationFile().lastModified()) {
                        updateTranslationFile(modelFile);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new BuildException(e);
        } catch (EventConventionException ece) {
            throw new BuildException(ece);
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    }

    private static final String MODEL2TRANSLATION = "model2translation.xsl";
    private static final String MERGETRANSLATION = "merge-translation.xsl";

    private File getParentDir(EventModel model) {
        Iterator iter = model.getProducers();
        assert iter.hasNext();
        EventProducerModel producer = (EventProducerModel) iter.next();
        assert !iter.hasNext();
        String interfaceName = producer.getInterfaceName();
        int startLocalName = interfaceName.lastIndexOf(".");
        if (startLocalName < 0) {
            return destDir;
        } else {
            String dirname = interfaceName.substring(0, startLocalName);
            dirname = dirname.replace('.', File.separatorChar);
            return new File(destDir, dirname);
        }
    }

    /**
     * Updates the translation file with new entries for newly found event producer methods.
     * @param modelFile the model file to use
     * @throws IOException if an I/O error occurs
     */
    protected void updateTranslationFile(File modelFile) throws IOException {
        try {
            boolean resultExists = getTranslationFile().exists();
            SAXTransformerFactory tFactory
                = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

            //Generate fresh generated translation file as template
            Source src = new StreamSource(modelFile.toURI().toURL().toExternalForm());
            StreamSource xslt1 = new StreamSource(
                    getClass().getResourceAsStream(MODEL2TRANSLATION));
            if (xslt1.getInputStream() == null) {
                throw new FileNotFoundException(MODEL2TRANSLATION + " not found");
            }
            DOMResult domres = new DOMResult();
            Transformer transformer = tFactory.newTransformer(xslt1);
            transformer.transform(src, domres);
            final Node generated = domres.getNode();

            Node sourceDocument;
            if (resultExists) {
                //Load existing translation file into memory (because we overwrite it later)
                src = new StreamSource(getTranslationFile().toURI().toURL().toExternalForm());
                domres = new DOMResult();
                transformer = tFactory.newTransformer();
                transformer.transform(src, domres);
                sourceDocument = domres.getNode();
            } else {
                //Simply use generated as source document
                sourceDocument = generated;
            }

            //Generate translation file (with potentially new translations)
            src = new DOMSource(sourceDocument);

            //The following triggers a bug in older Xalan versions
            //Result res = new StreamResult(getTranslationFile());
            OutputStream out = new java.io.FileOutputStream(getTranslationFile());
            out = new java.io.BufferedOutputStream(out);
            Result res = new StreamResult(out);
            try {
                StreamSource xslt2 = new StreamSource(
                        getClass().getResourceAsStream(MERGETRANSLATION));
                if (xslt2.getInputStream() == null) {
                    throw new FileNotFoundException(MERGETRANSLATION + " not found");
                }
                transformer = tFactory.newTransformer(xslt2);
                transformer.setURIResolver(new URIResolver() {
                    public Source resolve(String href, String base) throws TransformerException {
                        if ("my:dom".equals(href)) {
                            return new DOMSource(generated);
                        }
                        return null;
                    }
                });
                if (resultExists) {
                    transformer.setParameter("generated-url", "my:dom");
                }
                transformer.transform(src, res);
                if (resultExists) {
                    log("Translation file updated: " + getTranslationFile());
                } else {
                    log("Translation file generated: " + getTranslationFile());
                }
            } finally {
                IOUtils.closeQuietly(out);
            }
        } catch (TransformerException te) {
            throw new IOException(te.getMessage());
        }
    }

    /**
     * Processes the file sets defined for the task.
     * @param collector the collector to use for collecting the event producers
     * @return the time of the latest modification of any of the files inspected
     * @throws IOException if an I/O error occurs
     * @throws EventConventionException if the EventProducer conventions are violated
     * @throws ClassNotFoundException if a required class cannot be found
     */
    protected long processFileSets(EventProducerCollector collector)
            throws IOException, EventConventionException, ClassNotFoundException {
        long lastModified = 0;
        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            File directory = fs.getDir(getProject());
            for (String filename : srcFiles) {
                File src = new File(directory, filename);
                boolean eventProducerFound = collector.scanFile(src);
                if (eventProducerFound) {
                    lastModified = Math.max(lastModified, src.lastModified());
                }
            }
        }
        return lastModified;
    }

    /**
     * Adds a file set.
     * @param set the file set
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }

    /**
     * Sets the destination directory for the event models.
     *
     * @param destDir the destination directory
     */
    public void setDestDir(File destDir) {
        if (!destDir.isDirectory()) {
            throw new IllegalArgumentException("destDir must be a directory");
        }
        this.destDir = destDir;
    }

    /**
     * Sets the translation file for the event producer methods.
     * @param f the translation file
     */
    public void setTranslationFile(File f) {
        this.translationFile = f;
    }

    /**
     * Returns the translation file for the event producer methods.
     * @return the translation file
     */
    public File getTranslationFile() {
        return this.translationFile;
    }

    /**
     * Command-line interface for testing purposes.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            Project project = new Project();

            EventProducerCollectorTask generator = new EventProducerCollectorTask();
            generator.setProject(project);
            project.setName("Test");
            FileSet fileset = new FileSet();
            fileset.setDir(new File("test/java"));

            FilenameSelector selector = new FilenameSelector();
            selector.setName("**/*.java");
            fileset.add(selector);
            generator.addFileset(fileset);

            File targetDir = new File("build/codegen1");
            targetDir.mkdirs();

            generator.setTranslationFile(new File("out1.xml"));
            generator.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
