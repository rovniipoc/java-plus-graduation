package ru.practicum.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.category.model.Category;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.feign.UserServiceClient;
import ru.practicum.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    private final UserServiceClient userServiceClient;
    private final EventMapper eventMapper;

    public CompilationDto toCompilationDto(Compilation compilation) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setEvents(mapShortAndAddUsersAndCategories(compilation.getEvents()));
        return compilationDto;
    }

    public List<CompilationDto> toCompilationDto(Iterable<Compilation> compilations) {
        List<CompilationDto> result = new ArrayList<>();
        for (Compilation compilation : compilations) {
            result.add(toCompilationDto(compilation));
        }
        return result;
    }

    public static Compilation toCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setEvents(new HashSet<Event>());
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());
        return compilation;
    }

    private Set<EventShortDto> mapShortAndAddUsersAndCategories(Set<Event> events) {
        List<Long> userIds = events.stream().map(Event::getInitiator).toList();
        Map<Long, User> users = loadUsers(userIds);
        Map<Long, Category> categories = events.stream().collect(Collectors.toMap(Event::getId, Event::getCategory));
        return events.stream()
                .map(e -> eventMapper.toEventShortDto(e, users.get(e.getInitiator())))
                .collect(Collectors.toSet());
    }

    private Map<Long, User> loadUsers(List<Long> ids) {
        return userServiceClient.getUsersWithIds(ids).stream().collect(Collectors.toMap(User::getId, user -> user));
    }
}
