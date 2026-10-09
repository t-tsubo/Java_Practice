package com.portfolio.qiita_summary_notifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;

@SpringBootApplication
@Retryable 
@EnableResilientMethods
public class QiitaSummaryNotifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(QiitaSummaryNotifierApplication.class, args);
	}

}
