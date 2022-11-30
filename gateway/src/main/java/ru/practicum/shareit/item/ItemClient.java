package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(long ownerId, ItemDto itemDto) {

        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> addComment(long authorId, long itemId, CommentDto commentDto) {

        return post("/" + itemId + "/comment", authorId, commentDto);
    }

    public ResponseEntity<Object> getItem(long requesterId, long id) {
        return get("/" + id, requesterId);
    }

    public ResponseEntity<Object> getOwnerItems(long ownerId, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );

        return get("?from={from}&size={size}", ownerId, parameters);
    }

    public ResponseEntity<Object> searchAvailableItems(
            long ownerId, String text, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );

        return get("/search?text={text}&from={from}&size={size}", ownerId, parameters);
    }

    public ResponseEntity<Object> updateItem(long ownerId, long itemId, ItemDto itemDto) {

        return patch("/" + itemId, ownerId, itemDto);
    }
}
