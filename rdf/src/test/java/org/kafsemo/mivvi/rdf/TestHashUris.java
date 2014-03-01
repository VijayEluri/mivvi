/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2014 Joseph Walton
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

package org.kafsemo.mivvi.rdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.kafsemo.mivvi.rdf.HashUris;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


public class TestHashUris extends TestCase
{
    private static String asString(Collection<?> a)
    {
        List<String> l = new ArrayList<String>(a.size());
        for (Object o : a) {
            l.add(o.toString());
        }
        Collections.sort(l);
        
        return l.toString();
    }

    private static void assertEquals(Collection<?> a, Collection<?> b)
    {
        assertEquals(asString(a), asString(b));
    }

    private static ReadableByteChannel wrap(byte[] ba)
    {
        return Channels.newChannel(new ByteArrayInputStream(ba));
    }
    
    /**
     * Verify that the expected hashes are produced for the empty file.
     * 
     * @throws IOException
     */
    public void testHashEmptyFile() throws IOException
    {
        File tf = File.createTempFile("hash", "tmp");
        tf.deleteOnExit();
        
        Collection<URI> c = HashUris.digest(tf);
        assertNotNull(c);
        
        Set<Resource> expected = new HashSet<Resource>(Arrays.asList(new Resource[]{
                new URIImpl("urn:md5:2QOYZWMPACZAJ2MABGMOZ6CCPY"),
                new URIImpl("urn:sha1:3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ")
        }));
        
        assertEquals(expected, new HashSet<Resource>(c));
        
        c = HashUris.digestStream(wrap(new byte[0]));
        assertNotNull(c);
        assertEquals(expected, new HashSet<Resource>(c));
    }

    /**
     * Verify that the expected hashes are produced for a file with contents.
     * 
     * @throws IOException
     */
    public void testHashNonEmptyFile() throws IOException
    {
        byte[] ba = new byte[196609]; // Three 64k blocks + 1
        
        for (int i = 0 ; i < ba.length ; i++) {
            ba[i] = (byte) (i % 0x100);
        }
        
        Collection<URI> c = HashUris.digestStream(wrap(ba));
        assertNotNull(c);
        
        Set<URI> expected = new HashSet<URI>(Arrays.asList(new URI[]{
                new URIImpl("urn:md5:27JYG2USBLM33O6WE5W7M5ZESA"),
                new URIImpl("urn:sha1:GNRFWBAU42M5CELITECKMR6UZBZJH2SU")
        }));
        
        assertEquals(expected, new HashSet<URI>(c));
    }
    
    public void testIsHashUri()
    {
        assertTrue(HashUris.isHashUri("urn:sha1:"));
        assertTrue(HashUris.isHashUri("urn:md5:"));
        
        assertFalse(HashUris.isHashUri("urn:sha1"));
        assertFalse(HashUris.isHashUri("urn:md5"));
        assertFalse(HashUris.isHashUri("http://www.example.com/"));
        
        assertFalse(HashUris.isHashUri((Value) null));
    }
    
    public void testReplaceHashUris() throws RepositoryException
    {
        Resource f1 = new URIImpl("file:///file1"),
            f2 = new URIImpl("file:///file2");

        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        
        RepositoryConnection cn = rep.getConnection();
        
        Resource m1 = new URIImpl("urn:md5:2QOYZWMPACZAJ2MABGMOZ6CCPY"),
            s1 = new URIImpl("urn:sha1:3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ"),
            m2 = new URIImpl("urn:md5:27JYG2USBLM33O6WE5W7M5ZESA"),
            s2 = new URIImpl("urn:sha1:GNRFWBAU42M5CELITECKMR6UZBZJH2SU"),
            
            asin = new URIImpl("urn:x-asin:");
        
        Literal lit = new LiteralImpl("The Title");
            
        // Initially, we have partial statements about two separate files
        cn.add(f1, RdfUtil.Dc.identifier, m1);
        cn.add(f2, RdfUtil.Dc.identifier, s1);
        cn.add(f1, RdfUtil.Dc.title, lit); // Titles shouldn't be touched
        cn.add(f1, RdfUtil.Dc.identifier, asin); // Non-hash identifiers shouldn't be touched

        assertTrue(cn.hasStatement(f1, RdfUtil.Dc.identifier, m1, false));
        assertTrue(cn.hasStatement(f2, RdfUtil.Dc.identifier, s1, false));
        assertTrue(cn.hasStatement(f1, RdfUtil.Dc.title, lit, false));
        assertTrue(cn.hasStatement(f1, RdfUtil.Dc.identifier, asin, false));
        
        Collection<Resource> newHashes = Arrays.asList(new Resource[]{m2, s2});
        
        HashUris.replaceHashUris(cn, f1, newHashes);
        
        assertFalse(cn.hasStatement(f1, RdfUtil.Dc.identifier, m1, false));
        assertTrue(cn.hasStatement(f2, RdfUtil.Dc.identifier, s1, false));
        assertTrue(cn.hasStatement(f1, RdfUtil.Dc.title, lit, false));
        assertTrue(cn.hasStatement(f1, RdfUtil.Dc.identifier, asin, false));
        
        assertTrue(cn.hasStatement(f1, RdfUtil.Dc.identifier, m2, false));
        assertTrue(cn.hasStatement(f1, RdfUtil.Dc.identifier, s2, false));
    }
}
