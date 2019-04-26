package com.celeritassolutions.demo.model

/**
 * Created by AkshayeJH on 30/11/17.
 */

class Conv {

    var isSeen: Boolean = false
    var timestamp: Long = 0

    constructor() {

    }

    constructor(seen: Boolean, timestamp: Long) {
        this.isSeen = seen
        this.timestamp = timestamp
    }
}