package de.dsuckow;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleFilterConfiguration {

	@Bean
	public FilterRegistrationBean filterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(simpleRequestFilter());
		registration.addUrlPatterns("/*");
		registration.addInitParameter(SimpleRequestFilter.NUMBER_ALLOWED_REQUESTS, "5");
		registration.setName("simpleRequestFilter");
		registration.setOrder(1);
		return registration;
	}

	@Bean(name = "simpleRequestFilter")
	public Filter simpleRequestFilter() {
		return new SimpleRequestFilter();
	}
}
