package com.example.inventory.controller;

import com.example.inventory.model.Item;
import com.example.inventory.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository repository;

    // VULNERABILITY: Reads the hardcoded secret from application.properties
    // and exposes it through the /debug endpoint below (CWE-200).
    @Value("${api.key}")
    private String apiKey;

    // VULNERABILITY: Hardcoded fake/dummy credentials in source code (CWE-798)
    // Vault Radar detects these patterns directly in .java files.
    private static final String AWS_ACCESS_KEY    = "AKIAIOSFODNN7EXAMPLE";
    private static final String AWS_SECRET_KEY    = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    private static final String GITHUB_TOKEN      = "ghp_aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890";
    private static final String STRIPE_SECRET_KEY = "sk_live_4a7f2c91d83e0b56f1a2c3d4e5f60718";

    public ItemController(ItemRepository repository) {
        this.repository = repository;
    }

    // GET /api/items           → all items
    // GET /api/items?name=foo  → filtered by name (SQL-injection vector)
    @GetMapping
    public List<Item> list(@RequestParam(required = false) String name) {
        if (name != null && !name.isEmpty()) {
            return repository.findByName(name);
        }
        return repository.findAll();
    }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Item> get(@PathVariable Long id) {
        Item item = repository.findById(id);
        return item != null
                ? ResponseEntity.ok(item)
                : ResponseEntity.notFound().build();
    }

    // POST /api/items
    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        Item created = repository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id,
                                       @RequestBody Item item) {
        if (repository.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repository.update(id, item));
    }

    // DELETE /api/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // VULNERABILITY: Sensitive Data Exposure (CWE-200)
    // GET /api/items/debug → returns runtime config including the live API key
    // in plain text. No authentication required.
    // -------------------------------------------------------------------------
    @GetMapping("/debug")
    public ResponseEntity<Map<String, String>> debug() {
        Map<String, String> info = new HashMap<>();
        info.put("status", "ok");
        info.put("api.key", apiKey);                          // leaks the secret
        info.put("aws.access.key", AWS_ACCESS_KEY);
        info.put("aws.secret.key", AWS_SECRET_KEY);
        info.put("github.token", GITHUB_TOKEN);
        info.put("stripe.key", STRIPE_SECRET_KEY);
        info.put("java.version", System.getProperty("java.version"));
        info.put("user.home", System.getProperty("user.home"));
        info.put("os.name", System.getProperty("os.name"));
        return ResponseEntity.ok(info);
    }

    // -------------------------------------------------------------------------
    // VULNERABILITY: Insecure Deserialization (CWE-502)
    // POST /api/items/import  body: { "data": "<base64 serialized object>" }
    // Deserializes untrusted user-supplied bytes with no validation — allows
    // remote code execution via a crafted gadget chain.
    // -------------------------------------------------------------------------
    @PostMapping("/import")
    public ResponseEntity<String> importItem(@RequestBody Map<String, String> payload) {
        try {
            byte[] bytes = Base64.getDecoder().decode(payload.get("data"));
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object obj = ois.readObject();                    // unsafe deserialization
            ois.close();
            return ResponseEntity.ok("Imported: " + obj.getClass().getSimpleName());
        } catch (Exception e) {
            return ResponseEntity.ok("Import failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // VULNERABILITY: Path Traversal (CWE-22)
    // GET /api/items/files?name=../../etc/passwd
    // Reads arbitrary files from the filesystem — no path sanitisation.
    // -------------------------------------------------------------------------
    @GetMapping("/files")
    public ResponseEntity<String> readFile(@RequestParam String name) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("/deployments/" + name)));
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + name);
        }
    }
}
