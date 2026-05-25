package org.example;

import org.example.controller.EmployeeController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EmployeeServiceApplication.class)
public class EmployeeServiceApplicationTest {

    @Autowired
    private EmployeeController employeeController;

    @Test
    void contextLoad(){
        Assertions.assertNotNull(employeeController);
    }
}
