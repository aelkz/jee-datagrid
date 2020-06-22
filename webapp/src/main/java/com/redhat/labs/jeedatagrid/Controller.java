package com.redhat.labs.jeedatagrid;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;

public class Controller {

    public boolean isLoggedIn(HttpServletRequest req) {
        return getSession(req) != null;
    }

    public void handleLogout(HttpServletRequest req) throws ServletException {
        if (isLogoutAction(req)) {
            req.logout();
        }
    }

    public boolean isLogoutAction(HttpServletRequest req) {
        return getAction(req).equals("logout");
    }

    public String getAccountUri(HttpServletRequest req) {
        KeycloakSecurityContext session = getSession(req);
        String baseUrl = getAuthServerBaseUrl(req);
        String realm = session.getRealm();
        return KeycloakUriBuilder.fromUri(baseUrl).path(ServiceUrlConstants.ACCOUNT_SERVICE_PATH)
                .queryParam("referrer", "app-jsp")
                .queryParam("referrer_uri", getReferrerUri(req)).build(realm).toString();
    }

    private String getReferrerUri(HttpServletRequest req) {
        StringBuffer uri = req.getRequestURL();
        String q = req.getQueryString();
        if (q != null) {
            uri.append("?").append(q);
        }
        return uri.toString();
    }

    private String getAuthServerBaseUrl(HttpServletRequest req) {
        AdapterDeploymentContext deploymentContext = (AdapterDeploymentContext) req.getServletContext().getAttribute(AdapterDeploymentContext.class.getName());
        KeycloakDeployment deployment = deploymentContext.resolveDeployment(null);
        return deployment.getAuthServerBaseUrl();
    }

    public String getMessage(HttpServletRequest req) {
        String action = getAction(req);
        if (action.equals("")) return "";
        if (isLogoutAction(req)) return "";

        try {
            return "Message: " + ServiceClient.callService(req, getSession(req), action);
        } catch (ServiceClient.Failure f) {
            return "<span class='error'>" + f.getStatus() + " " + f.getReason() + "</span>";
        }
    }

    private KeycloakSecurityContext getSession(HttpServletRequest req) {
        return (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
    }

    private String getAction(HttpServletRequest req) {
        if (req.getParameter("action") == null) return "";
        return req.getParameter("action");
    }
}
