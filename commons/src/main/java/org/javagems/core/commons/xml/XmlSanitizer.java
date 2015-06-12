package org.javagems.core.commons.xml;

/**
 * Java Gems
 * Licensed under the MIT license.
 *
 * Copyright (c) 2013, Christophe Cassagnabere
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * The Software is provided "as is", without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders X be liable for any claim, damages or other
 * liability, whether in an action of contract, tort or otherwise, arising from,
 * out of or in connection with the software or the use or other dealings in
 * the Software.
 *
 * Except as contained in this notice, the name of the copyright holders shall
 * not be used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from the
 * copyright holders.
 */

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 * The class will attempt to <em>clean</em> the XML string the best it can, 
 * removing illegal characters (as defined by version 1.0 of the XML spec).
 * 
 */
public class XmlSanitizer {

    private final static Pattern PROCESSING_INSTRUCTIONS_REMOVAL_PATTERN =
            Pattern.compile("(?:^<\\?(?:!xml)+.*?>)|(?:.<\\?.*?>)", Pattern.DOTALL);
    private String xml = null;
    private boolean isSanitized;

    public XmlSanitizer(String xml) {
        this.xml = xml;
        this.isSanitized = false;
    }

    public XmlSanitizer(File xmlFile) throws Exception {
        this(xmlFile, "utf-8");
    }

    public XmlSanitizer(File xmlFile, String encoding) throws Exception {
        this(FileUtils.readFileToString(xmlFile, encoding));
    }

    public boolean isSanitized() {
        return this.isSanitized;            
    }
    
    public String sanitize() {
        if (!this.isSanitized && this.xml != null) {
            cleanByteOrderMarks();
            removeInvalidXMLCharacters();
            cleanProcessingInstructions();
            this.isSanitized = true;
        }
        return this.xml;
    }

    /**
     * Byte Order Marks can generally be detected by the JVM as long as the
     * correct encoding is used. SaxReader can detect them. However, UTF-8
     * BOMS (via Windows notepad) cause problems as do UTF16 (both BE/LE) when
     * not given correct encodings. If the Xml file/inputStream is converted to
     * an internal Java String without proper encoding, SaxReader will fail as
     * it expects the first actual character to be the opening declaration or
     * root node ('<').
     */
    public void cleanByteOrderMarks() {
        if (this.xml.codePointAt(0) == '\uFEFF') {
            this.xml = this.xml.substring(1);
        } else {
            char[] bom = new char[3];
            this.xml.getChars(0, 3, bom, 0);
            if ((bom[0] == '\u00ef') && (bom[1] == '\u00bb') && (bom[2] == '\u00bf')) {
                this.xml = this.xml.substring(3);
            } else if ((bom[0] == '\ufffd' && bom[1] == '\ufffd')
                    || (bom[0] == '\u00fe' && bom[1] == '\u00ff')
                    || (bom[0] == '\u00ff' && bom[1] == '\u00fe')) {
                this.xml = this.xml.substring(2);
            }
        }
    }

    /**
     * Scans through the xml string, removing all processing instructions,
     * including extra declarations. If will not remove a valid declaration 
     * ('<?xml>') if found at the very beginning of the string.
     */
    protected void cleanProcessingInstructions() {
        Matcher matcher = PROCESSING_INSTRUCTIONS_REMOVAL_PATTERN.matcher(this.xml);
        this.xml = matcher.replaceAll("");
    }

    /**
     * Ensures that the output String has only valid XML unicode characters as
     * specified by the XML 1.0 standard. For reference, please see the
     * standard.
     *
     * @author Donoiu Cristian, GPL
     */
    private void removeInvalidXMLCharacters() {
        StringBuilder sb = new StringBuilder();
        int codePoint;
        int i = 0;
        while (i < this.xml.length()) {
            codePoint = this.xml.codePointAt(i);
            if ((codePoint == 0x9)
                    || (codePoint == 0xA)
                    || (codePoint == 0xD)
                    || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                    || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                    || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
                sb.append(Character.toChars(codePoint));
            }
            i += Character.charCount(codePoint);
        }
        this.xml = sb.toString();
    }
}
