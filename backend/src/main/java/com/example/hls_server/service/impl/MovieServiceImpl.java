package com.example.hls_server.service.impl;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.MovieCreateRequest;
import com.example.hls_server.dto.MovieDto;
import com.example.hls_server.entity.Genre;
import com.example.hls_server.entity.Movie;
import com.example.hls_server.repository.GenreRepository;
import com.example.hls_server.repository.MovieRepository;
import com.example.hls_server.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Override
    public BaseResponse<MovieDto> createMovie(MovieCreateRequest request) {
        if (movieRepository.existsByTitle(request.getTitle())) {
            return BaseResponse.<MovieDto>builder()
                    .success(false)
                    .message("Thêm mới thất bại")
                    .data(null)
                    .build();
        }

        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setYear(request.getYear());
        movie.setDurationMin(request.getDurationMin());
        movie.setRating(request.getRating());
        movie.setVideoUrl(request.getVideoUrl());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setBackdropUrl(request.getBackdropUrl());
        movie.setCreatedAt(LocalDateTime.now());

        // set genres
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
            movie.setGenres(new HashSet<>(genres));
        }

        Movie saved = movieRepository.save(movie);
        return BaseResponse.<MovieDto>builder()
                .success(true)
                .message("Thêm mới thành công")
                .data(toDto(saved))
                .build();
    }

    @Override
    public BaseResponse<List<MovieDto>> getAllMovies() {
        List<MovieDto> movieList = movieRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return BaseResponse.<List<MovieDto>>builder()
                .success(true)
                .message("Lấy dữ liệu thành công")
                .data(movieList)
                .build();
    }

    @Override
    public MovieDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return toDto(movie);
    }

    @Override
    public BaseResponse<List<MovieDto>> getMoviesByGenreId(Long genreId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        List<MovieDto> movieList = genre.getMovies()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return BaseResponse.<List<MovieDto>>builder()
                .success(true)
                .message("Lấy danh sách phim theo thể loại thành công")
                .data(movieList)
                .build();
    }


    @Override
    public BaseResponse<List<MovieDto>> searchMovies(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return BaseResponse.<List<MovieDto>>builder()
                    .success(false)
                    .message("Từ khóa tìm kiếm không được để trống")
                    .data(new ArrayList<>())
                    .build();
        }

        List<MovieDto> movieList = movieRepository.findByTitleContainingIgnoreCase(keyword.trim())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return BaseResponse.<List<MovieDto>>builder()
                .success(true)
                .message("Tìm kiếm thành công")
                .data(movieList)
                .build();
    }

    private MovieDto toDto(Movie movie) {
        // Tạo defensive copy của genres set để tránh ConcurrentModificationException
        List<String> genreNames = new ArrayList<>();
        if (movie.getGenres() != null) {
            for (Genre genre : movie.getGenres()) {
                if (genre != null && genre.getName() != null) {
                    genreNames.add(genre.getName());
                }
            }
        }

        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .year(movie.getYear())
                .durationMin(movie.getDurationMin())
                .rating(movie.getRating())
                .videoUrl(movie.getVideoUrl())
                .posterUrl(movie.getPosterUrl())
                .backdropUrl(movie.getBackdropUrl())
                .genres(genreNames)
                .build();
    }
}
