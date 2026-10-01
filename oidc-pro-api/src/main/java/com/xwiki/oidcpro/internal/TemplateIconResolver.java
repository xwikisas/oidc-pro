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

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.oidcpro.OIDCProClientConfiguration;

/**
 * Retrieve the icon of an OIDC Pro template.
 *
 * @version $Id$
 */
@Component(roles = TemplateIconResolver.class)
@Singleton
public class TemplateIconResolver
{
    private static final String IMAGE_MIME_TYPE_PREFIX = "image/";

    private static final long MAX_ICON_SIZE = 512L * 1024;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Logger logger;

    /**
     * The icon is an image attached to the template page. It is inlined as a data URI because the login page is
     * displayed to guest users, who usually can't view the template page.
     *
     * @param templateObj the template object that references the icon
     * @return the icon as a data URI, or an empty string if the template has no valid icon
     */
    public String getIconDataURI(BaseObject templateObj)
    {
        String iconAttachment = templateObj.getStringValue(OIDCProClientConfiguration.PROPERTY_ICON_ATTACHMENT);
        if (StringUtils.isBlank(iconAttachment)) {
            return "";
        }

        XWikiContext context = contextProvider.get();
        XWikiDocument templateDoc = templateObj.getOwnerDocument();
        XWikiAttachment attachment = templateDoc.getAttachment(iconAttachment);
        if (attachment == null) {
            logger.warn("The OIDC Pro icon [{}] is not attached to [{}].", iconAttachment,
                templateDoc.getDocumentReference());
            return "";
        }
        String mimeType = attachment.getMimeType(context);
        if (!StringUtils.startsWith(mimeType, IMAGE_MIME_TYPE_PREFIX) || attachment.getLongSize() > MAX_ICON_SIZE) {
            logger.warn("The OIDC Pro icon [{}] attached to [{}] is not an image or is bigger than [{}] bytes.",
                iconAttachment, templateDoc.getDocumentReference(), MAX_ICON_SIZE);
            return "";
        }
        try (InputStream content = attachment.getContentInputStream(context)) {
            return String.format("data:%s;base64,%s", mimeType,
                Base64.getEncoder().encodeToString(IOUtils.toByteArray(content)));
        } catch (XWikiException | IOException e) {
            logger.error("Failed to load the OIDC Pro icon [{}] attached to [{}].", iconAttachment,
                templateDoc.getDocumentReference(), e);
            return "";
        }
    }
}
