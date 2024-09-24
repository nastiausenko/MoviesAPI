package dev.nastiausenko.movies.category;

import dev.nastiausenko.movies.movie.Movie;
import dev.nastiausenko.movies.movie.MovieRepository;
import dev.nastiausenko.movies.review.exception.ForbiddenException;
import dev.nastiausenko.movies.user.User;
import dev.nastiausenko.movies.user.UserRepository;
import dev.nastiausenko.movies.user.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {
    private final UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private MovieRepository movieRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getById(ObjectId id) {
        return categoryRepository.findById(id);
    }

    public Category create(String name, List<String> movieTitles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        ObjectId id = user.getId();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

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

        if (isAdmin) {
            category.setAdminCategory(true);
            category.setUserId(null);
            category.setPublicCategory(true);
        } else {
            category.setAdminCategory(false);
            category.setUserId(id);
            category.setPublicCategory(false);
        }

        return categoryRepository.save(category);
    }

    public Category addMovie(ObjectId id, List<String> movieTitles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        ObjectId userId = user.getId();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

        List<Movie> movies = movieTitles.stream()
                .map(movieRepository::findByTitle)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (category.isAdminCategory() && !isAdmin) {
            throw new ForbiddenException("Only admin can add movies to this category");
        } else if (category.getUserId() != userId) {
            throw new ForbiddenException("You can only add movies to your categories");
        } else {
            List<Movie> currentMovies = category.getMovies();
            currentMovies.addAll(movies);
            category.setMovies(currentMovies);
        }

        return categoryRepository.save(category);
    }
}
