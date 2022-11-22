package ru.practicum.shareit.request.repository;

import lombok.Generated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;

@Repository
@Generated
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    Collection<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(long requesterId);

    Collection<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(long requesterId);
}
