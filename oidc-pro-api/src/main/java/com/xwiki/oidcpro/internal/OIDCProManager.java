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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.contrib.oidc.provider.internal.OIDCManager;
import org.xwiki.template.TemplateManager;

import com.nimbusds.oauth2.sdk.Response;

/**
 * Override of {@link OIDCManager} replacing the OIDC provider selection template with the OIDC Pro one.
 * <p>
 * The OIDC authenticator hardcodes the {@code oidc/client/provider.vm} template, which is also shipped in its own jar
 * and thus cannot be reliably overridden from the classloader.
 * <p>
 * Registering this component replaces the original {@link OIDCManager} instead of hiding it, so it has to be put back
 * when OIDC Pro is uninstalled. Components which already hold a reference to this instance (e.g. the authenticator)
 * keep using it after the uninstall, so it falls back on the original template once the OIDC Pro one is gone.
 *
 * @version $Id$
 */
@Component(roles = OIDCManager.class)
@Singleton
public class OIDCProManager extends OIDCManager implements Disposable
{
    /**
     * The template displayed by the OIDC authenticator to select the provider.
     */
    public static final String OIDC_PROVIDER_TEMPLATE = "oidc/client/provider.vm";

    /**
     * The OIDC Pro template used instead of {@link #OIDC_PROVIDER_TEMPLATE}.
     */
    public static final String OIDCPRO_PROVIDER_TEMPLATE = "oidcpro/provider.vm";

    @Inject
    private TemplateManager templateManager;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    @Override
    public Response executeTemplate(String templateName) throws Exception
    {
        return super.executeTemplate(
            OIDC_PROVIDER_TEMPLATE.equals(templateName) && this.templateManager.getTemplate(OIDCPRO_PROVIDER_TEMPLATE)
                != null ? OIDCPRO_PROVIDER_TEMPLATE : templateName);
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        // When the component manager is disposed, this component is still registered. When it's unregistered (i.e.
        // OIDC Pro is uninstalled from the namespace of the component manager which replaced the original), nothing
        // is left to fall back on.
        if (!this.componentManager.hasComponent(OIDCManager.class)) {
            try {
                for (ComponentDescriptor<?> descriptor : new ComponentAnnotationLoader()
                    .getComponentsDescriptors(OIDCManager.class)) {
                    this.componentManager.registerComponent(descriptor);
                }
            } catch (ComponentRepositoryException e) {
                this.logger.error("Failed to restore the original OIDC manager component.", e);
            }
        }
    }
}
