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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.oidc.auth.internal.store.OIDCClientConfigurationCache;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.oidcpro.OIDCProClientConfiguration;

/**
 * Retrieve oidc pro configurations.
 *
 * @version $Id$
 */
@Component(roles = ConfigurationStore.class)
@Singleton
public class ConfigurationStore implements Initializable
{
    private static final String TEMPLATE_NAME = "templateName";

    private static final List<String> OIDC_PRO_CODE_SPACE = Arrays.asList("OIDCPro", "Code");

    private static final LocalDocumentReference PRO_TEMPLATE_BINDER_CLASS =
        new LocalDocumentReference(OIDC_PRO_CODE_SPACE, "OIDCProTemplateBinderClass");

    private static final LocalDocumentReference PRO_TEMPLATE_CLASS =
        new LocalDocumentReference(OIDC_PRO_CODE_SPACE, "OIDCProTemplateClass");

    @Inject
    private QueryManager queryManager;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private OIDCClientConfigurationCache configurationCache;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Logger logger;

    private Cache<OIDCProClientConfiguration> proClientConfigurationCache;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.proClientConfigurationCache =
                this.cacheManager.createNewCache(new LRUCacheConfiguration("oidcpro.client.configuration", 10));
        } catch (CacheException e) {
            throw new InitializationException("Failed to create cache with if [oidc.client.configuration]");
        }
    }

    /**
     * @return the OIDC configurations of the current wiki.
     */
    public List<OIDCProClientConfiguration> getConfigurations()
    {
        List<OIDCProClientConfiguration> configurations = new ArrayList<>();
        XWikiContext context = contextProvider.get();
        try {
            List<Object[]> result = queryManager.createQuery(
                "select distinct doc.fullName, oidc.configurationName from Document doc, "
                    + "doc.object('XWiki.OIDC.ClientConfigurationClass') as oidc where oidc.skipped <> '1'",
                Query.XWQL).execute();

            for (Object[] entry : result) {
                String serializedDocument = entry[0].toString();
                String cfgName = entry[1].toString();
                OIDCProClientConfiguration proClientConfiguration = proClientConfigurationCache.get(cfgName);
                if (proClientConfiguration == null) {
                    proClientConfiguration = getClientConfiguration(cfgName, serializedDocument, context);
                }
                configurations.add(proClientConfiguration);
            }
        } catch (QueryException | XWikiException e) {
            logger.error("Failed to retrieve the oidc configurations.", e);
            return Collections.emptyList();
        }
        return configurations;
    }

    private OIDCProClientConfiguration getClientConfiguration(String cfgName, String serializedDocument,
        XWikiContext context) throws XWikiException, QueryException
    {
        OIDCClientConfigurationCache.CacheEntry cacheEntry =
            this.configurationCache.get(cfgName);
        OIDCClientConfiguration clientConfiguration = null;
        BaseObject templateObj = null;
        if (cacheEntry == null) {
            XWikiDocument document =
                contextProvider.get().getWiki()
                    .getDocument(documentReferenceResolver.resolve(serializedDocument), context);
            clientConfiguration =
                new OIDCClientConfiguration(document.getXObject(OIDCClientConfiguration.CLASS_REFERENCE));

            BaseObject templateBinderObj = document.getXObject(PRO_TEMPLATE_BINDER_CLASS);
            String templateId = templateBinderObj.getStringValue(TEMPLATE_NAME);
            templateId = templateId == null || templateId.isEmpty() ? "default" : templateId;

            List<Object> result = queryManager.createQuery("from doc.object('OIDCPro.Code.OIDCProTemplateClass') as "
                + "template where template"
                + ".name = :templateName", Query.XWQL).bindValue(TEMPLATE_NAME, templateId).execute();
            String templateDocRef = result.get(0).toString();

            XWikiDocument templateDoc =
                context.getWiki().getDocument(documentReferenceResolver.resolve(templateDocRef), context);
            templateObj = templateDoc.getXObject(PRO_TEMPLATE_CLASS);

            configurationCache.set(cfgName, clientConfiguration);
        } else {
            clientConfiguration = cacheEntry.getConfiguration();
        }
        return new OIDCProClientConfiguration(clientConfiguration, templateObj);
    }
}
