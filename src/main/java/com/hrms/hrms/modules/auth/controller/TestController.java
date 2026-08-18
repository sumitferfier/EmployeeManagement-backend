package com.hrms.hrms.modules.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/v1/test")
    public String test(Authentication authentication) {
        return "Hello " + authentication.getName() + ", you are authenticated!";
    }

    @GetMapping("/api/v1/admin-test")
    public String adminTest(Authentication authentication) {
        return "Hello Admin " + authentication.getName() + ", you have ADMIN access!";
    }

    @GetMapping("/api/v1/employee-test")
    public String employeeTest(Authentication authentication) {

        return "Hello Employee " + authentication.getName() + ", you have EMPLOYEE access!";
    }
}