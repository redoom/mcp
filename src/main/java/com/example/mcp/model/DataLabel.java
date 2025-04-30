package com.example.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataLabel {
    private String returnedData;
    private int nextData;
    private List<Path> paths;
}
