package com.bwd4.mootd.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Device {
    @Id
    private String id;
}