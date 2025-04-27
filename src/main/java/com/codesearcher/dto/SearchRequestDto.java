package com.codesearcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {

    @NotBlank(message = "Search query cannot be empty")
    @Size(min = 3, max = 100, message = "Query must be between 3 and 100 characters")
    private String query;

    @Pattern(regexp = "^[a-zA-Z0-9+#]*$", message = "Language must contain only alphanumeric characters and + or #")
    private String language;

    @Pattern(regexp = "^[a-zA-Z0-9-_./]*$", message = "Repository must be in format 'owner/repo'")
    private String repository;

    @Pattern(regexp = "^\\.[a-zA-Z0-9]*$", message = "File extension must start with a dot followed by alphanumeric characters")
    private String fileExtension;
}