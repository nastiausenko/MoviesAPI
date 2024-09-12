package dev.nastiausenko.movies.admin;

import dev.nastiausenko.movies.movie.Movie;
import dev.nastiausenko.movies.movie.MovieRepository;
import dev.nastiausenko.movies.user.User;
import dev.nastiausenko.movies.user.UserRepository;
import dev.nastiausenko.movies.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public void grantAdmin(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        if (!user.getRoles().contains("ADMIN")) {
            user.getRoles().add("ADMIN");
            userRepository.save(user);
        } else {
            //TODO exception
            throw new RuntimeException("User already has admin rights");
        }
    }

    public void revokeAdmin(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        if (user.getRoles().contains("ADMIN")) {
            user.getRoles().remove("ADMIN");
            userRepository.save(user);
        } else {
            //TODO exception
            throw new RuntimeException("User doesn't have admin rights");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Movie addMovie(Movie movie) {
        Movie newMovie =  Movie.builder()
                .imdbId(movie.getImdbId())
                .title(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .trailerLink(movie.getTrailerLink())
                .poster(movie.getPoster())
                .genres(movie.getGenres())
                .backdrops(movie.getBackdrops())
                .build();
        movieRepository.save(newMovie);
        return newMovie;
    }

    public Movie editMovie(String imdbId, Movie movie) {
        Movie existingMovie = movieRepository.findByImdbId(imdbId).orElseThrow(() -> new RuntimeException("Movie not found"));
        existingMovie.setTitle(movie.getTitle());
        existingMovie.setReleaseDate(movie.getReleaseDate());
        existingMovie.setTrailerLink(movie.getTrailerLink());
        existingMovie.setPoster(movie.getPoster());
        existingMovie.setGenres(movie.getGenres());
        existingMovie.setBackdrops(movie.getBackdrops());
        movieRepository.save(existingMovie);
        return existingMovie;
    }

    public void deleteMovie(String imdbId) {
        Movie movie = movieRepository.findByImdbId(imdbId).orElseThrow(() -> new RuntimeException("Movie not found"));
        movieRepository.delete(movie);
    }

    public void blockUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        if (user.isAccountNonLocked()) {
            user.setBlocked(true);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User is already blocked");
        }
    }

    public void unblockUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        if (!user.isAccountNonLocked()) {
            user.setBlocked(false);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User is already unblocked");
        }
    }
}
