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

package com.xwiki.oidcpro;

import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Provides the configuration on an OIDC client together with the configuration of the template that is used to display
 * it.
 *
 * @version $Id$
 */
public class OIDCProClientConfiguration
{
    /**
     * Name of the property that contains the layout json.
     */
    public static final String PROPERTY_LAYOUT_JSON = "layoutJson";

    /**
     * Name of the property that contains the name of the template.
     */
    public static final String PROPERTY_NAME = "name";

    /**
     * Name of the property that contains the icon name.
     */
    public static final String PROPERTY_ICON_NAME = "iconName";

    /**
     * Name of the property that contains the icon reference..
     */
    public static final String PROPERTY_ICON_REFERENCE = "iconReference";

    /**
     * Name of the property that contains the custom code.
     */
    public static final String PROPERTY_CUSTOM_CODE = "customCode";

    /**
     * Name of the property that contains the code insertion point.
     */
    public static final String PROPERTY_CODE_INSERTION_POINT = "codeInsertionPoint";

    private final BaseObject templateObject;

    private final OIDCClientConfiguration clientConfiguration;

    /**
     * @param configuration the OIDCClientConfiguration that gets wrapped by this class.
     * @param templateObject the template object that is associated to the client configuration.
     */
    public OIDCProClientConfiguration(OIDCClientConfiguration configuration, BaseObject templateObject)
    {
        this.clientConfiguration = configuration;
        this.templateObject = templateObject;
    }

    /**
     * @return the OIDC client configuration where the information is stored and retrieved from.
     */
    public OIDCClientConfiguration getClientConfiguration()
    {
        return clientConfiguration;
    }

    /**
     * @return the JSON that specifies the layout and all the necessary metadata for displaying the configuration.
     */
    public String getLayoutJson()
    {
        return this.templateObject.getLargeStringValue(PROPERTY_LAYOUT_JSON);
    }

    /**
     * @return the name of the
     */
    public String getTemplateName()
    {
        return this.templateObject.getStringValue(PROPERTY_NAME);
    }

    /**
     * @return the name of the icon that is associated to this configuration.
     */
    public String getIconName()
    {
        return this.templateObject.getStringValue(PROPERTY_ICON_NAME);
    }

    /**
     * @return the reference of the icon that is associated to this configuration. It can be used for displaying in
     *     various places.
     */
    public String getIconReference()
    {
        return this.templateObject.getStringValue(PROPERTY_ICON_REFERENCE);
    }

    /**
     * @return the custom code that shall be executed when displaying this configuration. Can achieve different purposes
     *     like displaying additional html, injecting javascript or styles.
     */
    public String getCustomCode()
    {
        return this.templateObject.getLargeStringValue(PROPERTY_CUSTOM_CODE);
    }

    /**
     * @return the place where the custom code will be inserted, relative to the standard layout. Can be "header" or
     *     "footer".
     */
    public String getCodeInsertionPoint()
    {
        return this.templateObject.getStringValue(PROPERTY_CODE_INSERTION_POINT);
    }
}
