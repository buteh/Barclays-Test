package com.eaglebank.user.api.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler
{
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BadRequestErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex)
	{
		List<BadRequestErrorResponse.Detail> details = ex.getBindingResult().getFieldErrors().stream().map(this::toDetail).toList();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BadRequestErrorResponse("Validation failed", details));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<BadRequestErrorResponse> handleConstraintViolation(ConstraintViolationException ex)
	{
		List<BadRequestErrorResponse.Detail> details = ex.getConstraintViolations().stream()
				.map(v -> new BadRequestErrorResponse.Detail(v.getPropertyPath().toString(), v.getMessage(), "constraint_violation"))
				.toList();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BadRequestErrorResponse("Validation failed", details));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex)
	{
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex)
	{
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex)
	{
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex)
	{
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex)
	{
		// keep message generic (don’t leak internals)
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleUnauthorized(UnauthorizedException ex)
	{
		return new ErrorResponse(ex.getMessage());
	}

	@ExceptionHandler(UnprocessableEntityException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ErrorResponse handleUnprocessable(UnprocessableEntityException ex)
	{
		return new ErrorResponse(ex.getMessage());
	}

	private BadRequestErrorResponse.Detail toDetail(FieldError fe)
	{
		String type = fe.getCode() == null ? "validation_error" : fe.getCode();
		return new BadRequestErrorResponse.Detail(fe.getField(), fe.getDefaultMessage(), type);
	}

}
