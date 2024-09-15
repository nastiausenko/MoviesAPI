package dev.nastiausenko.movies.movie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository repository;

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
