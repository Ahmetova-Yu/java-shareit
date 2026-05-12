package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
	@NotNull(message = "ID вещи не может быть null")
	private Long itemId;

	@NotNull(message = "Дата начала не может быть null")
	@FutureOrPresent(message = "Дата начала не может быть в прошлом")
	private LocalDateTime start;

	@NotNull(message = "Дата окончания не может быть null")
	@Future(message = "Дата окончания должна быть в будущем")
	private LocalDateTime end;
}