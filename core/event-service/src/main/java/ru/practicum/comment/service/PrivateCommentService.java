package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewComment;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.util.List;

public interface PrivateCommentService {

    CommentShortDto createComment(Long userId, Long eventId, NewComment newComment);

    List<CommentShortDto> getUserComments(Long userId, Integer from, Integer size);

    CommentShortDto updateComment(Long userId, Long commentId, UpdateCommentDto updateComment);

    void deleteComment(Long userId, Long commentId);
}
