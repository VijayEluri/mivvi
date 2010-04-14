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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

public class TestSeriesData
{
    public static SeriesData fromGraph(Graph g) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
    {
        MemoryStore ms = new MemoryStore();
        SailRepository sr = new SailRepository(ms);
        sr.initialize();
        RepositoryConnection cn = sr.getConnection();
        
        for (Statement st : g) {
            cn.add(st);
        }

        SeriesData sd = new SeriesData();
        sd.initMviRepository(sr);
        
        return sd;
    }
    
    @Test
    public void getIconForResource() throws Exception
    {
        Graph g = new GraphImpl();
        
        /* A directly defined icon */
        g.add(new URIImpl("http://www.example.com/#"),
                RdfMiscVocabulary.smIcon,
                new URIImpl("file:///example-icon.png"));
        
        SeriesData sd = fromGraph(g);

        List<? extends URI> expected = Arrays.asList(
                new URIImpl("file:///example-icon.png"));
        
        List<URI> icons = sd.getResourceIcons(new URIImpl("http://www.example.com/#"));

        assertEquals(expected, icons);
    }
    
    @Test
    public void getIconByTypeForResource() throws Exception
    {
        Graph g = new GraphImpl();
        
        /* It has a type... */
        g.add(new URIImpl("http://www.example.com/#"),
                RdfUtil.Rdf.type,
                RdfUtil.Mvi.Series);

        /* ...and that type has an icon */
        g.add(RdfUtil.Mvi.Series,
                RdfMiscVocabulary.smIcon,
                new URIImpl("file:///generic-series-icon.png"));
        
        SeriesData sd = fromGraph(g);

        List<? extends URI> expected = Arrays.asList(
                new URIImpl("file:///generic-series-icon.png"));
        
        List<URI> icons = sd.getResourceIcons(new URIImpl("http://www.example.com/#"));

        assertEquals(expected, icons);
    }

    @Test
    public void getIconReturnsNullIfNoIcon() throws Exception
    {
        Graph g = new GraphImpl();
        
        /* It has a type... */
        g.add(new URIImpl("http://www.example.com/#"),
                RdfUtil.Rdf.type,
                RdfUtil.Mvi.Series);

        /* ...but no icon for that type */
        
        SeriesData sd = fromGraph(g);

        List<URI> icons = sd.getResourceIcons(new URIImpl("http://www.example.com/#"));

        assertEquals(Collections.emptyList(), icons);
    }
}