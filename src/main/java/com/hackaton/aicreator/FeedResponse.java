package com.hackaton.aicreator;

import java.util.List;

public class FeedResponse {

    private List<Post> posts;

    public FeedResponse(List<Post> posts) {
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
