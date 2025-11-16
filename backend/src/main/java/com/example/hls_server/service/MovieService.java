package com.example.hls_server.service;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.MovieCreateRequest;
import com.example.hls_server.dto.MovieDto;

import java.util.List;

public interface MovieService {
    BaseResponse<MovieDto> createMovie(MovieCreateRequest movie);
    BaseResponse<List<MovieDto>> getAllMovies();
    MovieDto getMovieById(Long id);
    List<MovieDto> getMoviesByGenreId(Long genreId);
    BaseResponse<List<MovieDto>> searchMovies(String keyword);
}
