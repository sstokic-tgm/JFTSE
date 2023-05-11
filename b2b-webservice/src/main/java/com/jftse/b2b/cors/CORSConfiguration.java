package com.jftse.b2b.cors;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class CORSConfiguration {

    @CrossOrigin(
            origins = "*",
            methods = {RequestMethod.GET},
            allowedHeaders = {"Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"},
            maxAge = 3600)
    @ResponseBody
    public void handleCorsConfiguration() {
    }
}
