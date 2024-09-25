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
        User user = getAuthenticatedUser();
        ObjectId id = user.getId();
        boolean isAdmin = isAdminUser();

        List<Movie> movies = getMoviesByTitles(movieTitles);

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

    public Category addMovie(ObjectId categoryId, List<String> movieTitles) {
        User user = getAuthenticatedUser();
        boolean isAdmin = isAdminUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        checkAccessRights(category, user, isAdmin);

        List<Movie> moviesToAdd = getMoviesByTitles(movieTitles);
        category.getMovies().addAll(moviesToAdd);

        return categoryRepository.save(category);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }

    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));
    }

    private List<Movie> getMoviesByTitles(List<String> movieTitles) {
        if (movieTitles == null || movieTitles.isEmpty()) {
            return List.of();
        }

        return movieTitles.stream()
                .map(movieRepository::findByTitle)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void checkAccessRights(Category category, User user, boolean isAdmin) {
        if (category.isAdminCategory() && !isAdmin) {
            throw new ForbiddenException("Only admin can modify this category");
        }

        if (!category.isAdminCategory() && !category.getUserId().equals(user.getId())) {
            throw new ForbiddenException("You can only modify your own categories");
        }
    }
}
