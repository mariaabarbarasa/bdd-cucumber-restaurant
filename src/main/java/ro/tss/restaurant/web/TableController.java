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
import org.springframework.web.bind.annotation.RestController;
import ro.tss.restaurant.dto.TableRequest;
import ro.tss.restaurant.dto.TableResponse;
import ro.tss.restaurant.service.TableService;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService service;

    public TableController(TableService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TableResponse> create(@Valid @RequestBody TableRequest request) {
        TableResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/tables/" + created.id())).body(created);
    }

    @GetMapping
    public List<TableResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TableResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public TableResponse update(@PathVariable Long id, @Valid @RequestBody TableRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
