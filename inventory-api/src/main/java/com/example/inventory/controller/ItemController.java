package com.example.inventory.controller;

import com.example.inventory.model.Item;
import com.example.inventory.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository repository;

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
}
