package com.example.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSyncResponse {
    
    private String systemName;
    private int usersFetched;
    private int usersStored;
    private boolean success;
    private String message;
    private List<String> errors;
}
