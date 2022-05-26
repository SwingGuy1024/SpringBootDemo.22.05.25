package com.infosys.dummy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 *
 */
    @ComponentScan(basePackages = {
        "com.infosys.dummy",
        "org.openapitools",
        "configuration"
    })
    @EnableCaching
    @SpringBootApplication
    public class ServerMaster implements CommandLineRunner {
      private static final Logger log = LoggerFactory.getLogger(ServerMaster.class);
      public static void main(String[] args) {
        log.info("Launching From ServerMaster");
        new SpringApplication(ServerMaster.class).run(args);
      }

      @Override
      public void run(String... arg0) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("Thread {}: {}", t.getName(), e.getLocalizedMessage(), e));
        if ((arg0.length > 0) && "exitcode".equals(arg0[0])) { //NON-NLS
          throw new ExitException();
        }
      }

      static class ExitException extends RuntimeException implements ExitCodeGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public int getExitCode() {
          return 10;
        }

      }

      // This was suggested at https://stackoverflow.com/questions/49538896/spring-boot-error-message-doesnt-work
      // in order to give me better error messages when OpenAPI validations are triggered, but it doesn't work.
      @Bean
      public Validator validator() {
        return new LocalValidatorFactoryBean();
      }
    }
