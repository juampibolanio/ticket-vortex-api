package com.chacuio.ticketvortexapi.common.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private String message;
    private int status;
    private String path;
    private Instant timestamp;
    private Map<String, String> fieldErrors;
}
