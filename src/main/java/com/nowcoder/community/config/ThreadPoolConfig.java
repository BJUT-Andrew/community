package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author andrew
 * @create 2021-11-07 23:31
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
