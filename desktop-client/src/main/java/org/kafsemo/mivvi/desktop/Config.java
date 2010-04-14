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

package org.kafsemo.mivvi.desktop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.kafsemo.mivvi.app.TokenSetFile;
import org.kafsemo.mivvi.app.UriSetFile;

public class Config
{
    public static Config getInstance() throws IOException, URISyntaxException
    {
        return new Config();
    }
    
    private final String radiotimesUrl;
    
    private final String[] dataUrls;

    Config() throws IOException, URISyntaxException
    {
        InputStream in = getClass().getResourceAsStream("Mivvi.properties");
        if (in == null)
            throw new FileNotFoundException("Internal resource 'Mivvi.properties' not found");

        Properties p = new Properties();
        p.load(in);
        
        in.close();
        
        this.radiotimesUrl = new URI(p.getProperty("radiotimes.url")).toString();

        List<String> l = new ArrayList<String>();

        int i = 0;
        String s;
        while ((s = p.getProperty("mivvidata.url." + i)) != null) {
            l.add(new URI(s).toString());
            i++;
        }
        
        this.dataUrls = l.toArray(new String[l.size()]);
    }

    public UriSetFile getUriSetFile(String collectionName)
    {
        return new UriSetFile(new File(Const.BASE, collectionName + ".uris"));
    }
    
    public TokenSetFile getTokenSetFile(String collectionName)
    {
        return new TokenSetFile(new File(Const.BASE, collectionName + ".tokens"));
    }
    
    public File getCacheDirectory()
    {
        return new File(Const.BASE, "webcache");
    }
    
    public String getRadioTimesBaseUrl()
    {
        return radiotimesUrl;
    }

    public String[] getMivviDataUrls() throws MalformedURLException
    {
        return dataUrls;
    }

    public File getFeedListFile()
    {
        return new File(Const.BASE, "feeds.xml");
    }
}
