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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.xwiki.test.ui.po.editor.EditPage;

/**
 * Represents the form used to create or edit an OIDC client configuration.
 *
 * @version $Id$
 */
public class OIDCProConfigurationEditPage extends EditPage
{
    private static final String FIELD_PREFIX = "XWiki.OIDC.ClientConfigurationClass_0_";

    /**
     * Wait for the form to be displayed.
     */
    public OIDCProConfigurationEditPage()
    {
        getDriver().waitUntilElementIsVisible(By.id(FIELD_PREFIX + "configurationName"));
    }

    /**
     * @param name the name of the configuration
     * @return this form
     */
    public OIDCProConfigurationEditPage setConfigurationName(String name)
    {
        return setField("configurationName", name);
    }

    /**
     * @param clientId the client ID registered at the provider
     * @return this form
     */
    public OIDCProConfigurationEditPage setClientId(String clientId)
    {
        return setField("clientId", clientId);
    }

    /**
     * @param clientSecret the client secret registered at the provider
     * @return this form
     */
    public OIDCProConfigurationEditPage setClientSecret(String clientSecret)
    {
        return setField("clientSecret", clientSecret);
    }

    /**
     * @param endpoint the authorization endpoint of the provider
     * @return this form
     */
    public OIDCProConfigurationEditPage setAuthorizationEndpoint(String endpoint)
    {
        return setField("authorizationEndpoint", endpoint);
    }

    /**
     * @param enabled whether users can log in with this configuration (the opposite of "Is skipped?")
     * @return this form
     */
    public OIDCProConfigurationEditPage setEnabled(boolean enabled)
    {
        new Select(getDriver().findElement(By.id(FIELD_PREFIX + "skipped"))).selectByValue(enabled ? "0" : "1");
        return this;
    }

    private OIDCProConfigurationEditPage setField(String property, String value)
    {
        WebElement field = getDriver().findElement(By.id(FIELD_PREFIX + property));
        field.clear();
        field.sendKeys(value);
        return this;
    }
}
