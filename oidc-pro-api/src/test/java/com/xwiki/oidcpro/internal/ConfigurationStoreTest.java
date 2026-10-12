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

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfigurationStore;
import org.xwiki.model.document.DocumentAuthors;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.user.UserReference;
import org.xwiki.user.UserReferenceSerializer;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.oidcpro.OIDCProClientConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    private static final String PLANTED_PAGE = "Sandbox.Planted";

    private static final String TEMPLATE_PAGE = "OIDCPro.Code.Templates.EntraTemplate";

    private static final String UNTRUSTED_TEMPLATE_PAGE = "Sandbox.PlantedTemplate";

    private static final String TEMPLATE_NAME = "templateName";

    private static final WikiReference WIKI = new WikiReference("xwiki");

    private static final DocumentReference ADMIN = new DocumentReference("xwiki", "XWiki", "Admin");

    private static final DocumentReference EDITOR = new DocumentReference("xwiki", "XWiki", "Editor");

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private ConfigurationStore store;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private OIDCClientConfigurationStore oidcConfigurationStore;

    @MockComponent
    private AuthorizationManager authorizationManager;

    @MockComponent
    @Named("document")
    private UserReferenceSerializer<DocumentReference> documentUserReferenceSerializer;

    @MockComponent
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @MockComponent
    private OIDCProClientConfigurationCache proClientConfigurationCache;

    @MockComponent
    private TemplateIconResolver templateIconResolver;

    private XWikiContext context;

    private XWiki wiki;

    private Query configurationsQuery;

    private Query templateQuery;

    private XWikiDocument configurationDocument;

    private OIDCClientConfiguration clientConfiguration;

    @BeforeEach
    void setUp() throws Exception
    {
        this.context = mock(XWikiContext.class);
        this.wiki = mock(XWiki.class);
        when(this.context.getWiki()).thenReturn(this.wiki);
        when(this.contextProvider.get()).thenReturn(this.context);
        when(this.authorizationManager.hasAccess(Right.ADMIN, ADMIN, WIKI)).thenReturn(true);

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

        this.configurationDocument = mockDocument(CONFIGURATION_PAGE, ADMIN);
        when(this.oidcConfigurationStore.getOIDCClientConfigurationDocument(CONFIGURATION_NAME))
            .thenReturn(this.configurationDocument);
        this.clientConfiguration = new OIDCClientConfiguration(mock(BaseObject.class));
        when(this.oidcConfigurationStore.getOIDCClientConfiguration(CONFIGURATION_NAME))
            .thenReturn(this.clientConfiguration);

        mockTemplate(TEMPLATE_PAGE, ADMIN, "entra");
    }

    private XWikiDocument mockDocument(String serializedReference, DocumentReference author) throws Exception
    {
        DocumentReference reference = new DocumentReference("xwiki", "Space", serializedReference);
        when(this.documentReferenceResolver.resolve(serializedReference)).thenReturn(reference);
        XWikiDocument document = mock(XWikiDocument.class);
        when(document.getDocumentReference()).thenReturn(reference);
        UserReference authorUserReference = mock(UserReference.class);
        DocumentAuthors authors = mock(DocumentAuthors.class);
        when(authors.getEffectiveMetadataAuthor()).thenReturn(authorUserReference);
        when(document.getAuthors()).thenReturn(authors);
        when(this.documentUserReferenceSerializer.serialize(authorUserReference)).thenReturn(author);
        when(this.wiki.getDocument(reference, this.context)).thenReturn(document);
        return document;
    }

    private BaseObject mockTemplate(String serializedReference, DocumentReference author, String name)
        throws Exception
    {
        XWikiDocument templateDocument = mockDocument(serializedReference, author);
        BaseObject template = mock(BaseObject.class);
        when(templateDocument.getXObject(ConfigurationStore.PRO_TEMPLATE_CLASS)).thenReturn(template);
        when(template.getStringValue(OIDCProClientConfiguration.PROPERTY_NAME)).thenReturn(name);
        when(this.templateIconResolver.getIconDataURI(template)).thenReturn("data:image/png;base64," + name);
        return template;
    }

    @Test
    void getConfigurationsFromCache() throws Exception
    {
        OIDCProClientConfiguration cachedConfiguration = mock(OIDCProClientConfiguration.class);
        when(this.proClientConfigurationCache.get(CONFIGURATION_NAME)).thenReturn(cachedConfiguration);

        assertEquals(List.of(cachedConfiguration), this.store.getConfigurations());

        verify(this.oidcConfigurationStore, never()).getOIDCClientConfigurationDocument(any());
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
        assertSame(this.clientConfiguration, configuration.getClientConfiguration());
        assertEquals("entra", configuration.getTemplateName());
        assertEquals("data:image/png;base64,entra", configuration.getIconURL());
        verify(this.templateQuery).bindValue(TEMPLATE_NAME, "entra");
        verify(this.proClientConfigurationCache).set(CONFIGURATION_NAME, configuration);
    }

    @Test
    void getConfigurationsWithDefaultTemplate()
    {
        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertEquals("entra", configurations.get(0).getTemplateName());
        verify(this.templateQuery).bindValue(TEMPLATE_NAME, "default");
    }

    @Test
    void getConfigurationsIgnoresConfigurationNotSelectedByTheOIDCStore() throws Exception
    {
        // The OIDC store only selects documents authored by a wiki administrator, so a configuration planted by
        // another user is never the one it returns.
        mockDocument(PLANTED_PAGE, EDITOR);
        when(this.configurationsQuery.execute()).thenReturn(List.of(new Object[] { PLANTED_PAGE, CONFIGURATION_NAME },
            new Object[] { CONFIGURATION_PAGE, CONFIGURATION_NAME }));

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertSame(this.clientConfiguration, configurations.get(0).getClientConfiguration());
    }

    @Test
    void getConfigurationsWithoutTrustedConfigurationDocument() throws Exception
    {
        when(this.oidcConfigurationStore.getOIDCClientConfigurationDocument(CONFIGURATION_NAME)).thenReturn(null);

        assertTrue(this.store.getConfigurations().isEmpty());

        verify(this.oidcConfigurationStore, never()).getOIDCClientConfiguration(any());
        verify(this.proClientConfigurationCache, never()).set(any(), any());
    }

    @Test
    void getConfigurationsWhenOIDCStoreReturnsNoConfiguration() throws Exception
    {
        when(this.oidcConfigurationStore.getOIDCClientConfiguration(CONFIGURATION_NAME)).thenReturn(null);

        assertTrue(this.store.getConfigurations().isEmpty());
    }

    @Test
    void getConfigurationsIgnoresTemplateNotAuthoredByWikiAdmin() throws Exception
    {
        mockTemplate(UNTRUSTED_TEMPLATE_PAGE, EDITOR, "planted");
        when(this.templateQuery.execute()).thenReturn(List.of(UNTRUSTED_TEMPLATE_PAGE, TEMPLATE_PAGE));

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertEquals("entra", configurations.get(0).getTemplateName());
    }

    @Test
    void getConfigurationsFallsBackToDefaultTemplateWhenBoundTemplateIsNotTrusted() throws Exception
    {
        BaseObject binderObject = mock(BaseObject.class);
        when(binderObject.getStringValue(TEMPLATE_NAME)).thenReturn("planted");
        when(this.configurationDocument.getXObject(ConfigurationStore.PRO_TEMPLATE_BINDER_CLASS))
            .thenReturn(binderObject);
        mockTemplate(UNTRUSTED_TEMPLATE_PAGE, EDITOR, "planted");
        when(this.templateQuery.execute()).thenReturn(List.of(UNTRUSTED_TEMPLATE_PAGE), List.of(TEMPLATE_PAGE));

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertEquals("entra", configurations.get(0).getTemplateName());
        verify(this.templateQuery).bindValue(TEMPLATE_NAME, "planted");
        verify(this.templateQuery).bindValue(TEMPLATE_NAME, "default");
    }

    @Test
    void getConfigurationsWithoutTrustedTemplate() throws Exception
    {
        mockTemplate(UNTRUSTED_TEMPLATE_PAGE, EDITOR, "planted");
        when(this.templateQuery.execute()).thenReturn(List.of(UNTRUSTED_TEMPLATE_PAGE));
        when(this.templateIconResolver.getIconDataURI(null)).thenReturn("");

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        OIDCProClientConfiguration configuration = configurations.get(0);
        assertSame(this.clientConfiguration, configuration.getClientConfiguration());
        assertNull(configuration.getTemplateName());
        assertEquals("", configuration.getIconURL());
    }

    @Test
    void getConfigurationsCreatedByAnotherApplicationWithoutAnyTemplate() throws Exception
    {
        // Configurations created by other applications (e.g. the EntraID application) are not bound to a template.
        when(this.templateQuery.execute()).thenReturn(Collections.emptyList());

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertSame(this.clientConfiguration, configurations.get(0).getClientConfiguration());
        assertNull(configurations.get(0).getTemplateName());
        verify(this.proClientConfigurationCache).set(CONFIGURATION_NAME, configurations.get(0));
    }

    @Test
    void getTemplateReference() throws Exception
    {
        mockTemplate(UNTRUSTED_TEMPLATE_PAGE, EDITOR, "entra");
        when(this.templateQuery.execute()).thenReturn(List.of(UNTRUSTED_TEMPLATE_PAGE, TEMPLATE_PAGE));

        assertEquals(this.documentReferenceResolver.resolve(TEMPLATE_PAGE), this.store.getTemplateReference("entra"));
        verify(this.templateQuery).bindValue(TEMPLATE_NAME, "entra");
    }

    @Test
    void getTemplateReferenceWithoutTrustedTemplate() throws Exception
    {
        mockTemplate(UNTRUSTED_TEMPLATE_PAGE, EDITOR, "planted");
        when(this.templateQuery.execute()).thenReturn(List.of(UNTRUSTED_TEMPLATE_PAGE));

        assertNull(this.store.getTemplateReference("planted"));
    }

    @Test
    void getTemplateReferences() throws Exception
    {
        mockTemplate(UNTRUSTED_TEMPLATE_PAGE, EDITOR, "planted");
        String otherTemplatePage = "OIDCPro.Code.Templates.OtherTemplate";
        mockTemplate(otherTemplatePage, ADMIN, "other");
        Query allTemplatesQuery = mock(Query.class);
        when(this.queryManager.createQuery("from doc.object('OIDCPro.Code.OIDCProTemplateClass') as template",
            Query.XWQL)).thenReturn(allTemplatesQuery);
        when(allTemplatesQuery.execute())
            .thenReturn(List.of(TEMPLATE_PAGE, UNTRUSTED_TEMPLATE_PAGE, otherTemplatePage));

        assertEquals(List.of(this.documentReferenceResolver.resolve(TEMPLATE_PAGE),
            this.documentReferenceResolver.resolve(otherTemplatePage)), this.store.getTemplateReferences());
    }

    @Test
    void getConfigurationsWithUnknownBoundTemplate() throws Exception
    {
        BaseObject binderObject = mock(BaseObject.class);
        when(binderObject.getStringValue(TEMPLATE_NAME)).thenReturn("missing");
        when(this.configurationDocument.getXObject(ConfigurationStore.PRO_TEMPLATE_BINDER_CLASS))
            .thenReturn(binderObject);
        when(this.templateQuery.execute()).thenReturn(Collections.emptyList(), List.of(TEMPLATE_PAGE));

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertEquals("entra", configurations.get(0).getTemplateName());
    }

    @Test
    void getConfigurationsSkipsConfigurationThatFailsToLoad() throws Exception
    {
        String brokenName = "Broken";
        when(this.configurationsQuery.execute()).thenReturn(List.of(new Object[] { PLANTED_PAGE, brokenName },
            new Object[] { PLANTED_PAGE, null }, new Object[] { CONFIGURATION_PAGE, CONFIGURATION_NAME }));
        when(this.oidcConfigurationStore.getOIDCClientConfigurationDocument(brokenName))
            .thenThrow(new XWikiException());

        List<OIDCProClientConfiguration> configurations = this.store.getConfigurations();

        assertEquals(1, configurations.size());
        assertSame(this.clientConfiguration, configurations.get(0).getClientConfiguration());
        assertEquals("Failed to retrieve the oidc configuration [Broken] from [Sandbox.Planted].",
            this.logCapture.getMessage(0));
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
