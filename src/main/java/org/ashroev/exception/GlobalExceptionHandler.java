package org.ashroev.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.ashroev.dto.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Не найдено", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public Object handleBadRequest(Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Некорректный запрос", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidation(Exception exception, HttpServletRequest request) {
        List<String> details = extractValidationDetails(exception);
        String message = details.isEmpty() ? "Проверьте корректность введенных данных" : details.get(0);
        return buildResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации", message, request, details);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Конфликт данных",
                "Запись нарушает ограничение базы данных. Проверьте уникальность и связанные записи.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(Exception exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка",
                "Не удалось выполнить операцию. Попробуйте еще раз или обратитесь к администратору.",
                request,
                List.of()
        );
    }

    private Object buildResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            List<String> details) {

        if (isApiRequest(request)) {
            ApiErrorResponse response = new ApiErrorResponse(
                    LocalDateTime.now(),
                    status.value(),
                    error,
                    message,
                    request.getRequestURI(),
                    details
            );
            return ResponseEntity.status(status).body(response);
        }

        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", error);
        modelAndView.addObject("message", message);
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return request.getRequestURI().startsWith("/api/")
                || (accept != null && accept.contains("application/json"));
    }

    private List<String> extractValidationDetails(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentException) {
            return methodArgumentException.getBindingResult().getFieldErrors().stream()
                    .map(this::formatFieldError)
                    .toList();
        }

        if (exception instanceof BindException bindException) {
            return bindException.getBindingResult().getFieldErrors().stream()
                    .map(this::formatFieldError)
                    .toList();
        }

        return List.of();
    }

    private String formatFieldError(FieldError error) {
        return error.getDefaultMessage();
    }
}
