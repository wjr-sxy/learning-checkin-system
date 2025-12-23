package com.example.learningcheckin.dto;

import lombok.Data;

@Data
public class RankingDTO {
    private Long userId;
    private String username;
    private String avatar;
    private Integer score;
    private Integer rank; // Added for frontend display convenience
}
