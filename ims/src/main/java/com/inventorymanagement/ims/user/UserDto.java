package com.inventorymanagement.ims.user;

import java.util.List;

public record UserDto(
        String username,
        List<String> roles
) {
}