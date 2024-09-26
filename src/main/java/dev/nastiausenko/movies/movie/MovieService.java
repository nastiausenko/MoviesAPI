package dev.nastiausenko.movies.movie;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MovieService {
    private final MovieRepository repository;

    public List<Movie> allMovies() {
        return repository.findAll();
    }

    public Optional<Movie> findMovieByImdbId(String imdbId) {
        return repository.findByImdbId(imdbId);
    }

    public List<Movie> findMovieByGenres(List<String> genres) {
        return repository.findByGenresIn(genres);
    }
}
