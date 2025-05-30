package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.model.Weight;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(componentModel = "spring")
public interface WeightMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weight", expression = "java(mapWeight(userAction))")
    Weight toWeight(UserActionAvro userAction);

    default double mapWeight(UserActionAvro userAction) {
        return switch (userAction.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
