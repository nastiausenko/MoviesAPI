package dev.nastiausenko.movies.category;

import dev.nastiausenko.movies.movie.Movie;
import dev.nastiausenko.movies.movie.MovieRepository;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {
    private CategoryRepository categoryRepository;
    private MovieRepository movieRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getById(ObjectId id) {
        return categoryRepository.findById(id);
    }

    public Category create(String name, List<String> movieTitles) {
        List<Movie> movies = null;

        if (movieTitles != null && !movieTitles.isEmpty()) {
            movies = movieTitles.stream()
                    .map(movieRepository::findByTitle)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

        Category category = new Category();
        category.setName(name);
        category.setMovies(movies);

        return categoryRepository.save(category);
    }

    public Category addMovie(ObjectId id, List<String> movieTitles) {
        List<Movie> movies = movieTitles.stream()
                .map(movieRepository::findByTitle)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

        List<Movie> currentMovies = category.getMovies();
        currentMovies.addAll(movies);
        category.setMovies(currentMovies);

        return categoryRepository.save(category);
    }
}
