/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.console.common.server.page;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.login.LoginManager;
import org.bonitasoft.console.common.server.utils.TenantFolder;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;

public class CustomPageServlet extends HttpServlet {

    /**
     * Logger
     */
    private static Logger LOGGER = Logger.getLogger(CustomPageServlet.class.getName());

    /**
     * uuid
     */
    private static final long serialVersionUID = -5410859017103815654L;

    public static final String APP_ID_PARAM = "applicationId";

    protected ResourceRenderer resourceRenderer = new ResourceRenderer();

    protected PageRenderer pageRenderer = new PageRenderer(resourceRenderer);

    protected TenantFolder tenantFolder = new TenantFolder();

    protected CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        /*
         * Check if requested URL is missing last slash, like "custom-page/page-name".
         * If missing, redirect to "custom-page/page-name/"
         */
        if (isPageUrlWithoutFinalSlash(request)) {
            customPageRequestModifier.redirectToValidPageUrl(request, response);
            return;
        }

        final String appID = request.getParameter(APP_ID_PARAM);
        final HttpSession session = request.getSession();
        final APISession apiSession = (APISession) session.getAttribute(LoginManager.API_SESSION_PARAM_KEY);

        final List<String> pathSegments = resourceRenderer.getPathSegments(request);
        if (pathSegments.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The name of the page is required.");
            return;
        }
        String pageName = pathSegments.get(0);

        try {

            if (isPageRequest(pathSegments)) {
                if (!isAuthorized(apiSession, appID, pageName)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not Authorized");
                    return;
                }
                pageRenderer.displayCustomPage(request, response, apiSession, pageName);
            } else {
                File resourceFile = getResourceFile(request.getPathInfo(), pageName, apiSession);
                resourceRenderer.renderFile(request, response, resourceFile);
            }

        } catch (final Exception e) {
            handleException(pageName, e);
        }

    }

    private boolean isPageRequest(List<String> pathSegments) {
        if (pathSegments.size() == 1) {
            return true;
        } else if (pathSegments.size() == 2) {
            return isAnIndexSegment(pathSegments.get(1));
        }
        return false;
    }

    private boolean isAnIndexSegment(String segment) {
        return segment.equalsIgnoreCase(CustomPageService.PAGE_INDEX_FILENAME) || segment.equalsIgnoreCase(CustomPageService.PAGE_CONTROLLER_FILENAME)
                || segment.equalsIgnoreCase(CustomPageService.PAGE_INDEX_NAME);
    }

    private boolean isPageUrlWithoutFinalSlash(final HttpServletRequest request) {
        return request.getPathInfo().matches("/[^/]+");
    }

    private File getResourceFile(String resourcePath, String pageName, APISession apiSession) throws IOException, BonitaException {
        final PageResourceProvider pageResourceProvider =  pageRenderer.getPageResourceProvider(pageName, apiSession.getTenantId());
        File resourceFile = new File(pageResourceProvider.getPageDirectory(), CustomPageService.RESOURCES_PROPERTY + File.separator
                + getResourcePathWithoutPageName(resourcePath, pageName));

        if (!tenantFolder.isInFolder(resourceFile, pageResourceProvider.getPageDirectory())) {
            throw new BonitaException("Unauthorized access to the file " + resourcePath);
        }
        return resourceFile;
    }

    private String getResourcePathWithoutPageName(String resourcePath, String pageName) {
        //resource path match "/pagename/resourcefolder/filename"
        return resourcePath.substring(pageName.length() + 2);
    }

    private boolean isAuthorized(final APISession apiSession, final String appID, final String pageName) throws BonitaException {
        return getCustomPageAuthorizationsHelper(apiSession).isPageAuthorized(appID, pageName);
    }

    private void handleException(final String pageName, final Exception e) throws ServletException {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.log(Level.WARNING, "Error while trying to render the custom page " + pageName, e);
        }
        throw new ServletException(e.getMessage());
    }

    protected CustomPageAuthorizationsHelper getCustomPageAuthorizationsHelper(final APISession apiSession) throws BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException {
        return new CustomPageAuthorizationsHelper(new GetUserRightsHelper(apiSession),
                TenantAPIAccessor.getLivingApplicationAPI(apiSession), TenantAPIAccessor.getCustomPageAPI(apiSession));
    }
}
