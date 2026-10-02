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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.oidcpro.OIDCProClientConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TemplateIconResolver}.
 *
 * @version $Id$
 */
@ComponentTest
class TemplateIconResolverTest
{
    private static final String ICON_NAME = "logo.png";

    private static final DocumentReference TEMPLATE_REFERENCE =
        new DocumentReference("xwiki", "Templates", "MyTemplate");

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private TemplateIconResolver resolver;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    private XWikiContext context;

    private BaseObject templateObject;

    private XWikiDocument templateDocument;

    private XWikiAttachment attachment;

    @BeforeEach
    void setUp()
    {
        this.context = mock(XWikiContext.class);
        when(this.contextProvider.get()).thenReturn(this.context);

        this.templateDocument = mock(XWikiDocument.class);
        when(this.templateDocument.getDocumentReference()).thenReturn(TEMPLATE_REFERENCE);

        this.templateObject = mock(BaseObject.class);
        when(this.templateObject.getOwnerDocument()).thenReturn(this.templateDocument);
        when(this.templateObject.getStringValue(OIDCProClientConfiguration.PROPERTY_ICON_ATTACHMENT))
            .thenReturn(ICON_NAME);

        this.attachment = mock(XWikiAttachment.class);
        when(this.templateDocument.getAttachment(ICON_NAME)).thenReturn(this.attachment);
        when(this.attachment.getMimeType(this.context)).thenReturn("image/png");
        when(this.attachment.getLongSize()).thenReturn(4L);
    }

    @Test
    void getIconDataURI() throws Exception
    {
        byte[] content = "icon".getBytes(StandardCharsets.UTF_8);
        when(this.attachment.getContentInputStream(this.context)).thenReturn(new ByteArrayInputStream(content));

        assertEquals("data:image/png;base64," + Base64.getEncoder().encodeToString(content),
            this.resolver.getIconDataURI(this.templateObject));
    }

    @Test
    void getIconDataURIWithoutIcon()
    {
        when(this.templateObject.getStringValue(OIDCProClientConfiguration.PROPERTY_ICON_ATTACHMENT)).thenReturn(" ");

        assertEquals("", this.resolver.getIconDataURI(this.templateObject));
    }

    @Test
    void getIconDataURIWhenAttachmentIsMissing()
    {
        when(this.templateDocument.getAttachment(ICON_NAME)).thenReturn(null);

        assertEquals("", this.resolver.getIconDataURI(this.templateObject));
        assertEquals("The OIDC Pro icon [logo.png] is not attached to [xwiki:Templates.MyTemplate].",
            this.logCapture.getMessage(0));
    }

    @Test
    void getIconDataURIWhenAttachmentIsNotAnImage()
    {
        when(this.attachment.getMimeType(this.context)).thenReturn("text/html");

        assertEquals("", this.resolver.getIconDataURI(this.templateObject));
        assertEquals("The OIDC Pro icon [logo.png] attached to [xwiki:Templates.MyTemplate] is not an image or is "
            + "bigger than [524288] bytes.", this.logCapture.getMessage(0));
    }

    @Test
    void getIconDataURIWhenAttachmentIsTooBig()
    {
        when(this.attachment.getLongSize()).thenReturn(512L * 1024 + 1);

        assertEquals("", this.resolver.getIconDataURI(this.templateObject));
        assertEquals("The OIDC Pro icon [logo.png] attached to [xwiki:Templates.MyTemplate] is not an image or is "
            + "bigger than [524288] bytes.", this.logCapture.getMessage(0));
    }

    @Test
    void getIconDataURIWhenContentCannotBeRead() throws Exception
    {
        when(this.attachment.getContentInputStream(this.context)).thenThrow(new XWikiException());

        assertEquals("", this.resolver.getIconDataURI(this.templateObject));
        assertEquals("Failed to load the OIDC Pro icon [logo.png] attached to [xwiki:Templates.MyTemplate].",
            this.logCapture.getMessage(0));
    }
}
