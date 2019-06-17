package dev.voras.framework.api.authentication.internal;

import java.io.IOException;
import java.security.Principal;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
		service=Filter.class,
		scope=ServiceScope.PROTOTYPE,
		property = {"osgi.http.whiteboard.filter.pattern=/*"},
		//		configurationPid= {"dev.voras"},
		//		configurationPolicy=ConfigurationPolicy.REQUIRE,
		name="Voras JWT Auth"
		)
public class JwtAuthFilter implements Filter {
	
	private final Log logger = LogFactory.getLog(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}
		
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;

		if ("/auth".equals(servletRequest.getServletPath())) {  // dont do this for the auth url
			chain.doFilter(request, response);
			return;
		}

		Principal principal = servletRequest.getUserPrincipal();
		if (principal != null) {  // already authenticated
			chain.doFilter(request, response);
			return;
		}

		String authorization = servletRequest.getHeader("Authorization");
		if (authorization == null) {
			chain.doFilter(request, response);
			return;
		}

		StringTokenizer st = new StringTokenizer(authorization);
		if (!st.hasMoreTokens()) {
			chain.doFilter(request, response);
			return;
		}

		String bearer = st.nextToken();
		if (!"bearer".equalsIgnoreCase(bearer)) {
			chain.doFilter(request, response);
			return;
		}

		if (!st.hasMoreTokens()) {
			chain.doFilter(request, response);
			return;
		}
		
		String sJwt = st.nextToken();
		// TODO validate JWT
		
//		RequestWrapper wrapper = new RequestWrapper(name.toLowerCase(), roles, servletRequest);
		chain.doFilter(servletRequest, servletResponse);
	}

//	private void invalidAuth(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
//		servletResponse.setStatus(401);
//		servletResponse.addHeader("WWW-Authenticate", "Bearer realm=\"Voras\"");  //*** Ability to set the realm
//		servletResponse.getWriter().write("Invalid authentication");
//		return;
//	}

	@Override
	public void destroy() {
	}

}
