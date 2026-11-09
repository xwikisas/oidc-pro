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

import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;

import com.xpn.xwiki.objects.BaseObject;

/**
 * A read-only summary of an OIDC client configuration, displayed by the administration of the wikis that don't manage
 * their own configurations. It doesn't expose the client secret.
 *
 * @version $Id$
 * @since 1.1.0
 */
public class ReadOnlyOIDCClientConfiguration
{
    private final String configurationName;

    private final String clientId;

    private final String authorizationEndpoint;

    private final boolean enabled;

    private final boolean defaultClient;

    /**
     * @param configurationObject the OIDC client configuration object
     * @param defaultClient the name of the configuration used by default to authenticate
     */
    public ReadOnlyOIDCClientConfiguration(BaseObject configurationObject, String defaultClient)
    {
        this.configurationName =
            configurationObject.getStringValue(OIDCClientConfiguration.FIELD_CONFIGURATION_NAME);
        this.clientId = configurationObject.getStringValue(OIDCClientConfiguration.FIELD_CLIENT_ID);
        this.authorizationEndpoint =
            configurationObject.getStringValue(OIDCClientConfiguration.FIELD_ENDPOINT_AUTHORIZATION);
        this.enabled = configurationObject.getIntValue(OIDCClientConfiguration.FIELD_SKIPPED) != 1;
        this.defaultClient = this.configurationName.equals(defaultClient);
    }

    /**
     * @return the name of the configuration
     */
    public String getConfigurationName()
    {
        return this.configurationName;
    }

    /**
     * @return the identifier of the client at the provider
     */
    public String getClientId()
    {
        return this.clientId;
    }

    /**
     * @return the authorization endpoint of the provider
     */
    public String getAuthorizationEndpoint()
    {
        return this.authorizationEndpoint;
    }

    /**
     * @return {@code true} if users can log in with this configuration
     */
    public boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * @return {@code true} if this configuration is the one used by default to authenticate
     */
    public boolean isDefault()
    {
        return this.defaultClient;
    }
}
