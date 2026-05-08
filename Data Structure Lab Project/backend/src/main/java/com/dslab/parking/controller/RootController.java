package com.dslab.parking.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;

/**
 * Serve the SPA entry point at "/" so users can hit http://localhost:3000.
 * Spring's static resource handler will serve everything else under
 * /assets/* automatically because of the static-locations setting in
 * application.properties.
 */
@Controller
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Resource> index() {
        File f = new File("../frontend/index.html");
        if (!f.exists()) {
            f = new File("frontend/index.html"); // fallback when running from project root
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new FileSystemResource(f));
    }
}
