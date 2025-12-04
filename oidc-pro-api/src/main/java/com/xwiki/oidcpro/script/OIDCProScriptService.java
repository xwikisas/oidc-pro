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

package com.xwiki.oidcpro.script;

import javax.inject.Inject;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.oidc.auth.OIDCAuthServiceImpl;
import org.xwiki.contrib.oidc.auth.internal.OIDCAuthService;
import org.xwiki.script.service.ScriptService;
import org.xwiki.security.authservice.internal.DefaultXWikiAuthServiceComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiAuthService;

/**
 * Provides methods related to the OIDC Pro implementation accessible in the xwiki platform.
 *
 * @version $Id$
 */
@Component
public class OIDCProScriptService implements ScriptService
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    /**
     * @return true if the current xwiki authenticator is OIDC.
     */
    public boolean isOIDCAuthServiceEnabled()
    {
        XWikiContext context = contextProvider.get();
        if (context == null) {
            return false;
        }

        XWikiAuthService authService = context.getWiki().getAuthService();

        try {
            authService = authService instanceof DefaultXWikiAuthServiceComponent ?
                ((DefaultXWikiAuthServiceComponent) authService).getAuthService() : authService;
        } catch (XWikiException e) {
            return false;
        }

        return authService instanceof OIDCAuthServiceImpl || authService instanceof OIDCAuthService;
    }
}
