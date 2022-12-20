package ru.practicum.shareit.item.comment.mapper;

import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
@Generated
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    Comment mapToModel(CommentDto commentDto, User author, Item item);

    @Mapping(source = "comment.author.name", target = "authorName")
    CommentDto mapToDto(Comment comment);
}