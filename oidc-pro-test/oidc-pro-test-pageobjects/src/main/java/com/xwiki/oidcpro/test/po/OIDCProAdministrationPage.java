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

import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.livedata.test.po.LiveDataElement;
import org.xwiki.livedata.test.po.TableLayoutElement;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents the OIDC Pro section of the global administration.
 *
 * @version $Id$
 */
public class OIDCProAdministrationPage extends ViewPage
{
    private static final String NAME_COLUMN = "Configuration Name";

    private static final String ENABLED_COLUMN = "Enabled?";

    private static final String DEFAULT_COLUMN = "Default";

    private static final String TOGGLE_DONE_MESSAGE = "OIDC client successfully toggled";

    private static final By TOGGLE_INPUT = By.tagName("input");

    private static final By TOGGLE_SLIDER = By.className("oidcpro-slider");

    private final LiveDataElement configurations = new LiveDataElement("oidcpro-connections");

    /**
     * Opens the OIDC Pro administration section.
     *
     * @return the administration section
     */
    public static OIDCProAdministrationPage gotoPage()
    {
        getUtil().gotoPage("XWiki", "XWikiPreferences", "admin", "editor=globaladmin&section=OIDC%20Pro");
        return new OIDCProAdministrationPage();
    }

    /**
     * Click the Create button, which opens the form of a new configuration based on the Default template in a new
     * browser tab, and switch to that tab.
     *
     * @return the form of the new configuration
     */
    public OIDCProConfigurationEditPage createConfiguration()
    {
        Set<String> windows = getDriver().getWindowHandles();
        getDriver().findElement(By.cssSelector("button.oidcpro-config-create")).click();
        String newWindow = getDriver().waitUntilCondition(driver -> driver.getWindowHandles().stream()
            .filter(handle -> !windows.contains(handle)).findFirst().orElse(null));
        getDriver().switchTo().window(newWindow);
        return new OIDCProConfigurationEditPage();
    }

    /**
     * @param configurationName the name of an OIDC client configuration
     * @return {@code true} if the configuration is listed in the table
     */
    public boolean hasConfiguration(String configurationName)
    {
        return filter(configurationName).countRows() == 1;
    }

    /**
     * @param configurationName the name of an OIDC client configuration
     * @return {@code true} if the Enabled? switch of the configuration is on
     */
    public boolean isEnabled(String configurationName)
    {
        return getToggle(configurationName, ENABLED_COLUMN).findElement(TOGGLE_INPUT).isSelected();
    }

    /**
     * Flip the Enabled? switch of a configuration and wait for the change to be saved.
     *
     * @param configurationName the name of an OIDC client configuration
     */
    public void toggleEnabled(String configurationName)
    {
        getToggle(configurationName, ENABLED_COLUMN).findElement(TOGGLE_SLIDER).click();
        waitForNotificationSuccessMessage(TOGGLE_DONE_MESSAGE);
    }

    /**
     * @param configurationName the name of an OIDC client configuration
     * @return {@code true} if the configuration is the default one
     */
    public boolean isDefault(String configurationName)
    {
        return getToggle(configurationName, DEFAULT_COLUMN).findElement(TOGGLE_INPUT).isSelected();
    }

    /**
     * Make a configuration the default one and wait for the change to be saved.
     *
     * @param configurationName the name of an OIDC client configuration
     */
    public void setDefault(String configurationName)
    {
        getToggle(configurationName, DEFAULT_COLUMN).findElement(TOGGLE_SLIDER).click();
        waitForNotificationSuccessMessage(TOGGLE_DONE_MESSAGE);
    }

    private WebElement getToggle(String configurationName, String column)
    {
        return filter(configurationName).getCell(column, 1);
    }

    private TableLayoutElement filter(String configurationName)
    {
        TableLayoutElement table = this.configurations.getTableLayout();
        table.filterColumn(NAME_COLUMN, configurationName);
        return table;
    }
}
