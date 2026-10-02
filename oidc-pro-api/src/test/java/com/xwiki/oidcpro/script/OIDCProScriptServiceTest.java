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

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.oidc.auth.OIDCAuthServiceImpl;
import org.xwiki.contrib.oidc.auth.internal.OIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.store.OIDCClientsConfigurationStore;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xwiki.oidcpro.OIDCProClientConfiguration;
import com.xwiki.oidcpro.internal.ConfigurationStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OIDCProScriptService}.
 *
 * @version $Id$
 */
@ComponentTest
class OIDCProScriptServiceTest
{
    @InjectMockComponents
    private OIDCProScriptService scriptService;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @MockComponent
    private ConfigurationStore configurationStore;

    @MockComponent
    private OIDCClientsConfigurationStore oidcStore;

    @MockComponent
    private ConfigurationSource xwikiProperties;

    @MockComponent
    private OIDCClientConfiguration oidcClientConfiguration;

    private XWiki wiki;

    @BeforeEach
    void setUp()
    {
        XWikiContext context = mock(XWikiContext.class);
        this.wiki = mock(XWiki.class);
        when(context.getWiki()).thenReturn(this.wiki);
        when(this.contextProvider.get()).thenReturn(context);
    }

    @Test
    void isOIDCAuthServiceEnabled()
    {
        when(this.wiki.getAuthService()).thenReturn(mock(OIDCAuthServiceImpl.class));

        assertTrue(this.scriptService.isOIDCAuthServiceEnabled());
    }

    @Test
    void isOIDCAuthServiceEnabledWithAnotherAuthService()
    {
        when(this.wiki.getAuthService()).thenReturn(mock(XWikiAuthService.class));

        assertFalse(this.scriptService.isOIDCAuthServiceEnabled());
    }

    @Test
    void isOIDCAuthServiceEnabledWithoutContext()
    {
        when(this.contextProvider.get()).thenReturn(null);

        assertFalse(this.scriptService.isOIDCAuthServiceEnabled());
    }

    @Test
    void getConfigurations()
    {
        List<OIDCProClientConfiguration> configurations = List.of(mock(OIDCProClientConfiguration.class));
        when(this.configurationStore.getConfigurations()).thenReturn(configurations);

        assertSame(configurations, this.scriptService.getConfigurations());
    }

    @Test
    void isDefaultClientFromWikiConfiguration()
    {
        when(this.oidcStore.getDefaultClientConfiguration()).thenReturn("EntraID");

        assertTrue(this.scriptService.isDefaultClient("EntraID"));
        assertFalse(this.scriptService.isDefaultClient("Keycloak"));
        assertFalse(this.scriptService.isDefaultClient(""));
        assertFalse(this.scriptService.isDefaultClient(null));
    }

    @Test
    void isDefaultClientFromXWikiProperties()
    {
        when(this.xwikiProperties.getProperty(OIDCClientConfiguration.DEFAULT_CLIENT_CONFIGURATION_PROPERTY,
            OIDCClientConfiguration.DEFAULT_CLIENT_CONFIGURATION)).thenReturn("Keycloak");

        assertTrue(this.scriptService.isDefaultClient("Keycloak"));
        assertFalse(this.scriptService.isDefaultClient("EntraID"));
    }

    @Test
    void getClientConfigurationCookieFromWikiConfiguration()
    {
        when(this.oidcStore.getClientConfigurationCookie()).thenReturn("myCookie");

        assertEquals("myCookie", this.scriptService.getClientConfigurationCookie());
    }

    @Test
    void getClientConfigurationCookieFromXWikiProperties()
    {
        when(this.xwikiProperties.getProperty(OIDCClientConfiguration.CLIENT_CONFIGURATION_COOKIE_PROPERTY,
            OIDCClientConfiguration.DEFAULT_OIDC_CONFIGURATION_COOKIE)).thenReturn("oidcProvider");

        assertEquals("oidcProvider", this.scriptService.getClientConfigurationCookie());
    }

    @Test
    void resetClientConfigurationSelection()
    {
        this.scriptService.resetClientConfigurationSelection();

        verify(this.oidcClientConfiguration)
            .removeSessionAttribute(OIDCClientConfiguration.DEFAULT_CLIENT_CONFIGURATION_PROPERTY);
    }
}
