package org.thegreycore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class NewEntryDTO {
    private String service;
    private String username;
    private String password;
    private String masterKey;
}
