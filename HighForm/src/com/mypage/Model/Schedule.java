package com.mypage.Model;

import java.time.LocalDate;

public record Schedule(
        Long id,
        Long userId,
        String title,
        String memo,
        LocalDate startDate,
        LocalDate endDate
) {
    public boolean contains(LocalDate d) {
        return !(d.isBefore(startDate) || d.isAfter(endDate));
    }
}
