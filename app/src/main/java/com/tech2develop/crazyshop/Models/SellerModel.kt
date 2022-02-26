package com.tech2develop.crazyshop.Models

import android.net.Uri

data class SellerModel(
    var companyName : String,
    var companyDescription : String,
    var fullName : String,
    var email : String,
    var phoneNo : String,
    var password : String,
    var audienceSize : String?,
    var isVerified : String?,
    var sellerKey : String?

)
