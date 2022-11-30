package ru.practicum.shareit.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class,
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

    @Getter
    @AllArgsConstructor
    static class ErrorResponse {
        private final String errorName;
        private final String error;
    }
}
