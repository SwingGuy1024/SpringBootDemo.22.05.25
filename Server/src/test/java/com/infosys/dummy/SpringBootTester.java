package com.infosys.dummy;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>This Spring Boot application exists because other modules need
 * access to it for their unit tests. Maven packaging needs demand that I put it in the Server module, which
 * depends on all the other modules, so is the last module to get built, presumably for packaging reasons. This
 * is not the main application, which is in the Root module.</p>
 *
 * <p>Caching Configuration Reference:
 * https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#why-spring-redis</p>
 *
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 3/2/21
 * <p>Time: 11:43 PM
 *
 * @author Miguel Mu\u00f1oz
 */
//@SuppressWarnings({"CallToNumericToString", "HardCodedStringLiteral", "MagicNumber", "RedundantSuppression"})
////@RunWith(SpringRunner.class)
//@ComponentScan(basePackages = {
//        "com.infosys.dummy",
//        "org.openapitools",
//})
//@SpringBootTest(classes = ServerMaster.class)
//@SpringBootApplication
public class SpringBootTester implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SpringBootTester.class);
    public static void main(String[] args) {
        log.info("Launching From SpringBootTester");
        new SpringApplication(SpringBootTester.class).run(args);
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

}
