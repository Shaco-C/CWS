package com.watergun.dto;

import com.watergun.entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO extends Reviews {
    private String username;
    private String avatarUrl;

}
