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
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfigurationStore;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.user.UserReferenceSerializer;

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
public class ConfigurationStore
{
    /**
     * The name of the property holding the name of the template bound to an OIDC client configuration.
     */
    static final String TEMPLATE_NAME = "templateName";

    /**
     * The space holding the OIDC Pro code pages.
     */
    static final List<String> OIDC_PRO_CODE_SPACE = Arrays.asList("OIDCPro", "Code");

    /**
     * The class of the object that binds an OIDC client configuration to a template.
     */
    static final LocalDocumentReference PRO_TEMPLATE_BINDER_CLASS =
        new LocalDocumentReference(OIDC_PRO_CODE_SPACE, "OIDCProTemplateBinderClass");

    /**
     * The class of the OIDC Pro template objects.
     */
    static final LocalDocumentReference PRO_TEMPLATE_CLASS =
        new LocalDocumentReference(OIDC_PRO_CODE_SPACE, "OIDCProTemplateClass");

    private static final String DEFAULT_TEMPLATE = "default";

    @Inject
    private QueryManager queryManager;

    @Inject
    private OIDCClientConfigurationStore oidcConfigurationStore;

    @Inject
    private AuthorizationManager authorizationManager;

    @Inject
    @Named("document")
    private UserReferenceSerializer<DocumentReference> documentUserReferenceSerializer;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private OIDCProClientConfigurationCache proClientConfigurationCache;

    @Inject
    private TemplateIconResolver templateIconResolver;

    @Inject
    private Logger logger;

    /**
     * @return the OIDC configurations of the current wiki.
     */
    public List<OIDCProClientConfiguration> getConfigurations()
    {
        List<Object[]> result;
        try {
            result = queryManager.createQuery(
                "select distinct doc.fullName, oidc.configurationName from Document doc, "
                    + "doc.object('XWiki.OIDC.ClientConfigurationClass') as oidc where oidc.skipped <> '1'",
                Query.XWQL).execute();
        } catch (QueryException e) {
            logger.error("Failed to retrieve the oidc configurations.", e);
            return Collections.emptyList();
        }

        // A configuration that can't be loaded must not prevent the others from being offered.
        List<OIDCProClientConfiguration> configurations = new ArrayList<>();
        XWikiContext context = contextProvider.get();
        for (Object[] entry : result) {
            String serializedDocument = Objects.toString(entry[0], null);
            String cfgName = Objects.toString(entry[1], null);
            if (StringUtils.isAnyBlank(serializedDocument, cfgName)) {
                continue;
            }
            try {
                OIDCProClientConfiguration proClientConfiguration = proClientConfigurationCache.get(cfgName);
                if (proClientConfiguration == null) {
                    proClientConfiguration = getClientConfiguration(cfgName, serializedDocument, context);
                    if (proClientConfiguration == null) {
                        continue;
                    }
                    proClientConfigurationCache.set(cfgName, proClientConfiguration);
                }
                configurations.add(proClientConfiguration);
            } catch (QueryException | XWikiException e) {
                logger.error("Failed to retrieve the oidc configuration [{}] from [{}].", cfgName, serializedDocument,
                    e);
            }
        }
        return configurations;
    }

    private OIDCProClientConfiguration getClientConfiguration(String cfgName, String serializedDocument,
        XWikiContext context) throws XWikiException, QueryException
    {
        // Only the OIDC store decides which document provides a configuration, since it is the one that checks that
        // the document can be trusted. Any other document holding a configuration with the same name is ignored.
        XWikiDocument document = this.oidcConfigurationStore.getOIDCClientConfigurationDocument(cfgName);
        DocumentReference listedReference = this.documentReferenceResolver.resolve(serializedDocument);
        if (document == null || !listedReference.equals(document.getDocumentReference())) {
            logger.debug("Ignoring the OIDC client configuration [{}] from the untrusted document [{}].", cfgName,
                listedReference);
            return null;
        }

        OIDCClientConfiguration clientConfiguration = this.oidcConfigurationStore.getOIDCClientConfiguration(cfgName);
        if (clientConfiguration == null) {
            return null;
        }

        // The template only changes how the configuration is displayed, so configurations created outside OIDC Pro
        // are still offered when no template applies to them.
        BaseObject templateObj = getTemplateObject(context, document);
        if (templateObj == null) {
            logger.debug("No trusted OIDC Pro template was found for the OIDC client configuration [{}].", cfgName);
        }
        return new OIDCProClientConfiguration(clientConfiguration, templateObj,
            templateIconResolver.getIconDataURI(templateObj));
    }

