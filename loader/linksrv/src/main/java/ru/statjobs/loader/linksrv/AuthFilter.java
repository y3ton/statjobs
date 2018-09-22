package ru.statjobs.loader.linksrv;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class AuthFilter implements Filter {

    private volatile String secKey;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        secKey = (String) filterConfig.getServletContext().getAttribute(App.AUTH);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Enumeration enumeration = ((HttpServletRequest)request).getHeaders(HttpHeader.AUTHORIZATION.asString());
        if (StringUtils.isBlank(secKey) || (enumeration.hasMoreElements() && secKey.equals(enumeration.nextElement()))) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendError(HttpStatus.FORBIDDEN.value());
        }
    }

    @Override
    public void destroy() {

    }
}