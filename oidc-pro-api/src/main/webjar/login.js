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
/**
 * When the user picks an OIDC client (on the login page or in the top menu), remember it in the cookie read by the
 * OIDC authenticator. The element then does its default action: the button submits the login form and the link opens
 * the login page.
 * <p>
 * Page content can declare elements that look like the OIDC Pro login entries, so the name of the cookie and the
 * clients that can be selected are read from the script element, which only OIDC Pro renders, and never from the
 * clicked element.
 */
(function (script) {
  if (window.oidcProLoginInitialized || !script) {
    return;
  }

  // The name of a cookie must be a token (RFC 6265).
  var COOKIE_NAME_PATTERN = /^[!#$%&'*+\-.^_`|~0-9A-Za-z]+$/;
  var cookieName = script.dataset.oidcproCookie;
  var clients;
  try {
    clients = JSON.parse(script.dataset.oidcproClients || '[]');
  } catch (e) {
    return;
  }
  if (!COOKIE_NAME_PATTERN.test(cookieName || '') || !Array.isArray(clients)) {
    return;
  }
  window.oidcProLoginInitialized = true;

  // The cookie is only needed until the login request reads it, after which the OIDC authenticator keeps the selected
  // client in the session. Expire it shortly so that it doesn't preselect the client for later logins.
  var COOKIE_MAX_AGE = 300;

  document.addEventListener('click', function (event) {
    var element = event.target.closest('[data-oidcpro-client]');
    if (!element || clients.indexOf(element.dataset.oidcproClient) < 0) {
      return;
    }
    var cookie = cookieName + '=' + encodeURIComponent(element.dataset.oidcproClient) +
      '; path=/; Max-Age=' + COOKIE_MAX_AGE + '; SameSite=Lax';
    if (window.location.protocol === 'https:') {
      cookie += '; Secure';
    }
    document.cookie = cookie;
  });
})(document.currentScript);
