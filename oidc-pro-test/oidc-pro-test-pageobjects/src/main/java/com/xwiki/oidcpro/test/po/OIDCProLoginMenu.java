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
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents the "Login with" menu that OIDC Pro adds to the top bar for guest users.
 *
 * @version $Id$
 */
public class OIDCProLoginMenu extends BaseElement
{
    private static final By CLIENT_LINKS = By.cssSelector(".oidcpro-login-option a[data-oidcpro-client]");

    /**
     * @return {@code true} if the menu is displayed in the top bar of the current page
     */
    public boolean isDisplayed()
    {
        return getDriver().hasElementWithoutWaiting(By.id("oidcpro-login-menu-toggle"));
    }

    /**
     * Open the menu.
     *
     * @return this menu
     */
    public OIDCProLoginMenu open()
    {
        getDriver().findElement(By.id("oidcpro-login-menu-toggle")).click();
        getDriver().waitUntilElementIsVisible(By.cssSelector(".oidcpro-login-menu .dropdown-menu"));
        return this;
    }

    /**
     * @return the names of the clients listed in the menu, in display order
     */
    public List<String> getClientNames()
    {
        return getDriver().findElements(CLIENT_LINKS).stream()
            .map(link -> link.findElement(By.className("oidcpro-login-label")).getText())
            .collect(Collectors.toList());
    }

    /**
     * Click the entry of a client, which sends the browser to the authorization endpoint of its provider.
     *
     * @param clientName the name of the client
     * @param authorizationEndpoint the authorization endpoint configured for the client
     * @return the authorization request received by the provider
     */
    public AuthorizationRequest loginWith(String clientName, String authorizationEndpoint)
    {
        WebElement link = getDriver().findElements(CLIENT_LINKS).stream()
            .filter(element -> clientName.equals(element.getAttribute("data-oidcpro-client"))).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No menu entry for client [" + clientName + "]"));
        link.click();
        return new AuthorizationRequest(authorizationEndpoint);
    }
}
