package com.celeritassolutions.demo.model

import com.google.firebase.firestore.Exclude

open class BlogPostId {

    @Exclude
    var BlogPostId: String = ""

    fun <T : BlogPostId> withId(id: String): T {
        this.BlogPostId = id
        return this as T
    }

}