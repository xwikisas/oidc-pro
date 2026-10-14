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
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Unit tests for {@link OIDCProTemplateClassDocumentInitializer}.
 *
 * @version $Id$
 */
@ComponentTest
class OIDCProTemplateClassDocumentInitializerTest
{
    @InjectMockComponents
    private OIDCProTemplateClassDocumentInitializer initializer;

    @Test
    void createClass()
    {
        BaseClass xclass = new BaseClass();

        this.initializer.createClass(xclass);

        assertArrayEquals(new String[] { "layoutJson", "name", "iconName", "iconAttachment", "customCode",
            "codeInsertionPoint" }, xclass.getPropertyNames());

        TextAreaClass layoutJson = (TextAreaClass) xclass.get("layoutJson");
        assertEquals(TextAreaClass.ContentType.PURE_TEXT, TextAreaClass.ContentType.getByValue(layoutJson.getContentType()));

        TextAreaClass customCode = (TextAreaClass) xclass.get("customCode");
        assertEquals(TextAreaClass.ContentType.WIKI_TEXT, TextAreaClass.ContentType.getByValue(customCode.getContentType()));
        assertEquals(TextAreaClass.EditorType.TEXT, TextAreaClass.EditorType.getByValue(customCode.getEditor()));

        assertInstanceOf(StringClass.class, xclass.get("codeInsertionPoint"));
        assertEquals("Name of an image attached to this template page.",
            ((PropertyClass) xclass.get("iconAttachment")).getStringValue("hint"));
    }
}
