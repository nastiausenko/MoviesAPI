package dev.nastiausenko.movies.review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "review")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    private ObjectId id;
    @NotNull(message = "Review body is mandatory")
    private String body;
    private ObjectId userId;

    public Review(String body, ObjectId userId) {
        this.body = body;
        this.userId = userId;
    }
}
