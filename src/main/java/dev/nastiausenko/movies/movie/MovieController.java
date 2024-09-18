package dev.nastiausenko.movies.movie;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
@RequestMapping("/api/V1/movies")
public class MovieController {
    private final MovieService service;

    @GetMapping
    public ResponseEntity<List<Movie>> allMovies() {
        return new ResponseEntity<>(service.allMovies(), HttpStatus.OK);
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<Optional<Movie>> findMovieById(@PathVariable String imdbId) {
        return new ResponseEntity<>(service.findMovieByImdbId(imdbId), HttpStatus.OK);
    }

    @GetMapping("/genres")
    public ResponseEntity<List<Movie>> findMovieByGenre(@RequestParam List<String> genres) {
        return new ResponseEntity<>(service.findMovieByGenres(genres), HttpStatus.OK);
    }
}
