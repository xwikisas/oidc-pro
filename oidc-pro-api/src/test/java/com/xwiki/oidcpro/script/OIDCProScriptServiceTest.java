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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.oidc.auth.OIDCAuthServiceImpl;
import org.xwiki.contrib.oidc.auth.internal.OIDCAuthService;
import org.xwiki.contrib.oidc.auth.internal.OIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.store.OIDCClientsConfigurationStore;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryException;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xwiki.oidcpro.OIDCProClientConfiguration;
import com.xwiki.oidcpro.internal.ConfigurationStore;
import com.xwiki.oidcpro.internal.OIDCProLicenseChecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

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

    @MockComponent
    private OIDCProLicenseChecker licenseChecker;

    @MockComponent
    private ContextualAuthorizationManager contextualAuthorizationManager;

    private XWiki wiki;

    private XWikiContext context;

    @BeforeEach
    void setUp()
    {
        this.context = mock(XWikiContext.class);
        this.wiki = mock(XWiki.class);
        when(this.context.getWiki()).thenReturn(this.wiki);
        when(this.contextProvider.get()).thenReturn(this.context);
    }

    @Test
    void isOIDCAuthServiceEnabled()
    {
        when(this.wiki.getAuthService()).thenReturn(mock(OIDCAuthServiceImpl.class));

        assertTrue(this.scriptService.isOIDCAuthServiceEnabled());
    }

    @Test
    void isOIDCAuthServiceEnabledWithOIDCAuthService()
    {
        when(this.wiki.getAuthService()).thenReturn(mock(OIDCAuthService.class));

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
        OIDCProClientConfiguration configuration = mock(OIDCProClientConfiguration.class);
        when(configuration.getConfigurationName()).thenReturn("EntraID");
        when(this.configurationStore.getConfigurations()).thenReturn(List.of(configuration));
        when(this.licenseChecker.isLicensed()).thenReturn(true);

        List<SafeOIDCProClientConfiguration> configurations = this.scriptService.getConfigurations();

        assertEquals(1, configurations.size());
        assertEquals("EntraID", configurations.get(0).getConfigurationName());
    }

    @Test
    void isLicensed()
    {
        when(this.licenseChecker.isLicensed()).thenReturn(true);

        assertTrue(this.scriptService.isLicensed());
    }

    @Test
    void getConfigurationsWithoutLicense()
    {
        when(this.configurationStore.getConfigurations()).thenReturn(List.of(mock(OIDCProClientConfiguration.class)));

        assertEquals(List.of(), this.scriptService.getConfigurations());
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

    @Test
    void getTemplateReference() throws Exception
    {
        DocumentReference templateReference = new DocumentReference("xwiki", "Templates", "Entra");
        when(this.configurationStore.getTemplateReference("entra")).thenReturn(templateReference);

        assertSame(templateReference, this.scriptService.getTemplateReference("entra"));
    }

    @Test
    void getTemplateReferenceWhenStoreFails() throws Exception
    {
        when(this.configurationStore.getTemplateReference("entra"))
            .thenThrow(new QueryException("error", null, null));

        assertNull(this.scriptService.getTemplateReference("entra"));
        assertEquals("Failed to retrieve the OIDC Pro template [entra].", this.logCapture.getMessage(0));
    }

    @Test
    void getTemplateReferences() throws Exception
    {
        List<DocumentReference> templateReferences =
            List.of(new DocumentReference("xwiki", "Templates", "Entra"));
        when(this.configurationStore.getTemplateReferences()).thenReturn(templateReferences);

        assertSame(templateReferences, this.scriptService.getTemplateReferences());
    }

    @Test
    void getTemplateReferencesWhenStoreFails() throws Exception
    {
        when(this.configurationStore.getTemplateReferences()).thenThrow(new QueryException("error", null, null));

        assertTrue(this.scriptService.getTemplateReferences().isEmpty());
        assertEquals("Failed to retrieve the OIDC Pro templates.", this.logCapture.getMessage(0));
    }

    @Test
    void isTrustedConfigurationDocument() throws Exception
    {
        DocumentReference configurationReference = new DocumentReference("xwiki", "OIDCPro", "EntraID");
        when(this.configurationStore.isTrustedConfigurationDocument(configurationReference)).thenReturn(true);

        assertTrue(this.scriptService.isTrustedConfigurationDocument(configurationReference));
    }

    @Test
    void isTrustedConfigurationDocumentWhenStoreFails() throws Exception
    {
        DocumentReference configurationReference = new DocumentReference("xwiki", "OIDCPro", "EntraID");
        when(this.configurationStore.isTrustedConfigurationDocument(configurationReference))
            .thenThrow(new XWikiException());

        assertFalse(this.scriptService.isTrustedConfigurationDocument(configurationReference));
        assertEquals("Failed to check if the OIDC client configuration document [xwiki:OIDCPro.EntraID] is trusted.",
            this.logCapture.getMessage(0));
    }

    @Test
    void getMainWikiConfigurations() throws Exception
    {
        when(this.licenseChecker.isLicensed()).thenReturn(true);
        when(this.contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(true);
        when(this.context.getWikiId()).thenReturn("subwiki");
        when(this.context.getMainXWiki()).thenReturn("xwiki");
        when(this.oidcStore.getDefaultClientConfiguration()).thenReturn("EntraID");
        BaseObject defaultConfiguration = mock(BaseObject.class);
        when(defaultConfiguration.getStringValue("configurationName")).thenReturn("EntraID");
        BaseObject otherConfiguration = mock(BaseObject.class);
        when(otherConfiguration.getStringValue("configurationName")).thenReturn("Other");
        when(this.configurationStore.getClientConfigurationObjects())
            .thenReturn(List.of(defaultConfiguration, otherConfiguration));

        List<ReadOnlyOIDCClientConfiguration> configurations = this.scriptService.getMainWikiConfigurations();

        assertEquals(2, configurations.size());
        assertEquals("EntraID", configurations.get(0).getConfigurationName());
        assertTrue(configurations.get(0).isDefault());
        assertFalse(configurations.get(1).isDefault());
        // The configurations and the default client are read on the main wiki, then the current wiki is restored.
        InOrder inOrder = inOrder(this.context, this.oidcStore, this.configurationStore);
        inOrder.verify(this.context).setWikiId("xwiki");
        inOrder.verify(this.oidcStore).getDefaultClientConfiguration();
        inOrder.verify(this.configurationStore).getClientConfigurationObjects();
        inOrder.verify(this.context).setWikiId("subwiki");
    }

    @Test
    void getMainWikiConfigurationsWithoutLicense()
    {
        when(this.contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(true);

        assertTrue(this.scriptService.getMainWikiConfigurations().isEmpty());
        verify(this.context, never()).setWikiId(any());
    }

    @Test
    void getMainWikiConfigurationsWithoutAdminRight()
    {
        when(this.licenseChecker.isLicensed()).thenReturn(true);

        assertTrue(this.scriptService.getMainWikiConfigurations().isEmpty());
        verify(this.context, never()).setWikiId(any());
    }

    @Test
    void getMainWikiConfigurationsWhenStoreFails() throws Exception
    {
        when(this.licenseChecker.isLicensed()).thenReturn(true);
        when(this.contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(true);
        when(this.context.getWikiId()).thenReturn("subwiki");
        when(this.context.getMainXWiki()).thenReturn("xwiki");
        when(this.configurationStore.getClientConfigurationObjects()).thenThrow(new QueryException("error", null, null));

        assertTrue(this.scriptService.getMainWikiConfigurations().isEmpty());
        assertEquals("Failed to retrieve the OIDC client configurations of the main wiki.",
            this.logCapture.getMessage(0));
        verify(this.context).setWikiId("subwiki");
    }
}
