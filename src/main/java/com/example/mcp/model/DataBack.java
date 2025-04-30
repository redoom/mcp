package com.example.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataBack {
    private String data;
    private List<Path> nextPaths;
}
