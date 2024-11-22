package org.daxue.games.controller;

import org.daxue.games.annotation.RequestLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestLimit(count = 10)
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
