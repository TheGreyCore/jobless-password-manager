package org.thegreycore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class GetEntryDTO {
    private int id;
    private String service;
    private String username;
}
