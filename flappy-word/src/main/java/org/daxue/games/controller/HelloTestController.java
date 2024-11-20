package org.daxue.games.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/hello")
public class HelloTestController {

    @GetMapping
    public Map hello() {
        return Map.of(
        "msg", "hello"
        );
    }

}
