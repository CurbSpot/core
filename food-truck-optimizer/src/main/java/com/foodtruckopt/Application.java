package com.foodtruckopt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

import com.foodtruckopt.config.AppConfig;
import com.foodtruckopt.server.ServletContainerCustomizer;

@Configuration
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) throws Exception {
    	WebDbInitializer wi = new WebDbInitializer();
    	wi.contextInitialized();
    	//, ServletContainerCustomizer.class
        SpringApplication.run(new Object[]{AppConfig.class}, args);
    }
    
	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		return multipartResolver;
	}

	@Bean
    public FilterRegistrationBean multipartFilterRegistrationBean() {
        final MultipartFilter multipartFilter = new MultipartFilter();
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(multipartFilter);
        filterRegistrationBean.addInitParameter("multipartResolverBeanName", "commonsMultipartResolver");
        return filterRegistrationBean;
    }	
}
