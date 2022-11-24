package ru.practicum.shareit.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.booking.exception.*;
import ru.practicum.shareit.item.exception.EmptyItemPatchRequestException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerUpdatingItemException;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.EmptyUserPatchRequestException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            EmptyItemPatchRequestException.class,
            EmptyUserPatchRequestException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            EndBeforeOrEqualsStartException.class,
            IllegalArgumentException.class,
            ItemNotAvailableForBookingException.class,
            ApprovalAlreadySetException.class,
            CommenterDontHaveBookingException.class
    })
    ResponseEntity<ErrorResponse> handleBadRequestExceptions(final Exception e) {
        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);

        if (e instanceof ConstraintViolationException) {
            int start = exceptionMessage.lastIndexOf(":") + 2;
            exceptionMessage = e.getMessage().substring(start);

        } else if (e instanceof MethodArgumentNotValidException) {
            int start = exceptionMessage.lastIndexOf("[") + 1;
            exceptionMessage = e.getMessage().substring(start, exceptionMessage.indexOf("]", start));
        }

        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({
            WrongOwnerUpdatingItemException.class
    })
    ResponseEntity<ErrorResponse> handleForbiddenExceptions(final RuntimeException e) {
        String exceptionName = e.getClass().getName();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, e.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler({
            ItemNotFoundException.class,
            UserNotFoundException.class,
            BookingNotFoundException.class,
            CantViewUnrelatedBookingException.class,
            WrongUserUpdatingBooking.class,
            CantBookOwnedItemException.class,
            RequestNotFoundException.class
    })
    ResponseEntity<ErrorResponse> handleNotFoundExceptions(final RuntimeException e) {
        String exceptionName = e.getClass().getName();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler({
            DuplicateEmailException.class,
            TimeWindowOccupiedException.class
    })
    ResponseEntity<ErrorResponse> handleConflictExceptions(final RuntimeException e) {
        String exceptionName = e.getClass().getName();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, e.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @Getter
    @AllArgsConstructor
    static class ErrorResponse {
        private final String errorName;
        private final String error;
    }
}
