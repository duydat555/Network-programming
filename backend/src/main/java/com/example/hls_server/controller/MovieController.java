package com.example.hls_server.controller;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.MovieCreateRequest;
import com.example.hls_server.dto.MovieDto;
import com.example.hls_server.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public BaseResponse<MovieDto> createMovie(@RequestBody MovieCreateRequest request) {
        return movieService.createMovie(request);
    }

    @GetMapping
    public BaseResponse<List<MovieDto>> getAll() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    public MovieDto getById(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    @GetMapping("/by-genre/{genreId}")
    public List<MovieDto> getByGenre(@PathVariable Long genreId) {
        return movieService.getMoviesByGenreId(genreId);
    }
}
