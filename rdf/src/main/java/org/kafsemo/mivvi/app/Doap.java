/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2016 Joseph Walton
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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.app;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.kafsemo.mivvi.rdf.RdfUtil;

public class Doap
{
    public static final IRI MIVVI_DESKTOP_CLIENT = SimpleValueFactory.getInstance().createIRI("http://mivvi.net/code/#desktop-client");
    private static final IRI DOWNLOAD_PAGE = SimpleValueFactory.getInstance().createIRI("http://usefulinc.com/ns/doap#download-page");

    private final Version latest;
    private final String downloadUrl;

    private Doap(Version latest, String downloadUrl)
    {
        this.latest = latest;
        this.downloadUrl = downloadUrl;
    }

    public static Doap check(RepositoryConnection cn)
        throws RepositoryException
    {
        return new Doap(getLatestAvailableVersion(cn),
                getDownloadPage(cn));
    }

    public Version getLatestAvailableVersion()
    {
        return latest;
    }

    public String getDownloadPage()
    {
        return downloadUrl;
    }

    public static Version getLatestAvailableVersion(RepositoryConnection cn)
        throws RepositoryException
    {
        List<Version> allVersions = new ArrayList<Version>();

        RepositoryResult<Statement> res = cn.getStatements(MIVVI_DESKTOP_CLIENT, RdfUtil.Doap.release, null, false);

        while (res.hasNext()) {
            Statement stmt = res.next();

            Resource v = RdfUtil.asResource(stmt.getObject());
            if (v != null) {
                String rev = RdfUtil.getStringProperty(cn, v, RdfUtil.Doap.revision);
                if (rev != null) {
                    try {
                        allVersions.add(Version.parse(rev));
                    } catch (ParseException pe) {
                        // Ignore this version
                    }
                }
            }
        }

        Collections.sort(allVersions);

        if (allVersions.size() > 0) {
            return allVersions.get(allVersions.size() - 1);
        } else {
            return null;
        }
    }

    public static String getDownloadPage(RepositoryConnection cn) throws RepositoryException
    {
        Resource r = RdfUtil.getResProperty(cn, MIVVI_DESKTOP_CLIENT, DOWNLOAD_PAGE);
        if (r instanceof IRI) {
            return ((IRI)r).toString();
        } else {
            return null;
        }
    }
}
