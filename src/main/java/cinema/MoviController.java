package cinema;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/cinema")
public class MoviController {

    private MovieService movieService;

    public MoviController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public MovieDTO addMovie(@Valid @RequestBody CreateMovieCommand command) {
        return movieService.addMovie(command);
    }

    @DeleteMapping
    public void deleteAllMovies() {
        movieService.deleteAllMovies();
    }

    @GetMapping
    public List<MovieDTO> getMovies(@RequestParam(name = "title")Optional<String> title) {
        return movieService.getMovies(title);
    }

    @GetMapping("/{id}")
    public MovieDTO getMovieById(@PathVariable("id") long id) {
        return movieService.getMovieById(id);
    }

    @PutMapping("/{id}")
    public MovieDTO updateMovie(@PathVariable("id") long id, @RequestBody UpdateDateCommand command) {
        return movieService.updateMovie(id, command);
    }

    @PostMapping("/{id}/reserve")
    public MovieDTO reserveMovie(@PathVariable("id") long id, @RequestBody CreateReservationCommand command) {
        return movieService.reserveMovie(id, command);
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(MovieNotFoundException le) {
        Problem problem = Problem.builder()
                .withType(URI.create("cinema/not-found"))
                .withTitle("Not found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(le.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
     public ResponseEntity<Problem> handleNotFound(IllegalStateException le) {
        Problem problem = Problem.builder()
                .withType(URI.create("cinema/bad-reservation"))
                .withTitle("Not found")
                .withStatus(Status.BAD_REQUEST)
                .withDetail(le.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidExpection(MethodArgumentNotValidException ex) {
        List<Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new Violation(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        Problem problem = Problem.builder()
                .withType(URI.create("cinema/not-valid"))
                .withTitle("Validation error")
                .withStatus(Status.BAD_REQUEST)
                .withDetail(ex.getMessage())
                .with("violations", violations)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
