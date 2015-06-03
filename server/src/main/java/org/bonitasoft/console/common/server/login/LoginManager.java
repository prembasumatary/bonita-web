/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.console.common.server.login;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.bonitasoft.console.common.server.auth.AuthenticationFailedException;
import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerFactory;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerNotFoundException;
import org.bonitasoft.console.common.server.login.datastore.Credentials;
import org.bonitasoft.console.common.server.login.datastore.UserLogger;
import org.bonitasoft.console.common.server.utils.PermissionsBuilder;
import org.bonitasoft.console.common.server.utils.PermissionsBuilderAccessor;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.user.User;


/**
 * @author Anthony Birembaut
 */
public class LoginManager {

    /**
     * default locale
     */
    private static final String DEFAULT_LOCALE = "en";

    public void login(final HttpServletRequestAccessor request, final long tenantId, final UserLogger userLoger, final Credentials credentials)
            throws AuthenticationFailedException, ServletException, LoginFailedException {
        final Map<String, Serializable> credentialsMap = getAuthenticationManager(tenantId).authenticate(request, credentials);
        APISession apiSession;
        if (credentialsMap == null || credentialsMap.isEmpty()) {
            apiSession = userLoger.doLogin(credentials);
        } else {
            apiSession = userLoger.doLogin(credentialsMap);
        }
        storeCredentials(request, apiSession);
    }

    protected AuthenticationManager getAuthenticationManager(final long tenantId) throws ServletException {
        try {
            return AuthenticationManagerFactory.getAuthenticationManager(tenantId);
        } catch (final AuthenticationManagerNotFoundException e) {
            throw new ServletException(e);
        }
    }

    protected void storeCredentials(final HttpServletRequestAccessor request, final APISession session) throws LoginFailedException {
        String local = DEFAULT_LOCALE;
        if (request.getParameterMap().get("_l") != null
                && request.getParameterMap().get("_l").length >= 0) {
            local = request.getParameterMap().get("_l")[0];
        }
        final User user = new User(request.getUsername(), local);
        final PermissionsBuilder permissionsBuilder = createPermissionsBuilder(session);
        final Set<String> permissions = permissionsBuilder.getPermissions();
        SessionUtil.sessionLogin(user, session, permissions, request.getHttpSession());
    }

    protected PermissionsBuilder createPermissionsBuilder(final APISession session) throws LoginFailedException {
        return PermissionsBuilderAccessor.createPermissionBuilder(session);
    }
}
