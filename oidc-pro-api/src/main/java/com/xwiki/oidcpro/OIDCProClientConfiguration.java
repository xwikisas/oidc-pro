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
 * Provides the configuration of an OIDC client together with the template that is used to display it.
 * <p>
 * The client configuration holds sensitive data, such as the client secret, so this object must not be handed to
 * scripts: use {@link com.xwiki.oidcpro.script.SafeOIDCProClientConfiguration} instead.
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
     * Name of the property that contains the file name of the icon, attached to the template page.
     */
    public static final String PROPERTY_ICON_ATTACHMENT = "iconAttachment";

    /**
     * Name of the property that contains the custom code.
     */
    public static final String PROPERTY_CUSTOM_CODE = "customCode";

    /**
     * Name of the property that contains the code insertion point.
     */
    public static final String PROPERTY_CODE_INSERTION_POINT = "codeInsertionPoint";

    private final OIDCClientConfiguration clientConfiguration;

    private final String layoutJson;

    private final String templateName;

    private final String iconName;

    private final String iconAttachment;

    private final String iconURL;

    private final String customCode;

    private final String codeInsertionPoint;

    /**
     * The template values are copied, so that this object doesn't keep a reference to the shared template object.
     *
     * @param clientConfiguration the OIDC client configuration
     * @param templateObject the template object that is associated to the client configuration, or {@code null} when
     *     no trusted template applies to it (e.g. for configurations created by other applications)
     * @param iconURL the icon attached to the template page, as a data URI
     */
    public OIDCProClientConfiguration(OIDCClientConfiguration clientConfiguration, BaseObject templateObject,
        String iconURL)
    {
        this.clientConfiguration = clientConfiguration;
        this.layoutJson = getLargeStringValue(templateObject, PROPERTY_LAYOUT_JSON);
        this.templateName = getStringValue(templateObject, PROPERTY_NAME);
        this.iconName = getStringValue(templateObject, PROPERTY_ICON_NAME);
        this.iconAttachment = getStringValue(templateObject, PROPERTY_ICON_ATTACHMENT);
        this.iconURL = iconURL;
        this.customCode = getLargeStringValue(templateObject, PROPERTY_CUSTOM_CODE);
        this.codeInsertionPoint = getStringValue(templateObject, PROPERTY_CODE_INSERTION_POINT);
    }

    /**
     * @return the OIDC client configuration, which holds sensitive data such as the client secret
     */
    public OIDCClientConfiguration getClientConfiguration()
    {
        return this.clientConfiguration;
    }

    /**
     * @return the name of the OIDC client configuration, used to select it when authenticating
     */
    public String getConfigurationName()
    {
        return this.clientConfiguration.getConfigurationName();
    }

    /**
     * @return the JSON that specifies the layout and all the necessary metadata for displaying the configuration.
     */
    public String getLayoutJson()
    {
        return this.layoutJson;
    }

    /**
     * @return the name of the template that is used to display the configuration
     */
    public String getTemplateName()
    {
        return this.templateName;
    }

    /**
     * @return the name of the icon that is associated to this configuration.
     */
    public String getIconName()
    {
        return this.iconName;
    }

    /**
     * @return the file name of the icon, attached to the template page.
     */
    public String getIconAttachment()
    {
        return this.iconAttachment;
    }

    /**
     * @return the icon attached to the template page, inlined as a data URI so that it can be displayed to users that
     *     can't view the template page (e.g. guests on the login page). Empty if there is no icon.
     */
    public String getIconURL()
    {
        return this.iconURL;
    }

    /**
     * @return the custom code that shall be executed when displaying this configuration. Can achieve different purposes
     *     like displaying additional html, injecting javascript or styles.
     */
    public String getCustomCode()
    {
        return this.customCode;
    }

    /**
     * @return the place where the custom code will be inserted, relative to the standard layout. Can be "header" or
     *     "footer".
     */
    public String getCodeInsertionPoint()
    {
        return this.codeInsertionPoint;
    }

    private static String getStringValue(BaseObject templateObject, String property)
    {
        return templateObject != null ? templateObject.getStringValue(property) : null;
    }

    private static String getLargeStringValue(BaseObject templateObject, String property)
    {
        return templateObject != null ? templateObject.getLargeStringValue(property) : null;
    }
}
