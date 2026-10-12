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

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;

import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OIDCProClientConfiguration}.
 *
 * @version $Id$
 */
class OIDCProClientConfigurationTest
{
    @Test
    void templateValues()
    {
        BaseObject templateObject = mock(BaseObject.class);
        when(templateObject.getLargeStringValue(OIDCProClientConfiguration.PROPERTY_LAYOUT_JSON)).thenReturn("{}");
        when(templateObject.getStringValue(OIDCProClientConfiguration.PROPERTY_NAME)).thenReturn("entra");
        when(templateObject.getStringValue(OIDCProClientConfiguration.PROPERTY_ICON_NAME)).thenReturn("key");
        when(templateObject.getStringValue(OIDCProClientConfiguration.PROPERTY_ICON_ATTACHMENT)).thenReturn("a.png");
        when(templateObject.getLargeStringValue(OIDCProClientConfiguration.PROPERTY_CUSTOM_CODE)).thenReturn("code");
        when(templateObject.getStringValue(OIDCProClientConfiguration.PROPERTY_CODE_INSERTION_POINT))
            .thenReturn("footer");
        OIDCClientConfiguration clientConfiguration = mock(OIDCClientConfiguration.class);
        when(clientConfiguration.getConfigurationName()).thenReturn("EntraID");

        OIDCProClientConfiguration configuration =
            new OIDCProClientConfiguration(clientConfiguration, templateObject, "data:");

        assertSame(clientConfiguration, configuration.getClientConfiguration());
        assertEquals("EntraID", configuration.getConfigurationName());
        assertEquals("{}", configuration.getLayoutJson());
        assertEquals("entra", configuration.getTemplateName());
        assertEquals("key", configuration.getIconName());
        assertEquals("a.png", configuration.getIconAttachment());
        assertEquals("data:", configuration.getIconURL());
        assertEquals("code", configuration.getCustomCode());
        assertEquals("footer", configuration.getCodeInsertionPoint());
    }

    @Test
    void withoutTemplate()
    {
        OIDCProClientConfiguration configuration =
            new OIDCProClientConfiguration(mock(OIDCClientConfiguration.class), null, "");

        assertNull(configuration.getLayoutJson());
        assertNull(configuration.getTemplateName());
        assertNull(configuration.getIconName());
        assertNull(configuration.getIconAttachment());
        assertNull(configuration.getCustomCode());
        assertNull(configuration.getCodeInsertionPoint());
        assertEquals("", configuration.getIconURL());
    }
}
