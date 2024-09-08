package com.infosys.dummy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>This Spring Boot application exists because other modules need
 * access to it for their unit tests. Maven packaging needs demand that I put it in the Server module, which
 * depends on all the other modules, so is the last module to get built, presumably for packaging reasons. This
 * is not the main application, which is in the Root module.</p>
 *
 * <p>Caching Configuration Reference:<br>
 * @see <a href="https://docs.spring.io/spring-data-redis/docs/1.1.1.RELEASE/reference/html/why-spring-redis.html">
 *   Why Spring Data Redis?</a></p>
 *
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 3/2/21
 * <p>Time: 11:43 PM
 *
 * @author Miguel Muñoz
 */
@ComponentScan(basePackages = {
    "org.openapitools",
    "com.infosys.dummy.api",
    "com.infosys.dummy.server",
    "com.infosys.dummy.framework.util",
    "com.infosys.dummy.framework.engine",
})
@SpringBootApplication
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
