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

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xwiki.licensing.Licensor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OIDCProLicenseChecker}.
 *
 * @version $Id$
 */
@ComponentTest
class OIDCProLicenseCheckerTest
{
    private static final DocumentReference APP_HOME = new DocumentReference("subwiki", "OIDCPro", "WebHome");

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private OIDCProLicenseChecker licenseChecker;

    @MockComponent
    private Licensor licensor;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    private XWikiContext context;

    private XWiki wiki;

    @BeforeEach
    void setUp()
    {
        this.context = mock(XWikiContext.class);
        this.wiki = mock(XWiki.class);
        when(this.context.getWiki()).thenReturn(this.wiki);
        when(this.context.getWikiReference()).thenReturn(new WikiReference("subwiki"));
        when(this.contextProvider.get()).thenReturn(this.context);
    }

    @Test
    void isLicensed() throws Exception
    {
        when(this.wiki.exists(APP_HOME, this.context)).thenReturn(true);
        when(this.licensor.hasLicensure(APP_HOME)).thenReturn(true);

        assertTrue(this.licenseChecker.isLicensed());
    }

    @Test
    void isLicensedWithoutLicense() throws Exception
    {
        when(this.wiki.exists(APP_HOME, this.context)).thenReturn(true);

        assertFalse(this.licenseChecker.isLicensed());
        verify(this.licensor).hasLicensure(APP_HOME);
    }

    @Test
    void isLicensedWithoutApplicationHome()
    {
        assertFalse(this.licenseChecker.isLicensed());
        verifyNoInteractions(this.licensor);
    }

    @Test
    void isLicensedWhenApplicationHomeCannotBeChecked() throws Exception
    {
        when(this.wiki.exists(APP_HOME, this.context)).thenThrow(new XWikiException());

        assertFalse(this.licenseChecker.isLicensed());
        verifyNoInteractions(this.licensor);
        assertEquals("Failed to check the license of OIDC Pro on [subwiki:OIDCPro.WebHome].",
            this.logCapture.getMessage(0));
    }
}
