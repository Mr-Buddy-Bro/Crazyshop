package com.tech2develop.crazyshop.Models

import android.net.Uri

data class ProductModel(var name:String?, var description:String?, var category: String?,
                        var price: String?, var id : String?, var imageUrl : String?, var inStock : Boolean)
