package dev.nastiausenko.movies.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByName(String username);
    Optional<User> findByEmail(String email);
    void deleteByEmail(String email);
}
