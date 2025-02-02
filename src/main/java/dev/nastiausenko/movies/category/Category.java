package dev.nastiausenko.movies.category;

import dev.nastiausenko.movies.movie.Movie;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document(collection = "categories")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    private ObjectId id;
    @NotNull(message = "Category name is mandatory")
    private String name;
    private ObjectId userId;
    private boolean isAdminCategory;
    private boolean isPublicCategory;

    @DocumentReference
    private List<Movie> movies;
}
