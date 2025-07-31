package com.melvstein.ecommerce.api.shared.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record FilterPartsDto(
        String operator,
        String field,
        String logic,
        String value
) {
}
