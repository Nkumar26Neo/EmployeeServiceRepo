package com.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.EmployeeController;
import org.example.model.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

/**
 * Unit integration tests verifying web tier routing behavior using JUnit 5 and Spring MockMvc.
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/employees should return all initial records")
    void testGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name", is("Sarah Connor")))
                .andExpect(jsonPath("$[0].department", is("Engineering")));
    }

    @Test
    @DisplayName("GET /api/employees/{id} should return employee details when ID exists")
    void testGetEmployeeByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/employees/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.name", is("Sarah Connor")))
                .andExpect(jsonPath("$.role", is("Lead Cybersecurity Engineer")));
    }

    @Test
    @DisplayName("GET /api/employees/{id} should return 404 status when ID is missing")
    void testGetEmployeeByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("EM-404")))
                .andExpect(jsonPath("$.message", is("Employee with ID 999 does not exist.")));
    }

    @Test
    @DisplayName("POST /api/employees should create employee record and return status 201")
    void testCreateEmployeeSuccess() throws Exception {
        Employee newEmployee = new Employee(105, "T-800", "Physical Security Detail", "Operations", "terminator@skygrid.io", 95000, "ACTIVE");
        String jsonPayload = objectMapper.writeValueAsString(newEmployee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(105)))
                .andExpect(jsonPath("$.name", is("T-800")))
                .andExpect(jsonPath("$.department", is("Operations")));
    }
}