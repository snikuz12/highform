package com.mypage.Model.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curriculum {
    private Long id;
    private String name;
    private Date startDate;
    private Date endDate;
}
