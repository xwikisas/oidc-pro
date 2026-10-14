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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

import static com.xwiki.oidcpro.OIDCProClientConfiguration.PROPERTY_CODE_INSERTION_POINT;
import static com.xwiki.oidcpro.OIDCProClientConfiguration.PROPERTY_CUSTOM_CODE;
import static com.xwiki.oidcpro.OIDCProClientConfiguration.PROPERTY_ICON_ATTACHMENT;
import static com.xwiki.oidcpro.OIDCProClientConfiguration.PROPERTY_ICON_NAME;
import static com.xwiki.oidcpro.OIDCProClientConfiguration.PROPERTY_LAYOUT_JSON;
import static com.xwiki.oidcpro.OIDCProClientConfiguration.PROPERTY_NAME;

/**
 * Create or update the {@code OIDCPro.Code.OIDCProTemplateClass} class, describing the OIDC Pro templates.
 *
 * @version $Id$
 */
@Component
@Named("OIDCPro.Code.OIDCProTemplateClass")
@Singleton
public class OIDCProTemplateClassDocumentInitializer extends AbstractMandatoryClassInitializer
{
    private static final int TEXT_SIZE = 30;

    private static final int TEXTAREA_COLUMNS = 40;

    private static final int TEXTAREA_ROWS = 5;

    /**
     * Default constructor.
     */
    public OIDCProTemplateClassDocumentInitializer()
    {
        super(ConfigurationStore.PRO_TEMPLATE_CLASS);
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        xclass.addTextAreaField(PROPERTY_LAYOUT_JSON, PROPERTY_LAYOUT_JSON, TEXTAREA_COLUMNS, TEXTAREA_ROWS,
            TextAreaClass.ContentType.PURE_TEXT);
        xclass.addTextField(PROPERTY_NAME, PROPERTY_NAME, TEXT_SIZE);
        xclass.addTextField(PROPERTY_ICON_NAME, PROPERTY_ICON_NAME, TEXT_SIZE);
        xclass.addTextField(PROPERTY_ICON_ATTACHMENT, PROPERTY_ICON_ATTACHMENT, TEXT_SIZE);
        ((PropertyClass) xclass.get(PROPERTY_ICON_ATTACHMENT))
            .setHint("Name of an image attached to this template page.");
        xclass.addTextAreaField(PROPERTY_CUSTOM_CODE, PROPERTY_CUSTOM_CODE, TEXTAREA_COLUMNS, TEXTAREA_ROWS,
            TextAreaClass.EditorType.TEXT, TextAreaClass.ContentType.WIKI_TEXT);
        xclass.addTextField(PROPERTY_CODE_INSERTION_POINT, PROPERTY_CODE_INSERTION_POINT, TEXT_SIZE);
    }
}
