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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;
import com.xpn.xwiki.test.MockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.InjectMockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.OldcoreTest;
import com.xpn.xwiki.test.reference.ReferenceComponentList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link OIDCProTemplateClassDocumentInitializer}.
 *
 * @version $Id$
 */
@OldcoreTest
@ReferenceComponentList
class OIDCProTemplateClassDocumentInitializerTest
{
    @InjectMockitoOldcore
    private MockitoOldcore oldcore;

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

    @Test
    void updateDocumentRestrictsTheEditRightToWikiAdministrators()
    {
        XWikiDocument document =
            new XWikiDocument(new DocumentReference("xwiki", List.of("OIDCPro", "Code"), "OIDCProTemplateClass"));

        assertTrue(this.initializer.updateDocument(document));
        assertFalse(this.initializer.updateDocument(document));

        List<BaseObject> rightsObjects = document.getXObjects(AbstractOIDCProClassDocumentInitializer.RIGHTS_CLASS);
        assertEquals(1, rightsObjects.size());
        assertEquals("XWiki.XWikiAdminGroup", rightsObjects.get(0).getLargeStringValue("groups"));
        assertEquals("edit,delete", rightsObjects.get(0).getStringValue("levels"));
        assertEquals(1, rightsObjects.get(0).getIntValue("allow"));
    }

    @Test
    void updateDocumentIgnoresOtherRightsRules() throws Exception
    {
        XWikiDocument document =
            new XWikiDocument(new DocumentReference("xwiki", List.of("OIDCPro", "Code"), "OIDCProTemplateClass"));
        addRightsObject(document, "XWiki.XWikiAdminGroup", "edit,delete", 0);
        addRightsObject(document, "XWiki.XWikiAllGroup", "edit,delete", 1);
        addRightsObject(document, "XWiki.XWikiAdminGroup", "edit", 1);

        assertTrue(this.initializer.updateDocument(document));

        List<BaseObject> rightsObjects = document.getXObjects(AbstractOIDCProClassDocumentInitializer.RIGHTS_CLASS);
        assertEquals(4, rightsObjects.size());
        assertEquals("XWiki.XWikiAdminGroup", rightsObjects.get(3).getLargeStringValue("groups"));
        assertEquals("edit,delete", rightsObjects.get(3).getStringValue("levels"));
        assertEquals(1, rightsObjects.get(3).getIntValue("allow"));
    }

    private void addRightsObject(XWikiDocument document, String group, String levels, int allow) throws Exception
    {
        BaseObject rightsObject = document.newXObject(AbstractOIDCProClassDocumentInitializer.RIGHTS_CLASS,
            this.oldcore.getXWikiContext());
        rightsObject.setLargeStringValue("groups", group);
        rightsObject.setStringValue("levels", levels);
        rightsObject.setIntValue("allow", allow);
    }
}
