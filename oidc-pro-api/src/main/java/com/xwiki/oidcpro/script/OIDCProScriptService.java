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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.oidc.auth.OIDCAuthServiceImpl;
import org.xwiki.contrib.oidc.auth.internal.OIDCAuthService;
import org.xwiki.contrib.oidc.auth.internal.OIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.store.OIDCClientsConfigurationStore;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xwiki.oidcpro.OIDCProClientConfiguration;
import com.xwiki.oidcpro.internal.ConfigurationStore;

/**
 * Provides methods related to the OIDC Pro implementation accessible in the xwiki platform.
 *
 * @version $Id$
 */
@Component
@Singleton
@Named("oidcpro")
public class OIDCProScriptService implements ScriptService
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ConfigurationStore configurationStore;

    @Inject
    private OIDCClientsConfigurationStore oidcStore;

    @Inject
    private ConfigurationSource xwikiProperties;

    @Inject
    private OIDCClientConfiguration oidcClientConfiguration;

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

        return authService instanceof OIDCAuthServiceImpl || authService instanceof OIDCAuthService;
    }

    /**
     * @return the OIDC configurations of the current wiki.
     */
    public List<OIDCProClientConfiguration> getConfigurations()
    {
        return configurationStore.getConfigurations();
    }

    /**
     * @param clientName the OIDC client name.
     * @return true if the provided client is used for authentication by default. False is some other client is used by
     *     default for authentication.
     */
    public boolean isDefaultClient(String clientName)
    {
        return StringUtils.isNotEmpty(clientName) && clientName.equals(
            getConfigProperty(oidcStore.getDefaultClientConfiguration(),
                OIDCClientConfiguration.DEFAULT_CLIENT_CONFIGURATION_PROPERTY,
                OIDCClientConfiguration.DEFAULT_CLIENT_CONFIGURATION));
    }

    /**
     * @return the name of the cookie used to select the OIDC client configuration that will be used for
     *     authentication.
     */
    public String getClientConfigurationCookie()
    {
        return getConfigProperty(oidcStore.getClientConfigurationCookie(),
            OIDCClientConfiguration.CLIENT_CONFIGURATION_COOKIE_PROPERTY,
            OIDCClientConfiguration.DEFAULT_OIDC_CONFIGURATION_COOKIE);
    }

    /**
     * Forget the OIDC client configuration selected in the current session. The OIDC authenticator stores the client
     * read from the {@link #getClientConfigurationCookie() cookie} in the session and ignores the cookie afterwards, so
     * the session value must be cleared for a new selection to be taken into account.
     */
    public void resetClientConfigurationSelection()
    {
        oidcClientConfiguration.removeSessionAttribute(OIDCClientConfiguration.DEFAULT_CLIENT_CONFIGURATION_PROPERTY);
    }

    private String getConfigProperty(String wikiValue, String property, String defaultValue)
    {
        if (StringUtils.isNotEmpty(wikiValue)) {
            return wikiValue;
        }
        return xwikiProperties.getProperty(property, defaultValue);
    }
}
