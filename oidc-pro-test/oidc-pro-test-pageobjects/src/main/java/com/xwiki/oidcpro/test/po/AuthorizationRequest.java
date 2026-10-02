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
package com.xwiki.oidcpro.test.po;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents the authorization request that the OIDC authenticator sends to a provider, as seen in the URL the
 * browser is redirected to. The provider itself is not involved, so the tests don't need a real one.
 *
 * @version $Id$
 */
public class AuthorizationRequest extends BaseElement
{
    private final Map<String, String> parameters = new HashMap<>();

    /**
     * Wait for the browser to be redirected to the given authorization endpoint.
     *
     * @param authorizationEndpoint the authorization endpoint of the provider
     */
    public AuthorizationRequest(String authorizationEndpoint)
    {
        String url = getDriver().waitUntilCondition(driver -> {
            String currentURL = driver.getCurrentUrl();
            return currentURL.startsWith(authorizationEndpoint) ? currentURL : null;
        });
        String query = URI.create(url).getRawQuery();
        if (query != null) {
            for (String parameter : query.split("&")) {
                String[] keyValue = parameter.split("=", 2);
                this.parameters.put(decode(keyValue[0]), keyValue.length > 1 ? decode(keyValue[1]) : "");
            }
        }
    }

    /**
     * @param name the name of a request parameter, e.g. {@code client_id}
     * @return the value of the parameter, or {@code null} if the request doesn't have it
     */
    public String getParameter(String name)
    {
        return this.parameters.get(name);
    }

    private static String decode(String value)
    {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
