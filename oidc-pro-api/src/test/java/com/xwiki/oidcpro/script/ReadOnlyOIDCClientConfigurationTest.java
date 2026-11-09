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

import org.junit.jupiter.api.Test;

import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReadOnlyOIDCClientConfiguration}.
 *
 * @version $Id$
 */
class ReadOnlyOIDCClientConfigurationTest
{
    @Test
    void enabledDefaultConfiguration()
    {
        BaseObject configurationObject = mockConfigurationObject(0);

        ReadOnlyOIDCClientConfiguration configuration =
            new ReadOnlyOIDCClientConfiguration(configurationObject, "EntraID");

        assertEquals("EntraID", configuration.getConfigurationName());
        assertEquals("entra-client", configuration.getClientId());
        assertEquals("https://login.example.com/authorize", configuration.getAuthorizationEndpoint());
        assertTrue(configuration.isEnabled());
        assertTrue(configuration.isDefault());
    }

    @Test
    void disabledConfiguration()
    {
        ReadOnlyOIDCClientConfiguration configuration =
            new ReadOnlyOIDCClientConfiguration(mockConfigurationObject(1), "default");

        assertFalse(configuration.isEnabled());
        assertFalse(configuration.isDefault());
    }

    private BaseObject mockConfigurationObject(int skipped)
    {
        BaseObject configurationObject = mock(BaseObject.class);
        when(configurationObject.getStringValue("configurationName")).thenReturn("EntraID");
        when(configurationObject.getStringValue("clientId")).thenReturn("entra-client");
        when(configurationObject.getStringValue("authorizationEndpoint"))
            .thenReturn("https://login.example.com/authorize");
        when(configurationObject.getIntValue("skipped")).thenReturn(skipped);
        return configurationObject;
    }
}
