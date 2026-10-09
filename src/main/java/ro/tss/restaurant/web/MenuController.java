package ro.tss.restaurant.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.tss.restaurant.dto.MenuItemRequest;
import ro.tss.restaurant.dto.MenuItemResponse;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.service.MenuService;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService service;

    public MenuController(MenuService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/menu/" + created.id())).body(created);
    }

    @GetMapping
    public List<MenuItemResponse> findAll(@RequestParam(required = false) Category category) {
        return service.findAll(category);
    }

    @GetMapping("/{id}")
    public MenuItemResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
