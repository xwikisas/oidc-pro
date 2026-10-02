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
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xwiki.oidcpro.OIDCProClientConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OIDCProClientConfigurationCache}.
 *
 * @version $Id$
 */
@ComponentTest
class OIDCProClientConfigurationCacheTest
{
    @InjectMockComponents
    private OIDCProClientConfigurationCache configurationCache;

    @MockComponent
    private CacheManager cacheManager;

    private Cache<OIDCProClientConfiguration> cache;

    @BeforeComponent
    @SuppressWarnings("unchecked")
    void beforeComponent() throws Exception
    {
        this.cache = mock(Cache.class);
        when(this.cacheManager.<OIDCProClientConfiguration>createNewCache(any(CacheConfiguration.class)))
            .thenReturn(this.cache);
    }

    @Test
    void getSetAndRemoveAll()
    {
        OIDCProClientConfiguration configuration = mock(OIDCProClientConfiguration.class);
        when(this.cache.get("client")).thenReturn(configuration);

        this.configurationCache.set("client", configuration);
        assertSame(configuration, this.configurationCache.get("client"));
        this.configurationCache.removeAll();

        verify(this.cache).set("client", configuration);
        verify(this.cache).removeAll();
    }

    @Test
    void dispose() throws Exception
    {
        this.configurationCache.dispose();

        verify(this.cache).dispose();
    }

    @Test
    void initializeWhenCacheCannotBeCreated() throws Exception
    {
        CacheException cause = new CacheException("error");
        when(this.cacheManager.createNewCache(any(CacheConfiguration.class))).thenThrow(cause);

        InitializationException exception =
            assertThrows(InitializationException.class, () -> this.configurationCache.initialize());

        assertEquals("Failed to create cache with id [oidcpro.client.configuration]", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
