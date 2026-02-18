package com.example.springdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/coin-config")
    public String coinConfig() {
        return "coin-config";
    }

    @GetMapping("/coin-chain-config")
    public String coinChainConfig() {
        return "coin-chain-config";
    }

    @GetMapping("/blockchain-config")
    public String blockchainConfig() {
        return "blockchain-config";
    }
}
