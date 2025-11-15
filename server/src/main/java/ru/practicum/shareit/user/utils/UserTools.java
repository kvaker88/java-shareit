package ru.practicum.shareit.user.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;

@Service
@RequiredArgsConstructor
public class UserTools {
    public static void validateUserForUpdate(UpdateUserRequestDto userDto) {
        String email = userDto.getEmail();

        if (StringUtils.hasText(email)) {
            if (!email.contains("@")) {
                throw new ValidationException("Некорректный формат email");
            }
        }
    }
}
