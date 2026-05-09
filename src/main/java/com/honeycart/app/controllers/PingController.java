package com.honeycart.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    /**
     * This Class is defined because, I Deployed the application
     * on Render's free tier which puts the inative application to sleep mode after 15 minutes of
     * un-use, So to keep it alive I defined this class and using cron-job platform
     * by giving this class url which does not require any authentication....
     */

    @GetMapping("/")
    public String ping() {
        return "Backend Awake....!!!";
    }

}