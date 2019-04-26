package com.celeritassolutions.demo.model

import java.util.*

class UsersModel{

    var user_id: String = ""
    var user_description: String = ""
    var img_path: String = ""
    var user_name: String = ""

    constructor() {}

    constructor(user_id: String, user_description: String, img_path: String, user_name: String) {
        this.user_id = user_id
        this.user_description = user_description
        this.img_path = img_path
        this.user_name = user_name

    }


}