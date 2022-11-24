package ru.practicum.shareit.request.repository;

import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;

@Repository
@Generated
public interface ItemRequestRepository extends PagingAndSortingRepository<ItemRequest, Long> {

    Collection<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(long requesterId);

    Page<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(long requesterId, Pageable pageable);
}
