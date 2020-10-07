package com.hadoop.project.hadoop_project.twitter_project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterPost {

    @JsonProperty("content")
    private String content;
}
