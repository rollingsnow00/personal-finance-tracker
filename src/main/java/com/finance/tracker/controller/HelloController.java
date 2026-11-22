package com.finance.tracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
@Tag(name = "Lab 1 - REST Basics", description = "Small public endpoints used to verify the initial REST setup")
public class HelloController {
    @GetMapping
    @Operation(summary = "Say hello", description = "Public endpoint from the Lab 1 REST basics exercise")
    public ResponseEntity<String> sayHello() { return ResponseEntity.ok("Hello from Personal Finance Tracker!"); }

    @GetMapping("/{name}")
    @Operation(summary = "Say hello to a name")
    public ResponseEntity<String> sayHelloToName(@PathVariable String name) {
        return ResponseEntity.ok("Hello, " + name + "!");
    }

    @PostMapping
    @Operation(summary = "Echo a text message")
    public ResponseEntity<String> receiveMessage(@RequestBody String message) {
        return ResponseEntity.ok("You sent: " + message);
    }
}
