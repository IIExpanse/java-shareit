package ru.practicum.shareit.user.repository;

import lombok.Generated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

@Repository
@Generated
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
}
