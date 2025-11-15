package ru.practicum.shareit.item.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotBlank
    @Size(max = 1000, message = "Текст комментария не может превышать 1000 символов")
    private String text;
}