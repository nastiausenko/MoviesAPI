package dev.nastiausenko.movies.category;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
@RequestMapping("/api/v1/categories")
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
    @PutMapping("/change-name/{id}")
    public ResponseEntity<Category> changeName(@PathVariable("id") ObjectId id, @RequestParam String newName) {
        Category category = categoryService.changeName(id, newName);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @PutMapping("/remove-movie/{id}")
    public ResponseEntity<Category> removeMovie(@PathVariable("id") ObjectId id, @RequestParam ObjectId movieId) {
        Category category = categoryService.removeMovie(movieId, id);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @PutMapping("/visibility/{id}")
    public ResponseEntity<Category> visibility(@PathVariable("id") ObjectId id) {
        Category category = categoryService.changeVisibility(id);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/delete-category/{id}")
    public ResponseEntity<Category> deleteCategory(@PathVariable("id") ObjectId id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return new ResponseEntity<>(categoryService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable("id") ObjectId id) {
        Category category = categoryService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Category>> getPublicUserCategories() {
        List<Category> publicCategories = categoryService.getAllPublicUserCategories();
        return new ResponseEntity<>(publicCategories, HttpStatus.OK);
    }
}
