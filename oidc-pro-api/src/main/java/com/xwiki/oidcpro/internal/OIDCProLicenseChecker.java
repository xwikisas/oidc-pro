/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.oidcpro.internal;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xwiki.licensing.Licensor;

/**
 * Check whether OIDC Pro is licensed on the current wiki.
 * <p>
 * Without a license, only the OIDC Pro features are turned off: users keep logging in with the OpenID Connect
 * authenticator, which reads the same client configurations.
 *
 * @version $Id$
 */
@Component(roles = OIDCProLicenseChecker.class)
@Singleton
public class OIDCProLicenseChecker
{
    private static final LocalDocumentReference APP_HOME = new LocalDocumentReference("OIDCPro", "WebHome");

    @Inject
    private Licensor licensor;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Logger logger;

    /**
     * The license is checked on a page of the application, so that it applies to the wiki OIDC Pro is installed on.
     *
     * @return {@code true} if there is a valid license for OIDC Pro on the current wiki
     */
    public boolean isLicensed()
    {
        XWikiContext context = this.contextProvider.get();
        DocumentReference appHome = new DocumentReference(APP_HOME, context.getWikiReference());
        try {
            // The licensor accepts the pages that belong to no licensed extension, so a missing page must not be
            // enough.
            return context.getWiki().exists(appHome, context) && this.licensor.hasLicensure(appHome);
        } catch (XWikiException e) {
            this.logger.warn("Failed to check the license of OIDC Pro on [{}].", appHome, e);
            return false;
        }
    }
}
