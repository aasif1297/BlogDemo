package com.celeritassolutions.demo.model

import java.util.Date

class BlogPost : BlogPostId {

    var user_id: String = ""
    var post_title: String = ""
    var image_url: String = ""
    var desc: String = ""
    var image_thumb: String =""
    var timestamp: Date? = null

    constructor() {}

    constructor(user_id: String, post_title: String, image_url: String, desc: String, image_thumb: String, timestamp: Date) {
        this.user_id = user_id
        this.image_url = image_url
        this.desc = desc
        this.image_thumb = image_thumb
        this.timestamp = timestamp
        this.post_title = post_title
    }
}
