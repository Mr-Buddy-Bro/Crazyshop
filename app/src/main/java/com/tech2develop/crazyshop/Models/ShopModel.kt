package com.tech2develop.crazyshop.Models

data class ShopModel(var companyName : String?, var companyDescription: String?, var email : String?, var fullName: String?, var phoneNo: String?,
                    var sellerKey : String?, var catList : ArrayList<CategoryModel>?, var productList : ArrayList<ProductModel>?)
