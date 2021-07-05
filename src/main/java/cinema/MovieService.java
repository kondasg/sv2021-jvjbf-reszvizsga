package cinema;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private List<Movie> movies = new ArrayList<>();
    private ModelMapper modelMapper;
    private AtomicLong idGenerator = new AtomicLong();

    public MovieService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public MovieDTO addMovie(CreateMovieCommand command) {
        Movie movie = new Movie(
                idGenerator.incrementAndGet(),
                command.getTitle(),
                command.getDate(),
                command.getMaxSpaces());
        movies.add(movie);
        return modelMapper.map(movie, MovieDTO.class);
    }

    public void deleteAllMovies() {
        movies.clear();
        idGenerator = new AtomicLong();
    }

    public List<MovieDTO> getMovies(Optional<String> title) {
        Type targetListType = new TypeToken<List<MovieDTO>>() {}.getType();
        List<Movie> filtered = movies.stream()
                .filter(m -> title.isEmpty() || m.getTitle().equalsIgnoreCase(title.get()))
                .collect(Collectors.toList());
        return modelMapper.map(filtered, targetListType);
    }

    public MovieDTO getMovieById(long id) {
        return modelMapper.map(getMovie(id), MovieDTO.class);
    }

    public MovieDTO updateMovie(long id, UpdateDateCommand command) {
        Movie movie = getMovie(id);
        movie.setDate(command.getDate());
        return modelMapper.map(movie, MovieDTO.class);
    }

    public MovieDTO reserveMovie(long id, CreateReservationCommand command) {
        Movie movie = getMovie(id);
        movie.reservation(command.getSpaces());
        return modelMapper.map(movie, MovieDTO.class);
    }

    private Movie getMovie(long id) {
        return movies.stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElseThrow(() -> new MovieNotFoundException("Movie id not fond: " + id));
    }
}
