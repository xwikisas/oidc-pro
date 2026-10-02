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
package com.xwiki.oidcpro.test.po;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BasePage;

/**
 * Represents the provider selection page, displayed by the OIDC authenticator when the user has to log in and no
 * client configuration can be used by default.
 *
 * @version $Id$
 */
public class OIDCProLoginPage extends BasePage
{
    private static final By CLIENT_BUTTONS = By.cssSelector(".auth-methods .auth-method[data-oidcpro-client]");

    /**
     * Wait for the list of clients to be displayed.
     */
    public OIDCProLoginPage()
    {
        getDriver().waitUntilElementIsVisible(By.className("auth-methods"));
    }

    /**
     * Opens the login page, which displays the provider selection page when no client can be used by default.
     *
     * @param redirectURL the URL to go back to once logged in
     * @return the provider selection page
     */
    public static OIDCProLoginPage gotoPage(String redirectURL)
    {
        getUtil().gotoPage("XWiki", "XWikiLogin", "login", "xredirect=" + getUtil().escapeURL(redirectURL));
        return new OIDCProLoginPage();
    }

    /**
     * @return the names of the clients the user can log in with, in display order
     */
    public List<String> getClientNames()
    {
        return getDriver().findElements(CLIENT_BUTTONS).stream()
            .map(button -> button.findElement(By.className("auth-method-label")).getText())
            .collect(Collectors.toList());
    }

    /**
     * @param clientName the name of a client
     * @return the {@code src} of the image icon of the client, or {@code null} if it has none
     */
    public String getIconSource(String clientName)
    {
        WebElement button = getClientButton(clientName);
        By image = By.cssSelector(".auth-method-icon img");
        return getDriver().hasElementWithoutWaiting(button, image)
            ? button.findElement(image).getAttribute("src") : null;
    }

    /**
     * Click the button of a client, which sends the browser to the authorization endpoint of its provider.
     *
     * @param clientName the name of the client
     * @param authorizationEndpoint the authorization endpoint configured for the client
     * @return the authorization request received by the provider
     */
    public AuthorizationRequest loginWith(String clientName, String authorizationEndpoint)
    {
        getClientButton(clientName).click();
        return new AuthorizationRequest(authorizationEndpoint);
    }

    private WebElement getClientButton(String clientName)
    {
        return getDriver().findElements(CLIENT_BUTTONS).stream()
            .filter(button -> clientName.equals(button.getAttribute("data-oidcpro-client"))).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No login button for client [" + clientName + "]"));
    }
}
