package com.example.backend_spring.controller;

import com.example.backend_spring.model.Item;
import com.example.backend_spring.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Get all items
     * @return list of items
     */
    @GetMapping
    public List<Item> getItems() {
        return itemService.getAll(); // just return all the items
    }

    /**
     * Create a new item
     * @param item
     * @return respone status and created item
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        return ResponseEntity.ok(itemService.create(item)); // create the item and return it
    }
}