    private BaseObject getTemplateObject(XWikiContext context, XWikiDocument document)
        throws QueryException, XWikiException
    {
        BaseObject templateBinderObj = document.getXObject(PRO_TEMPLATE_BINDER_CLASS);
        String templateId = DEFAULT_TEMPLATE;
        if (templateBinderObj != null && !StringUtils.isEmpty(templateBinderObj.getStringValue(TEMPLATE_NAME))) {
            templateId = templateBinderObj.getStringValue(TEMPLATE_NAME);
        }

        BaseObject templateObj = findTrustedTemplate(templateId, context);
        if (templateObj == null && !DEFAULT_TEMPLATE.equals(templateId)) {
            templateObj = findTrustedTemplate(DEFAULT_TEMPLATE, context);
        }
        return templateObj;
    }

    private BaseObject findTrustedTemplate(String templateId, XWikiContext context)
        throws QueryException, XWikiException
    {
        XWikiDocument templateDoc = getTrustedTemplateDocument(templateId, context);
        return templateDoc != null ? templateDoc.getXObject(PRO_TEMPLATE_CLASS) : null;
    }

    /**
     * Templates contribute code to the login page and to the configuration sheet, so only the templates written by a
     * wiki administrator are returned.
     *
     * @param templateName the name of the template
     * @return the reference of the first trusted document holding a template with the given name, or {@code null} if
     *     there is none
     * @throws QueryException if the templates can't be retrieved
     * @throws XWikiException if a template document can't be loaded
     */
    public DocumentReference getTemplateReference(String templateName) throws QueryException, XWikiException
    {
        XWikiDocument templateDoc = getTrustedTemplateDocument(templateName, contextProvider.get());
        return templateDoc != null ? templateDoc.getDocumentReference() : null;
    }

    /**
     * Templates contribute code to the login page and to the configuration sheet, so only the templates written by a
     * wiki administrator are returned.
     *
     * @return the references of the trusted documents holding a template
     * @throws QueryException if the templates can't be retrieved
     * @throws XWikiException if a template document can't be loaded
     */
    public List<DocumentReference> getTemplateReferences() throws QueryException, XWikiException
    {
        XWikiContext context = contextProvider.get();
        List<DocumentReference> templateReferences = new ArrayList<>();
        List<Object> result =
            queryManager.createQuery("from doc.object('OIDCPro.Code.OIDCProTemplateClass') as template", Query.XWQL)
                .execute();
        for (Object templateDocRef : result) {
            XWikiDocument templateDoc = getDocument(templateDocRef, context);
            if (isTrustedTemplate(templateDoc)) {
                templateReferences.add(templateDoc.getDocumentReference());
            }
        }
        return templateReferences;
    }

    private XWikiDocument getTrustedTemplateDocument(String templateName, XWikiContext context)
        throws QueryException, XWikiException
    {
        List<Object> result = queryManager.createQuery("from doc.object('OIDCPro.Code.OIDCProTemplateClass') as "
            + "template where template"
            + ".name = :templateName", Query.XWQL).bindValue(TEMPLATE_NAME, templateName).execute();
        for (Object templateDocRef : result) {
            XWikiDocument templateDoc = getDocument(templateDocRef, context);
            if (isTrustedTemplate(templateDoc)) {
                return templateDoc;
            }
        }
        return null;
    }

    private XWikiDocument getDocument(Object serializedReference, XWikiContext context) throws XWikiException
    {
        return context.getWiki().getDocument(documentReferenceResolver.resolve(serializedReference.toString()),
            context);
    }

    private boolean isTrustedTemplate(XWikiDocument templateDoc)
    {
        DocumentReference author =
            this.documentUserReferenceSerializer.serialize(templateDoc.getAuthors().getEffectiveMetadataAuthor());
        boolean trusted = this.authorizationManager.hasAccess(Right.ADMIN, author,
            templateDoc.getDocumentReference().getWikiReference());
        if (!trusted) {
            logger.debug("Ignoring the OIDC Pro template from [{}] because its author [{}] is not a wiki "
                + "administrator.", templateDoc.getDocumentReference(), author);
        }
        return trusted;
    }
}
