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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Clear the OIDC Pro client configuration cache when a document holding an OIDC client configuration, an OIDC Pro
 * template or a template binding is created, updated or deleted. Attaching a template icon also updates the template
 * document, so icon changes are covered as well.
 *
 * @version $Id$
 */
@Component
@Named(OIDCProClientConfigurationCacheInvalidator.NAME)
@Singleton
public class OIDCProClientConfigurationCacheInvalidator extends AbstractEventListener
{
    /**
     * The name of the listener.
     */
    public static final String NAME = "com.xwiki.oidcpro.internal.OIDCProClientConfigurationCacheInvalidator";

    private static final List<EntityReference> WATCHED_CLASSES = Arrays.asList(
        OIDCClientConfiguration.CLASS_REFERENCE,
        ConfigurationStore.PRO_TEMPLATE_CLASS,
        ConfigurationStore.PRO_TEMPLATE_BINDER_CLASS);

    @Inject
    private OIDCProClientConfigurationCache cache;

    /**
     * Default constructor.
     */
    public OIDCProClientConfigurationCacheInvalidator()
    {
        super(NAME, new DocumentCreatedEvent(), new DocumentUpdatedEvent(), new DocumentDeletedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument document = (XWikiDocument) source;
        // The original document holds the objects that were removed by an update or a delete.
        if (hasWatchedObject(document) || hasWatchedObject(document.getOriginalDocument())) {
            // A template can be shared by several configurations, so clearing everything is the simplest correct
            // option. The cache is small and rebuilt on the next use.
            this.cache.removeAll();
        }
    }

    private boolean hasWatchedObject(XWikiDocument document)
    {
        if (document == null) {
            return false;
        }
        for (EntityReference classReference : WATCHED_CLASSES) {
            if (document.getXObject(classReference) != null) {
                return true;
            }
        }
        return false;
    }
}
