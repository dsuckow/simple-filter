package de.dsuckow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class SimpleRequestFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleRequestFilter.class);

	public static final String NUMBER_ALLOWED_REQUESTS = "numberAllowedRequests";
	public static final String COOKIE_NAME = "pageToken";
	private int initial;
	private Map<String, AtomicInteger> requestCounterMap = new HashMap<>();

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			final Cookie cookie = getCookie(httpRequest);
			if (cookie != null) {
				final String uuid = cookie.getValue();
				final AtomicInteger counter = requestCounterMap.get(uuid);
				if (counter.getAndIncrement() < 10) {
					LOG.debug("before filterchain counter = "+counter.get());
					chain.doFilter(request, response); // proceed
					LOG.debug("after filterchain counter = "+counter.get());
					counter.decrementAndGet();
				} else {
					httpResponse.sendError(429);
				}
			} else {
				setCookie(httpResponse);
			}
		}
		chain.doFilter(request, response);
	}

	private void setCookie(final HttpServletResponse httpResponse) {
		final String newuuid = UUID.randomUUID().toString();
		requestCounterMap.put(newuuid, new AtomicInteger(0));
		Cookie c = new Cookie(COOKIE_NAME, newuuid.toString());
		c.setMaxAge(60); // Sekunden
		httpResponse.addCookie(c);
		LOG.debug("set new cookie [" + newuuid + "] with value = " + initial);
	}

	private Cookie getCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (COOKIE_NAME.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		return null;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		try {
			initial = Integer.parseInt(arg0.getInitParameter(NUMBER_ALLOWED_REQUESTS));
		} catch (NumberFormatException e) {
			LOG.error("NumberFormatException [" + e.getMessage() + "] use default instead!");
			initial = 1;
		}
	}
}
