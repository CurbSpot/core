package com.foodtruckopt.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.foodtruckopt.repositories")
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.foodtruckopt.dto"})
public class RepositoryConfig {
}
