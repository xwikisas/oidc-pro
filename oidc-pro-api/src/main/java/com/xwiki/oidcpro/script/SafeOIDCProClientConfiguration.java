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

import com.xwiki.oidcpro.OIDCProClientConfiguration;

/**
 * The view of an {@link OIDCProClientConfiguration} that is safe to hand to scripts: it exposes what is needed to
 * offer the client to users (e.g. on the login page, to guests), but not the OIDC client configuration, which holds
 * the client secret and can be modified.
 *
 * @version $Id$
 */
public class SafeOIDCProClientConfiguration
{
    private final OIDCProClientConfiguration configuration;

    /**
     * @param configuration the configuration to protect
     */
    public SafeOIDCProClientConfiguration(OIDCProClientConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * @return the name of the OIDC client configuration, used to select it when authenticating
     */
    public String getConfigurationName()
    {
        return this.configuration.getConfigurationName();
    }

    /**
     * @return the JSON that specifies the layout and all the necessary metadata for displaying the configuration
     */
    public String getLayoutJson()
    {
        return this.configuration.getLayoutJson();
    }

    /**
     * @return the name of the template that is used to display the configuration
     */
    public String getTemplateName()
    {
        return this.configuration.getTemplateName();
    }

    /**
     * @return the name of the icon that is associated to this configuration
     */
    public String getIconName()
    {
        return this.configuration.getIconName();
    }

    /**
     * @return the file name of the icon, attached to the template page
     */
    public String getIconAttachment()
    {
        return this.configuration.getIconAttachment();
    }

    /**
     * @return the icon attached to the template page, inlined as a data URI, or empty if there is no icon
     */
    public String getIconURL()
    {
        return this.configuration.getIconURL();
    }

    /**
     * @return the custom code of the template that is used to display this configuration
     */
    public String getCustomCode()
    {
        return this.configuration.getCustomCode();
    }

    /**
     * @return the place where the custom code is inserted, relative to the standard layout: "header" or "footer"
     */
    public String getCodeInsertionPoint()
    {
        return this.configuration.getCodeInsertionPoint();
    }
}
