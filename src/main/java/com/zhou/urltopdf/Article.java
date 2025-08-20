package com.zhou.urltopdf;

import lombok.Data;

@Data
public class Article {
    private String title;
    private String link;
    private long create_time;
}
