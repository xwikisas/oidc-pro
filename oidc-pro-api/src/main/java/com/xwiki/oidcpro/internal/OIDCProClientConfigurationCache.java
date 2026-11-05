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

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xwiki.oidcpro.OIDCProClientConfiguration;

/**
 * Cache of the OIDC Pro client configurations of each wiki, indexed by configuration name.
 * <p>
 * The configurations (and the templates they use) are resolved for the current wiki, so the cache is scoped per wiki:
 * a configuration resolved on one wiki must never be reused by another wiki having a configuration with the same name.
 *
 * @version $Id$
 */
@Component(roles = OIDCProClientConfigurationCache.class)
@Singleton
public class OIDCProClientConfigurationCache implements Initializable, Disposable
{
    private static final String CACHE_ID = "oidcpro.client.configuration";

    @Inject
    private CacheManager cacheManager;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    private Cache<OIDCProClientConfiguration> cache;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.cache = this.cacheManager.createNewCache(new LRUCacheConfiguration(CACHE_ID, 10));
        } catch (CacheException e) {
            throw new InitializationException(String.format("Failed to create cache with id [%s]", CACHE_ID), e);
        }
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        if (this.cache != null) {
            this.cache.dispose();
        }
    }

    /**
     * @param configurationName the name of the OIDC client configuration
     * @return the configuration cached for the current wiki, or {@code null} if it is not cached
     */
    public OIDCProClientConfiguration get(String configurationName)
    {
        return this.cache.get(getKey(configurationName));
    }

    /**
     * @param configurationName the name of the OIDC client configuration
     * @param configuration the configuration resolved for the current wiki
     */
    public void set(String configurationName, OIDCProClientConfiguration configuration)
    {
        this.cache.set(getKey(configurationName), configuration);
    }

    /**
     * Remove all the cached configurations, of all the wikis.
     */
    public void removeAll()
    {
        this.cache.removeAll();
    }

    private String getKey(String configurationName)
    {
        // A wiki identifier can't contain a colon, so the key is unambiguous whatever the configuration name.
        return this.wikiDescriptorManager.getCurrentWikiId() + ':' + configurationName;
    }
}
