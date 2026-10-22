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
package com.xwiki.oidcpro.internal;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.contrib.oidc.provider.internal.OIDCManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OIDCProManager}.
 *
 * @version $Id$
 */
@ComponentTest
class OIDCProManagerTest
{
    @InjectMockComponents
    private OIDCProManager manager;

    @MockComponent
    private TemplateManager templates;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @Test
    void executeTemplateReplacesProviderTemplate() throws Exception
    {
        when(this.templates.getTemplate("oidcpro/provider.vm")).thenReturn(mock(Template.class));
        when(this.templates.render("oidcpro/provider.vm")).thenReturn("OIDC Pro provider");

        assertEquals("OIDC Pro provider",
            this.manager.executeTemplate("oidc/client/provider.vm").toHTTPResponse().getBody());
    }

    @Test
    void executeTemplateKeepsOtherTemplates() throws Exception
    {
        when(this.templates.render("oidc/client/logout.vm")).thenReturn("logout");

        assertEquals("logout", this.manager.executeTemplate("oidc/client/logout.vm").toHTTPResponse().getBody());
    }

    @Test
    void executeTemplateFallsBackWhenProviderTemplateIsMissing() throws Exception
    {
        when(this.templates.render("oidc/client/provider.vm")).thenReturn("OIDC provider");

        assertEquals("OIDC provider",
            this.manager.executeTemplate("oidc/client/provider.vm").toHTTPResponse().getBody());
    }

    @Test
    void disposeRestoresOriginalManagerWhenUnregistered() throws Exception
    {
        this.componentManager.unregisterComponent(OIDCManager.class, "default");

        this.manager.dispose();

        assertEquals(OIDCManager.class,
            this.componentManager.getComponentDescriptor(OIDCManager.class, "default").getImplementation());
    }

    @Test
    void disposeKeepsRegisteredManager() throws Exception
    {
        this.componentManager.registerMockComponent(OIDCManager.class);
        ComponentDescriptor<OIDCManager> descriptor =
            this.componentManager.getComponentDescriptor(OIDCManager.class, "default");

        this.manager.dispose();

        assertSame(descriptor, this.componentManager.getComponentDescriptor(OIDCManager.class, "default"));
    }

    @Test
    void disposeWhenOriginalManagerCannotBeRestored() throws Exception
    {
        ComponentManager failingComponentManager = mock(ComponentManager.class);
        doThrow(new ComponentRepositoryException("error")).when(failingComponentManager)
            .registerComponent(any(ComponentDescriptor.class));
        ReflectionUtils.setFieldValue(this.manager, "componentManager", failingComponentManager);
        Logger logger = mock(Logger.class);
        ReflectionUtils.setFieldValue(this.manager, "logger", logger);

        this.manager.dispose();

        verify(logger).error(eq("Failed to restore the original OIDC manager component."),
            any(ComponentRepositoryException.class));
    }
}
