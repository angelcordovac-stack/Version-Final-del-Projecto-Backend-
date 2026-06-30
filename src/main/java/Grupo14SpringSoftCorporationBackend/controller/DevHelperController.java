package Grupo14SpringSoftCorporationBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dev")
@ConditionalOnProperty(name = "app.dev-helper.enabled", havingValue = "true")
public class DevHelperController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/hash")
    public Map<String, String> hash(@RequestParam String password) {
        return Map.of("hash", passwordEncoder.encode(password));
    }
}
