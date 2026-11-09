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

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Base class of the initializers of the OIDC Pro classes. The OIDC Pro classes decide how the OIDC client
 * configurations are displayed and which code their templates contribute, so only wiki administrators are allowed to
 * edit (or delete) them.
 *
 * @version $Id$
 * @since 1.1.0
 */
public abstract class AbstractOIDCProClassDocumentInitializer extends AbstractMandatoryClassInitializer
{
    /**
     * The class of the page rights objects.
     */
    static final LocalDocumentReference RIGHTS_CLASS = new LocalDocumentReference("XWiki", "XWikiRights");

    /**
     * The group allowed to edit the OIDC Pro classes.
     */
    static final String ADMIN_GROUP = "XWiki.XWikiAdminGroup";

    /**
     * The rights restricted to the {@link #ADMIN_GROUP}.
     */
    static final String RESTRICTED_LEVELS = "edit,delete";

    static final String GROUPS = "groups";

    static final String LEVELS = "levels";

    static final String ALLOW = "allow";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Logger logger;

    /**
     * @param reference the reference of the class document
     */
    protected AbstractOIDCProClassDocumentInitializer(EntityReference reference)
    {
        super(reference);
    }

    @Override
    public boolean updateDocument(XWikiDocument document)
    {
        boolean needsUpdate = super.updateDocument(document);
        needsUpdate |= updateRights(document);
        return needsUpdate;
    }

    private boolean updateRights(XWikiDocument document)
    {
        // Allowing a right to a group denies it to everyone else.
        boolean restricted = document.getXObjects(RIGHTS_CLASS).stream().filter(Objects::nonNull)
            .anyMatch(rightsObject -> rightsObject.getIntValue(ALLOW) == 1
                && ADMIN_GROUP.equals(rightsObject.getLargeStringValue(GROUPS))
                && RESTRICTED_LEVELS.equals(rightsObject.getStringValue(LEVELS)));
        if (restricted) {
            return false;
        }

        try {
            BaseObject rightsObject = document.newXObject(RIGHTS_CLASS, this.contextProvider.get());
            rightsObject.setLargeStringValue(GROUPS, ADMIN_GROUP);
            rightsObject.setStringValue(LEVELS, RESTRICTED_LEVELS);
            rightsObject.setIntValue(ALLOW, 1);
            return true;
        } catch (XWikiException e) {
            this.logger.error("Failed to restrict the edit right on [{}] to the wiki administrators.",
                document.getDocumentReference(), e);
            return false;
        }
    }
}
