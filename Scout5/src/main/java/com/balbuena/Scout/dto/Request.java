package com.balbuena.Scout.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class Request {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PresidentCreate {
        @NotBlank(message = "President name is required")
        @Size(min = 2, max = 50, message = "President name must be between 2 and 50 characters")
        public String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        public String email;

        @NotBlank(message = "Club name is required")
        @Size(min = 2, max = 60, message = "Club name must be between 2 and 60 characters")
        public String clubName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PlaceBid {
        @NotNull(message = "presidentId is required")
        public Long presidentId;

        @NotNull(message = "bidAmount is required")
        @DecimalMin(value = "0.1", message = "Minimum bid is BRL 0.10")
        public Double bidAmount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Transfer {
        @NotNull(message = "presidentId is required")
        public Long presidentId;

        @NotNull(message = "playerOutId is required")
        public Long playerOutId;

        @NotNull(message = "playerInId is required")
        public Long playerInId;

        // Apenas para negociacao entre presidents
        public Long targetPresidentId;
        public Double offerAmount;
    }
}
