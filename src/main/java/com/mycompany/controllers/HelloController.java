package main.java.com.mycompany.controllers;

import main.java.com.mycompany.annotations.*;

@RestController
public class HelloController {

    @GetMapping(mapping = "/hello")
    public String hello(Request req, Response res) {
        return "Hello from microSpringBoot";
    }

    @GetMapping(mapping = "/bye")
    public String bye(Request req, Response res) {
        return "Finalizado";
    }
}
