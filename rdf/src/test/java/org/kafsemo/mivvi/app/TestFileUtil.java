/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright (C) 2004, 2005, 2006, 2010  Joseph Walton
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.app;

import java.io.File;

import org.kafsemo.mivvi.app.FileUtil;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import junit.framework.TestCase;

/**
 * @author joe
 */
public class TestFileUtil extends TestCase
{
    private static String asNative(String s)
    {
        StringBuffer sb = new StringBuffer(s.length());

        char[] ca = s.toCharArray();
        for (int i = 0 ; i < ca.length ; i++) {
            if (ca[i] == '/')
                sb.append(File.separatorChar);
            else
                sb.append(ca[i]);
        }
        
        return sb.toString();
    }

    private static boolean contains(String a, String b)
    {
        return FileUtil.contains(new File(asNative(a)), new File(asNative(b)));
    }

    public void testContains()
    {
        assertFalse(contains("", ""));
        assertFalse(contains("a", "ab"));
        assertTrue(contains("", "x"));

        assertFalse(contains("a", ""));
        assertFalse(contains("a", "a"));
        assertFalse(contains("a", "a/"));
        assertFalse(contains("a", "x"));
        assertFalse(contains("a/b", "a/x"));
        assertFalse(contains("a/b", "a/"));

        assertTrue(contains("", "x"));
        assertFalse(contains("a", "x"));
        assertTrue(contains("a", "a/b"));
        assertTrue(contains("a", "a/b/c"));
        assertTrue(contains("a/", "a/b/c"));
        assertTrue(contains("a/b", "a/b/c"));
    }
    
    public void testFileFrom()
    {
        Value v = new LiteralImpl("Just a string");
        assertNull(FileUtil.fileFrom(v));
        
        v = new URIImpl("http://www.example.com/");
        assertNull(FileUtil.fileFrom(v));
        
        File d = new File("").getAbsoluteFile();
        
        v = new URIImpl(d.toURI().toString());
        assertEquals(d, FileUtil.fileFrom(v));
    }
}