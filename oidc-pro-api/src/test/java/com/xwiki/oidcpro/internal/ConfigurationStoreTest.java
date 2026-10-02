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

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.contrib.oidc.auth.internal.store.OIDCClientConfigurationCache;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.oidcpro.OIDCProClientConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigurationStore}.
 *
 * @version $Id$
 */
@ComponentTest
class ConfigurationStoreTest
{
    private static final String CONFIGURATION_NAME = "EntraID";

    private static final String CONFIGURATION_PAGE = "OIDCPro.Configurations.EntraID";

    private static final String TEMPLATE_PAGE = "OIDCPro.Code.Templates.EntraTemplate";

    private static final String TEMPLATE_NAME = "templateName";

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private ConfigurationStore store;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private OIDCClientConfigurationCache configurationCache;

    @MockComponent
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @MockComponent
    private OIDCProClientConfigurationCache proClientConfigurationCache;

    @MockComponent
    private TemplateIconResolver templateIconResolver;

    private XWikiContext context;

    private Query configurationsQuery;

    private Query templateQuery;

    private XWikiDocument configurationDocument;

    private BaseObject clientObject;

    private BaseObject templateObject;

    @BeforeEach
    void setUp() throws Exception
    {
        this.context = mock(XWikiContext.class);
        XWiki wiki = mock(XWiki.class);
        when(this.context.getWiki()).thenReturn(wiki);
        when(this.contextProvider.get()).thenReturn(this.context);

        this.configurationsQuery = mock(Query.class);
        when(this.queryManager.createQuery(startsWith("select distinct doc.fullName"), eq(Query.XWQL)))
            .thenReturn(this.configurationsQuery);
        when(this.configurationsQuery.execute())
            .thenReturn(Collections.singletonList(new Object[] { CONFIGURATION_PAGE, CONFIGURATION_NAME }));

        this.templateQuery = mock(Query.class);
        when(this.queryManager.createQuery(startsWith("from doc.object('OIDCPro.Code.OIDCProTemplateClass')"),
            eq(Query.XWQL))).thenReturn(this.templateQuery);
        when(this.templateQuery.bindValue(eq(TEMPLATE_NAME), any())).thenReturn(this.templateQuery);
        when(this.templateQuery.execute()).thenReturn(List.of(TEMPLATE_PAGE));

        DocumentReference configurationReference =
            new DocumentReference("xwiki", List.of("OIDCPro", "Configurations"), CONFIGURATION_NAME);
        when(this.documentReferenceResolver.resolve(CONFIGURATION_PAGE)).thenReturn(configurationReference);
        this.configurationDocument = mock(XWikiDocument.class);
        when(wiki.getDocument(configurationReference, this.context)).thenReturn(this.configurationDocument);
        this.clientObject = mock(BaseObject.class);
        when(this.configurationDocument.getXObject(OIDCClientConfiguration.CLASS_REFERENCE))
            .thenReturn(this.clientObject);

        DocumentReference templateReference =
            new DocumentReference("xwiki", List.of("OIDCPro", "Code", "Templates"), "EntraTemplate");
        when(this.documentReferenceResolver.resolve(TEMPLATE_PAGE)).thenReturn(templateReference);
        XWikiDocument templateDocument = mock(XWikiDocument.class);
        when(wiki.getDocument(templateReference, this.context)).thenReturn(templateDocument);
        this.templateObject = mock(BaseObject.class);
        when(templateDocument.getXObject(ConfigurationStore.PRO_TEMPLATE_CLASS)).thenReturn(this.templateObject);
        when(this.templateObject.getStringValue(OIDCProClientConfiguration.PROPERTY_NAME)).thenReturn("entra");

        when(this.templateIconResolver.getIconDataURI(this.templateObject)).thenReturn("data:image/png;base64,");
    }

    @Test
    void getConfigurationsFromCache() throws Exception
    {
        OIDCProClientConfiguration cachedConfiguration = mock(OIDCProClientConfiguration.class);
        when(this.proClientConfigurationCache.get(CONFIGURATION_NAME)).thenReturn(cachedConfiguration);

        assertEquals(List.of(cachedConfiguration), this.store.getConfigurations());

        verify(this.context.getWiki(), never()).getDocument(any(DocumentReference.class), any(XWikiContext.class));
    }

    @Test
    void getConfigurationsLoadsConfigurationBoundToTemplate()
    {
        BaseObject binderObject = mock(BaseObject.class);
        when(binderObject.getStringValue(TEMPLATE_NAME)).thenReturn("entra");
        when(this.configurationDocument.getXObject(ConfigurationStore.PRO_TEMPLATE_BINDER_CLASS))
            .thenReturn(binderObject);

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        OIDCProClientConfiguration configuration = configurations.get(0);
        assertEquals("entra", configuration.getTemplateName());
        assertEquals("data:image/png;base64,", configuration.getIconURL());
        verify(this.templateQuery).bindValue(TEMPLATE_NAME, "entra");
        verify(this.configurationCache).set(eq(CONFIGURATION_NAME), any(OIDCClientConfiguration.class));
        verify(this.proClientConfigurationCache).set(CONFIGURATION_NAME, configuration);
    }

    @Test
    void getConfigurationsWithCachedClientConfigurationAndDefaultTemplate()
    {
        OIDCClientConfiguration clientConfiguration = new OIDCClientConfiguration(this.clientObject);
        OIDCClientConfigurationCache.CacheEntry cacheEntry = mock(OIDCClientConfigurationCache.CacheEntry.class);
        when(cacheEntry.getConfiguration()).thenReturn(clientConfiguration);
        when(this.configurationCache.get(CONFIGURATION_NAME)).thenReturn(cacheEntry);

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertSame(clientConfiguration, configurations.get(0).getClientConfiguration());
        verify(this.templateQuery).bindValue(TEMPLATE_NAME, "default");
        verify(this.configurationCache, never()).set(any(), any());
    }

    @Test
    void getConfigurationsWhenQueryFails() throws Exception
    {
        when(this.configurationsQuery.execute()).thenThrow(new QueryException("error", null, null));

        assertTrue(this.store.getConfigurations().isEmpty());
        assertEquals("Failed to retrieve the oidc configurations.", this.logCapture.getMessage(0));
    }

    @Test
    void getConfigurationsWithoutConfigurations() throws Exception
    {
        when(this.configurationsQuery.execute()).thenReturn(Collections.emptyList());

        assertTrue(this.store.getConfigurations().isEmpty());
    }
}
