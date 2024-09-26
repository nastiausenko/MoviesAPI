package dev.nastiausenko.movies.swagger;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToObjectIdConverter implements Converter<String, ObjectId> {

    @Override
    public ObjectId convert(@NotNull String source) {
        if (ObjectId.isValid(source)) {
            return new ObjectId(source);
        } else {
            throw new IllegalArgumentException("Invalid ObjectId: " + source);
        }
    }
}
