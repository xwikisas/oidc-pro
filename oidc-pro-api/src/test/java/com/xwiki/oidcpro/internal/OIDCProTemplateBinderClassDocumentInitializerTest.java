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

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.sheet.SheetBinder;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.test.MockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.InjectMockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.OldcoreTest;
import com.xpn.xwiki.test.reference.ReferenceComponentList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OIDCProTemplateBinderClassDocumentInitializer}.
 *
 * @version $Id$
 */
@OldcoreTest
@ReferenceComponentList
class OIDCProTemplateBinderClassDocumentInitializerTest
{
    private static final DocumentReference CLASS_REFERENCE =
        new DocumentReference("xwiki", List.of("OIDCPro", "Code"), "OIDCProTemplateBinderClass");

    private static final DocumentReference SHEET_REFERENCE =
        new DocumentReference("xwiki", List.of("OIDCPro", "Code"), "OIDCProConfigSheet");

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockitoOldcore
    private MockitoOldcore oldcore;

    @InjectMockComponents
    private OIDCProTemplateBinderClassDocumentInitializer initializer;

    @MockComponent
    @Named("class")
    private SheetBinder classSheetBinder;

    private XWikiDocument document;

    @BeforeEach
    void setUp()
    {
        this.document = new XWikiDocument(CLASS_REFERENCE);
    }

    @Test
    void updateDocumentCreatesTheClassAndBindsTheConfigurationSheet()
    {
        when(this.classSheetBinder.bind(this.document, SHEET_REFERENCE)).thenReturn(true);

        assertTrue(this.initializer.updateDocument(this.document));

        BaseClass xclass = this.document.getXClass();
        assertArrayEquals(new String[] { "templateName" }, xclass.getPropertyNames());
        assertInstanceOf(StringClass.class, xclass.get("templateName"));
        assertTrue(this.document.isHidden());
        assertEquals(new DocumentReference("xwiki", List.of("OIDCPro", "Code"), "WebHome"),
            this.document.getParentReference());
        verify(this.classSheetBinder).bind(this.document, SHEET_REFERENCE);

        List<BaseObject> rightsObjects = this.document.getXObjects(AbstractOIDCProClassDocumentInitializer.RIGHTS_CLASS);
        assertEquals(1, rightsObjects.size());
        assertEquals("XWiki.XWikiAdminGroup", rightsObjects.get(0).getLargeStringValue("groups"));
        assertEquals("edit,delete", rightsObjects.get(0).getStringValue("levels"));
        assertEquals(1, rightsObjects.get(0).getIntValue("allow"));
    }

    @Test
    void updateDocumentUnbindsOtherSheets()
    {
        DocumentReference otherSheet = new DocumentReference("xwiki", "Sandbox", "Sheet");
        when(this.classSheetBinder.getSheets(this.document)).thenReturn(List.of(otherSheet, SHEET_REFERENCE));
        this.initializer.updateDocument(this.document);
        when(this.classSheetBinder.unbind(this.document, otherSheet)).thenReturn(true);

        assertTrue(this.initializer.updateDocument(this.document));

        verify(this.classSheetBinder, times(2)).unbind(this.document, otherSheet);
        verify(this.classSheetBinder, never()).unbind(this.document, SHEET_REFERENCE);
        verify(this.classSheetBinder, never()).bind(any(), any());
    }

    @Test
    void updateDocumentWhenTheRightsCannotBeRestricted() throws Exception
    {
        XWikiDocument spiedDocument = spy(this.document);
        doThrow(new XWikiException()).when(spiedDocument)
            .newXObject(eq(AbstractOIDCProClassDocumentInitializer.RIGHTS_CLASS), any(XWikiContext.class));
        when(this.classSheetBinder.getSheets(spiedDocument)).thenReturn(List.of(SHEET_REFERENCE));
        this.initializer.updateDocument(spiedDocument);

        assertFalse(this.initializer.updateDocument(spiedDocument));

        assertTrue(spiedDocument.getXObjects(AbstractOIDCProClassDocumentInitializer.RIGHTS_CLASS).isEmpty());
        assertEquals("Failed to restrict the edit right on [xwiki:OIDCPro.Code.OIDCProTemplateBinderClass] to the "
            + "wiki administrators.", this.logCapture.getMessage(0));
        assertEquals("Failed to restrict the edit right on [xwiki:OIDCPro.Code.OIDCProTemplateBinderClass] to the "
            + "wiki administrators.", this.logCapture.getMessage(1));
    }

    @Test
    void updateDocumentWhenUpToDate()
    {
        when(this.classSheetBinder.getSheets(this.document)).thenReturn(List.of(SHEET_REFERENCE));
        this.initializer.updateDocument(this.document);

        assertFalse(this.initializer.updateDocument(this.document));

        verify(this.classSheetBinder, never()).bind(any(), any());
    }
}
