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

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.oidc.provider.internal.OIDCManager;

import com.nimbusds.oauth2.sdk.Response;

/**
 * Override of {@link OIDCManager} replacing the OIDC provider selection template with the OIDC Pro one.
 * <p>
 * The OIDC authenticator hardcodes the {@code oidc/client/provider.vm} template, which is also shipped in its own jar
 * and thus cannot be reliably overridden from the classloader.
 *
 * @version $Id$
 */
@Component(roles = OIDCManager.class)
@Singleton
public class OIDCProManager extends OIDCManager
{
    /**
     * The template displayed by the OIDC authenticator to select the provider.
     */
    public static final String OIDC_PROVIDER_TEMPLATE = "oidc/client/provider.vm";

    /**
     * The OIDC Pro template used instead of {@link #OIDC_PROVIDER_TEMPLATE}.
     */
    public static final String OIDCPRO_PROVIDER_TEMPLATE = "oidcpro/provider.vm";

    @Override
    public Response executeTemplate(String templateName) throws Exception
    {
        return super.executeTemplate(
            OIDC_PROVIDER_TEMPLATE.equals(templateName) ? OIDCPRO_PROVIDER_TEMPLATE : templateName);
    }
}
