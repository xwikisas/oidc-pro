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
package com.xwiki.oidcpro.test.ui;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import com.xwiki.oidcpro.test.po.AuthorizationRequest;
import com.xwiki.oidcpro.test.po.OIDCProAdministrationPage;
import com.xwiki.oidcpro.test.po.OIDCProLoginMenu;
import com.xwiki.oidcpro.test.po.OIDCProLoginPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional tests for OIDC Pro.
 * <p>
 * The tests make OpenID Connect the active authentication service, but they don't need a real provider: the
 * authorization endpoint of each client points to a page of the wiki, and the tests check the authorization request
 * the browser is redirected to.
 *
 * @version $Id$
 */
@UITest
class OIDCProIT
{
    private static final LocalDocumentReference AUTH_SERVICE_CONFIGURATION =
        new LocalDocumentReference(List.of("XWiki", "AuthService"), "Configuration");

    private static final LocalDocumentReference CLIENTS_CONFIGURATION =
        new LocalDocumentReference(List.of("XWiki", "OIDC"), "ClientsConfiguration");

    private static final String CLIENT_CONFIGURATION_CLASS = "XWiki.OIDC.ClientConfigurationClass";

    private static final String CLIENT_ID_PARAMETER = "client_id";

    @BeforeAll
    static void beforeAll(TestUtils setup) throws Exception
    {
        setup.loginAsSuperAdmin();
        setup.deletePage(AUTH_SERVICE_CONFIGURATION);
        setup.addObject(AUTH_SERVICE_CONFIGURATION, "XWiki.AuthService.ConfigurationClass", "authService", "oidc");
    }

    @AfterAll
    static void afterAll(TestUtils setup)
    {
        // Go back to the standard authentication service.
        setup.loginAsSuperAdmin();
        setup.deletePage(AUTH_SERVICE_CONFIGURATION);
    }

    @Test
    void administration(TestUtils setup, TestReference testReference)
    {
        setup.loginAsSuperAdmin();
        String otherClient = createClient(setup, testReference, "Other", true);
        String client = testReference.getLastSpaceReference().getName() + "-Admin";
        String endpoint = getAuthorizationEndpoint(setup, testReference, "Admin");

        // Create a configuration from the Default template.
        OIDCProAdministrationPage.gotoPage().createConfiguration()
            .setConfigurationName(client)
            .setClientId("admin-client")
            .setClientSecret("secret")
            .setAuthorizationEndpoint(endpoint)
            .setEnabled(true)
            .clickSaveAndView();

        OIDCProAdministrationPage administration = OIDCProAdministrationPage.gotoPage();
        assertTrue(administration.hasConfiguration(client));
        assertTrue(administration.isEnabled(client));
        assertFalse(administration.isDefault(client));

        // Disable the configuration, then enable it again.
        administration.toggleEnabled(client);
        administration = OIDCProAdministrationPage.gotoPage();
        assertFalse(administration.isEnabled(client));
        administration.toggleEnabled(client);
        administration = OIDCProAdministrationPage.gotoPage();
        assertTrue(administration.isEnabled(client));

        // Only one configuration can be the default one.
        administration.setDefault(client);
        administration = OIDCProAdministrationPage.gotoPage();
        assertTrue(administration.isDefault(client));
        assertFalse(administration.isDefault(otherClient));
        administration.setDefault(otherClient);
        administration = OIDCProAdministrationPage.gotoPage();
        assertFalse(administration.isDefault(client));
        assertTrue(administration.isDefault(otherClient));
    }

    @Test
    void loginWithClient(TestUtils setup, TestReference testReference)
    {
        setup.loginAsSuperAdmin();
        // Without a usable default client, the authenticator asks the user to pick one.
        setup.updateObject(CLIENTS_CONFIGURATION, "XWiki.OIDC.ClientsConfigurationClass", 0,
            "defaultClientConfiguration", "");
        String firstClient = createClient(setup, testReference, "First", true);
        String secondClient = createClient(setup, testReference, "Second", true);
        String disabledClient = createClient(setup, testReference, "Disabled", false);
        setup.createPage(testReference, "", "");
        String pageURL = setup.getURL(testReference, "view", null);

        // The provider selection page lists the enabled clients, with the icon of their template.
        setup.forceGuestUser();
        OIDCProLoginPage loginPage = OIDCProLoginPage.gotoPage(pageURL);
        List<String> clients = loginPage.getClientNames();
        assertTrue(clients.containsAll(List.of(firstClient, secondClient)), clients.toString());
        assertFalse(clients.contains(disabledClient), clients.toString());
        assertTrue(loginPage.getIconSource(firstClient).startsWith("data:image/png;base64,"));

        // Picking a client sends the user to its provider.
        AuthorizationRequest request =
            loginPage.loginWith(secondClient, getAuthorizationEndpoint(setup, testReference, "Second"));
        assertEquals("Second-client", request.getParameter(CLIENT_ID_PARAMETER));
        assertTrue(request.getParameter("redirect_uri").endsWith("/oidc/authenticator/callback"),
            request.getParameter("redirect_uri"));

        // The top menu offers the same clients on every page.
        setup.forceGuestUser();
        setup.gotoPage(testReference);
        OIDCProLoginMenu menu = new OIDCProLoginMenu();
        assertTrue(menu.isDisplayed());
        clients = menu.open().getClientNames();
        assertTrue(clients.containsAll(List.of(firstClient, secondClient)), clients.toString());
        assertFalse(clients.contains(disabledClient), clients.toString());
        request = menu.loginWith(firstClient, getAuthorizationEndpoint(setup, testReference, "First"));
        assertEquals("First-client", request.getParameter(CLIENT_ID_PARAMETER));
    }

    /**
     * Create an OIDC client configuration whose authorization endpoint is a page of the wiki.
     *
     * @return the name of the configuration, which contains no character that needs to be encoded in a cookie
     */
    private String createClient(TestUtils setup, TestReference testReference, String suffix, boolean enabled)
    {
        String name = testReference.getLastSpaceReference().getName() + "-" + suffix;
        DocumentReference configurationReference =
            new DocumentReference("Client" + suffix, testReference.getLastSpaceReference());
        setup.deletePage(configurationReference);
        setup.addObject(configurationReference, CLIENT_CONFIGURATION_CLASS,
            "configurationName", name,
            "clientId", suffix + "-client",
            "clientSecret", "secret",
            "authorizationEndpoint", getAuthorizationEndpoint(setup, testReference, suffix),
            "skipped", enabled ? 0 : 1);
        return name;
    }

    private String getAuthorizationEndpoint(TestUtils setup, TestReference testReference, String suffix)
    {
        return setup.getURL(new DocumentReference("Provider" + suffix, testReference.getLastSpaceReference()),
            "view", null);
    }
}
