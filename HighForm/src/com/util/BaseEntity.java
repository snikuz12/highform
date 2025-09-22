package com.util;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEntity {
    protected Date createdAt;
    protected Date updatedAt;
    protected char del_yn;


    public void markAsDeleted() { this.del_yn = 'Y'; }
}

