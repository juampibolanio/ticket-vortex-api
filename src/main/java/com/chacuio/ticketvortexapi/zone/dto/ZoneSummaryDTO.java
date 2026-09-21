package com.chacuio.ticketvortexapi.zone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ZoneSummaryDTO {
    UUID id;
    String name;
    Integer capacity;
    UUID eventId;
}
