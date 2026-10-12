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

import com.xwiki.oidcpro.OIDCProClientConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SafeOIDCProClientConfiguration}.
 *
 * @version $Id$
 */
class SafeOIDCProClientConfigurationTest
{
    @Test
    void exposesDisplayValues()
    {
        OIDCProClientConfiguration configuration = mock(OIDCProClientConfiguration.class);
        when(configuration.getConfigurationName()).thenReturn("EntraID");
        when(configuration.getLayoutJson()).thenReturn("{}");
        when(configuration.getTemplateName()).thenReturn("entra");
        when(configuration.getIconName()).thenReturn("key");
        when(configuration.getIconAttachment()).thenReturn("a.png");
        when(configuration.getIconURL()).thenReturn("data:");
        when(configuration.getCustomCode()).thenReturn("code");
        when(configuration.getCodeInsertionPoint()).thenReturn("footer");

        SafeOIDCProClientConfiguration safeConfiguration = new SafeOIDCProClientConfiguration(configuration);

        assertEquals("EntraID", safeConfiguration.getConfigurationName());
        assertEquals("{}", safeConfiguration.getLayoutJson());
        assertEquals("entra", safeConfiguration.getTemplateName());
        assertEquals("key", safeConfiguration.getIconName());
        assertEquals("a.png", safeConfiguration.getIconAttachment());
        assertEquals("data:", safeConfiguration.getIconURL());
        assertEquals("code", safeConfiguration.getCustomCode());
        assertEquals("footer", safeConfiguration.getCodeInsertionPoint());
    }
}
