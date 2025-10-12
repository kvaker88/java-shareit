package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ItemRequest {
    private String description;
    private User requestor;
    private LocalDate created;
}
