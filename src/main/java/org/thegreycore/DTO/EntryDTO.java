package org.thegreycore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class EntryDTO {
    private String encryptedService;
    private String encryptedUsername;
    private String encryptedPassword;
}
