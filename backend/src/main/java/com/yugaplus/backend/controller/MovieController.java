package com.yugaplus.backend.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yugaplus.backend.api.MovieResponse;
import com.yugaplus.backend.api.Status;
import com.yugaplus.backend.model.Movie;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ai.embedding.EmbeddingModel;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
        private JdbcClient jdbcClient;

        private EmbeddingModel embeddingModel;

        public MovieController(
                        @Autowired(required = false) EmbeddingModel embeddingModel,
                        JdbcClient jdbcClient) {

                this.embeddingModel = embeddingModel;
                this.jdbcClient = jdbcClient;
        }

        @GetMapping("/{id}")
        public MovieResponse getMovieById(@PathVariable Integer id) {
                Movie movie = jdbcClient.sql(
                                "SELECT * FROM movie WHERE id = ?")
                                .param(id).query(Movie.class).single();

                return new MovieResponse(new Status(true, HttpServletResponse.SC_OK), List.of(movie));
        }

        @GetMapping("/search")
        public MovieResponse searchMovies(
                        @RequestParam("prompt") String prompt,
                        @RequestParam(name = "rank", required = false) Integer rank,
                        @RequestParam(name = "category", required = false) String category) {

                List<Double> embedding = embeddingModel.embed(prompt);

                List<Movie> movies = jdbcClient.sql(
                                "SELECT id, title, overview, vote_average, release_date FROM movie"
                                                + " WHERE 1 - (overview_vector <=> :prompt_vector::vector) > 0.7"
                                                + " ORDER BY overview_vector <=> :prompt_vector::vector"
                                                + " LIMIT 3")
                                .param("prompt_vector", embedding.toString())
                                .query(Movie.class).list();

                return new MovieResponse(
                                new Status(true, HttpServletResponse.SC_OK),
                                movies);
        }
}
