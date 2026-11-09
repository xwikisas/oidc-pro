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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents the search of the properties of an OIDC client configuration, available both in view and edit mode.
 *
 * @version $Id$
 */
public class OIDCProPropertySearch extends BaseElement
{
    /**
     * Search the properties by name or label. The sections holding matches are expanded, and the other properties are
     * hidden. An empty query displays the properties as they were before the search.
     *
     * @param query the text to search for
     * @return this search
     */
    public OIDCProPropertySearch search(String query)
    {
        WebElement searchInput = getDriver().findElement(By.id("oidcpro-property-search"));
        // Clearing the input with WebElement#clear doesn't fire the input event the search listens to.
        searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        searchInput.sendKeys(query);
        return this;
    }

    /**
     * @return the names of the properties the user can see, i.e. not hidden by the search nor by a collapsed section
     */
    public List<String> getDisplayedProperties()
    {
        return getDriver().findElements(By.className("oidcpro-property")).stream().filter(WebElement::isDisplayed)
            .map(property -> property.getAttribute("data-oidcpro-property-name")).collect(Collectors.toList());
    }

    /**
     * @return {@code true} if the user is told that no property matches the search
     */
    public boolean isNoPropertyFoundMessageDisplayed()
    {
        return getDriver().findElement(By.className("oidcpro-search-noresults")).isDisplayed();
    }
}
