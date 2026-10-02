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

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OIDCProClientConfigurationCacheInvalidator}.
 *
 * @version $Id$
 */
@ComponentTest
class OIDCProClientConfigurationCacheInvalidatorTest
{
    @InjectMockComponents
    private OIDCProClientConfigurationCacheInvalidator listener;

    @MockComponent
    private OIDCProClientConfigurationCache cache;

    private XWikiDocument document;

    private XWikiDocument originalDocument;

    static Stream<LocalDocumentReference> watchedClasses()
    {
        return Stream.of(OIDCClientConfiguration.CLASS_REFERENCE, ConfigurationStore.PRO_TEMPLATE_CLASS,
            ConfigurationStore.PRO_TEMPLATE_BINDER_CLASS);
    }

    @BeforeEach
    void setUp()
    {
        this.document = mock(XWikiDocument.class);
        this.originalDocument = mock(XWikiDocument.class);
        when(this.document.getOriginalDocument()).thenReturn(this.originalDocument);
    }

    @ParameterizedTest
    @MethodSource("watchedClasses")
    void onEventWhenDocumentHasWatchedObject(LocalDocumentReference classReference)
    {
        when(this.document.getXObject(classReference)).thenReturn(mock(BaseObject.class));

        this.listener.onEvent(new DocumentUpdatedEvent(), this.document, null);

        verify(this.cache).removeAll();
    }

    @Test
    void onEventWhenWatchedObjectWasRemoved()
    {
        when(this.originalDocument.getXObject(ConfigurationStore.PRO_TEMPLATE_CLASS))
            .thenReturn(mock(BaseObject.class));

        this.listener.onEvent(new DocumentDeletedEvent(), this.document, null);

        verify(this.cache).removeAll();
    }

    @Test
    void onEventWhenDocumentIsUnrelated()
    {
        this.listener.onEvent(new DocumentUpdatedEvent(), this.document, null);

        verify(this.cache, never()).removeAll();
    }

    @Test
    void onEventWithoutOriginalDocument()
    {
        when(this.document.getOriginalDocument()).thenReturn(null);

        this.listener.onEvent(new DocumentUpdatedEvent(), this.document, null);

        verify(this.cache, never()).removeAll();
    }
}
