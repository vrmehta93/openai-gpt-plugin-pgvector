package com.yugaplus.backend.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yugaplus.backend.api.MovieResponse;
import com.yugaplus.backend.api.Status;
import com.yugaplus.backend.model.Movie;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
    private JdbcClient jdbcClient;

    private EmbeddingModel embeddingModel;
//     private ChatModel chatModel;

    public MovieController(
            @Autowired EmbeddingModel embeddingModel,
        //     @Autowired ChatModel chatModel,
            @Autowired JdbcClient jdbcClient) {

        this.embeddingModel = embeddingModel;
        // this.chatModel = chatModel;
        this.jdbcClient = jdbcClient;

    }

    @GetMapping("/{id}")
    public MovieResponse getMovieById(@PathVariable Integer id) {
        Movie movie = jdbcClient.sql(
                "SELECT * FROM movie WHERE id = ?")
                .param(id).query(Movie.class).single();

        return new MovieResponse(new Status(true, HttpServletResponse.SC_OK), List.of(movie));
    }
/*
 *      EMBEDDED
 */
    @GetMapping("/search")
    public MovieResponse searchMovies(
            @RequestParam("prompt") String prompt,
            @RequestParam(name = "rank", required = false) Integer rank,
            @RequestParam(name = "category", required = false) String category) {
        
        System.out.println("Starting embedded search");
        float[] embedding = embeddingModel.embed(prompt);       // Network call to OpenAI where it runs it embedded model. Response time will be faster
        
        // "<=>" is the cosine distance. The smaller the angle, the more relevant a movie is to the provided user prompt
        List<Movie> movies = jdbcClient.sql(
                "SELECT id, title, overview, vote_average, release_date FROM movie"
                        + " WHERE 1 - (overview_vector <=> :prompt_vector::vector) > 0.7"
                        + " ORDER BY overview_vector <=> :prompt_vector::vector"
                        + " LIMIT 3")
                .param("prompt_vector", embedding)      // To avoid SQL injection
                .query(Movie.class).list();
        System.out.println("Movies found: ");
        System.out.println(movies.toString());
        return new MovieResponse(
                new Status(true, HttpServletResponse.SC_OK),
                movies);
    }

/*
 *      CHAT SEARCH
 */
//     @GetMapping("/search")
//     public MovieResponse searchMovies(
//             @RequestParam("prompt") String prompt,
//             @RequestParam(name = "rank", required = false) Integer rank,
//             @RequestParam(name = "category", required = false) String category) {
        
//         System.out.println("Starting chat search");
        
//         // Hardest part
//         String systemMessage = """
//                         You are a world-famous movie critic who can easily come up with movie recommendations
//                         even for the most demanding users.

//                         Suggest at least three movies based on the user's preferences. Each movie MUST have
//                         the title, overview, vote average, and release date.
//                         Return the release date as a string in the format "yyyy-MM-dd".

//                         Don't add anything else to the response. Just the movies.
//                         """;
        
//         ChatClient chatClient = ChatClient.builder(chatModel)
//                 .defaultSystem(systemMessage).build();
        
//         List<Movie> movies = chatClient.prompt().user(prompt)
//                 .call() // Call the model
//                 .entity(new ParameterizedTypeReference<List<Movie>>() {
                        
//                 });   // Get response as an entity
        
//         return new MovieResponse(
//                 new Status(true, HttpServletResponse.SC_OK),
//                 movies);
//     }
}
