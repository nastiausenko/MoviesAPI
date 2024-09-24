package dev.nastiausenko.movies.category;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
//@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/V1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @SecurityRequirement(name = "JWT")
    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(@RequestParam String name, @RequestParam(required = false)List<String> movieTitles) {
        Category category = categoryService.create(name, movieTitles);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "JWT")
    @PostMapping("/add-movie/{id}")
    public ResponseEntity<Category> addMovie(@PathVariable ObjectId id, @RequestParam List<String> movieTitles) {
        Category category = categoryService.addMovie(id, movieTitles);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return new ResponseEntity<>(categoryService.getAll(), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable("id") ObjectId id) {
        Category category = categoryService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new ResponseEntity<>(category, HttpStatus.OK);
    }
}
