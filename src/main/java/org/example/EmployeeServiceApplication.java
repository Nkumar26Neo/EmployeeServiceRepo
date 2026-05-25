package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

 /**
 * Spring Boot Application Bootstrap Class.
 * Launches embedded Apache Tomcat on port 8080 by default.
 */
@SpringBootApplication
public class EmployeeServiceApplication {

     public static void main(String[] args) {
        SpringApplication.run(EmployeeServiceApplication.class, args);
    }
}
