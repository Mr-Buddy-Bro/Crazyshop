package com.tech2develop.crazyshop.Models

data class OrderModel(var itemName: String?, var deliveryAddress: AddressModel?, var itemPrice: String?, var deliveryStatus: String?, var shopName: String?, var date : String?) {
}