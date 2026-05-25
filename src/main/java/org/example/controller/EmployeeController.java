package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Employee;
import org.example.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST Controller serving employee operations using Spring Boot 3.x and JDK 25.
 * Features an internal memory store with hardcoded mock responses.
 */
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    // Thread-safe repository map for simulated persistence
    private final ConcurrentHashMap<Integer, Employee> employeeRepository = new ConcurrentHashMap<>();

    // Constructor initializing hardcoded mock data
    public EmployeeController() {
        initializeMockData();
    }

    private void initializeMockData() {
        logger.info("[JVM-25] Pre-populating employee records...");
        saveToMap(new Employee(101, "Sarah Connor", "Lead Cybersecurity Engineer", "Engineering", "sarah.connor@skygrid.io", 145000, "ACTIVE"));
        saveToMap(new Employee(102, "Marcus Wright", "Senior Systems Architect", "Infrastructure", "marcus.wright@skygrid.io", 132000, "ACTIVE"));
        saveToMap(new Employee(103, "John Connor", "Junior DevOps Engineer", "Operations", "john.connor@skygrid.io", 87000, "ACTIVE"));
        saveToMap(new Employee(104, "Catherine Weaver", "VP of Robotics & Automation", "Executive", "catherine.weaver@skygrid.io", 220000, "ON_LEAVE"));
        logger.info("[JVM-25] Initialized microservice with {} employee records successfully.", employeeRepository.size());
    }

    private void saveToMap(Employee employee) {
        employeeRepository.put(employee.getId(), employee);
    }

    /**
     * Fetch all employees optionally filtered by status or department.
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dept) {

        logger.info("GET /api/employees triggered with parameters [status={}, dept={}]", status, dept);

        List<Employee> results = new ArrayList<>(employeeRepository.values());

        if (status != null && !status.isBlank()) {
            results = results.stream()
                    .filter(e -> e.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        if (dept != null && !dept.isBlank()) {
            results = results.stream()
                    .filter(e -> e.getDepartment().equalsIgnoreCase(dept))
                    .toList();
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Retrieve a specific employee by ID, demonstrating pattern matching.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer id) {
        logger.info("GET /api/employees/{} triggered on thread: {}", id, Thread.currentThread().getName());

        Employee employee = employeeRepository.get(id);

        // Java Switch pattern-matching simulation (Modern JDK 21+ idiom)
        return switch (employee) {
            case null -> {
                logger.warn("Employee ID {} requested but not found", id);
                yield ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("EM-404", "Employee with ID " + id + " does not exist."));
            }
            case Employee emp -> ResponseEntity.ok(emp);
        };
    }

    /**
     * Register a new employee with valid parameters.
     */
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        logger.info("POST /api/employees saving record: {}", employee.getName());

        if (employeeRepository.containsKey(employee.getId())) {
            throw new IllegalArgumentException("Employee ID " + employee.getId() + " already registered");
        }

        saveToMap(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    /**
     * Remove employee from internal store.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        logger.info("DELETE /api/employees/{} triggered", id);

        if (!employeeRepository.containsKey(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        employeeRepository.remove(id);
        return ResponseEntity.noContent().build();
    }
}