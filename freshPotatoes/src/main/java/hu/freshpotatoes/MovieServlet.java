package hu.freshpotatoes;

import hu.freshpotatoes.dao.MovieDao;
import hu.freshpotatoes.dao.impl.MovieDaoImpl;
import hu.freshpotatoes.model.Movie;
import hu.freshpotatoes.controller.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// This annotation maps the servlet to the URL http://localhost:8080/your_app/movies
@WebServlet("/movies")
public class MovieServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Tell the browser we are sending back HTML
        response.setContentType("text/html;charset=UTF-8");

        // 2. Open the writer to send the HTML output
        try (PrintWriter out = response.getWriter()) {

            // 3. Print the top half of the HTML page
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Fresh Potatoes Movies</title></head>");
            out.println("<body>");
            out.println("<h1>Available Movies</h1>");
            out.println("<ul>");

            // 4. Run your database logic
            try {
                DataSource dataSource = DatabaseConnection.getDataSource();
                MovieDao movieDao = new MovieDaoImpl(dataSource);

                List<Movie> movies = movieDao.findAll();

                // 5. Loop through the movies and print them as HTML list items
                for (Movie movie : movies) {
                    out.println("<li>" +
                            movie.getId() + " " +
                            movie.getName() + " " +
                            movie.getYoutubeMovie() + " " +
                            movie.getReleaseDate() +
                            "</li>");
                }

                if (movies.isEmpty()) {
                    out.println("<li>No movies found in the database.</li>");
                }

            } catch (Exception e) {
                // Catching errors so the whole page doesn't just crash with a blank screen
                out.println("<li style='color: red;'>Error loading movies: " + e.getMessage() + "</li>");
            }

            // 6. Close out the HTML tags
            out.println("</ul>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}