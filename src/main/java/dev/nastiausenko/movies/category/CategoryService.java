package dev.nastiausenko.movies.category;

import dev.nastiausenko.movies.category.exception.CategoryNotFoundException;
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
    private final CategoryRepository categoryRepository;
    private final MovieRepository movieRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll().stream()
                .filter(Category::isPublicCategory)
                .toList();
    }

    public List<Category> getAllPublicUserCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.isPublicCategory() && !category.isAdminCategory())
                .toList();
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

        Category savedCategory = categoryRepository.save(category);

        if (!isAdmin) {
            user.getCategories().add(savedCategory);
            userRepository.save(user);
        }
        return savedCategory;
    }

    public Category addMovie(ObjectId categoryId, List<String> movieTitles) {
        User user = getAuthenticatedUser();
        boolean isAdmin = isAdminUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        checkAccessRights(category, user, isAdmin);

        List<Movie> moviesToAdd = getMoviesByTitles(movieTitles);
        category.getMovies().addAll(moviesToAdd);

        return categoryRepository.save(category);
    }

    public Category changeName(ObjectId categoryId, String newName) {
        User user = getAuthenticatedUser();
        boolean isAdmin = isAdminUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        checkAccessRights(category, user, isAdmin);
        category.setName(newName);
        return categoryRepository.save(category);
    }

    public void deleteCategory(ObjectId categoryId) {
        User user = getAuthenticatedUser();
        boolean isAdmin = isAdminUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        checkAccessRights(category, user, isAdmin);
        categoryRepository.delete(category);

        if (!isAdmin) {
            user.getCategories().remove(category);
            userRepository.save(user);
        }
    }

    public Category removeMovie(ObjectId movieId, ObjectId categoryId) {
        User user = getAuthenticatedUser();
        boolean isAdmin = isAdminUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        checkAccessRights(category, user, isAdmin);

        List<Movie> movies = category.getMovies();
        Movie movieToRemove = movies.stream()
                .filter(movie -> movie.getId().equals(movieId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Movie not found in category"));

        movies.remove(movieToRemove);

        category.setMovies(movies);
        return categoryRepository.save(category);
    }

    public Category changeVisibility(ObjectId categoryId) {
        User user = getAuthenticatedUser();
        boolean isAdmin = isAdminUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        checkAccessRights(category, user, isAdmin);

        category.setPublicCategory(!category.isPublicCategory());

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
