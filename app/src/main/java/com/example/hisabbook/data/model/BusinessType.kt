package com.example.hisabbook.data.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.HomeRepairService
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.hisabbook.R
import com.example.hisabbook.data.local.entity.InventoryItem

data class PresetInventoryItem(
    val name: String,
    val unit: String,
    val defaultPrice: Double,
    val minimumStock: Double = 10.0
)

enum class BusinessTypeCategory(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val defaultUnits: List<String>,
    val presetItems: List<PresetInventoryItem>
) {
    TEA_STALL_CAFE(
        id = "TEA_STALL_CAFE",
        titleRes = R.string.category_tea_stall,
        descriptionRes = R.string.category_tea_stall_desc,
        icon = Icons.Rounded.LocalCafe,
        defaultUnits = listOf("Cup", "Plate", "Pcs", "Packet", "Ltr"),
        presetItems = listOf(
            PresetInventoryItem("Special Masala Chai", "Cup", 15.0, 50.0),
            PresetInventoryItem("Cutting Chai", "Cup", 10.0, 100.0),
            PresetInventoryItem("Hot Coffee", "Cup", 25.0, 30.0),
            PresetInventoryItem("Samosa", "Pcs", 20.0, 20.0),
            PresetInventoryItem("Bun Maska", "Plate", 35.0, 15.0),
            PresetInventoryItem("Cream Roll", "Pcs", 15.0, 20.0)
        )
    ),

    GARAGE_MECHANIC(
        id = "GARAGE_MECHANIC",
        titleRes = R.string.category_garage_mechanic,
        descriptionRes = R.string.category_garage_mechanic_desc,
        icon = Icons.Rounded.Build,
        defaultUnits = listOf("Pcs", "Hours", "Ltr", "Set", "Service"),
        presetItems = listOf(
            PresetInventoryItem("Engine Oil 1L (4T 10W-30)", "Ltr", 450.0, 5.0),
            PresetInventoryItem("General Bike Servicing Labor", "Service", 350.0, 1.0),
            PresetInventoryItem("Front & Rear Brake Shoe Set", "Set", 280.0, 4.0),
            PresetInventoryItem("Chain Lube & Cleaning", "Service", 120.0, 10.0),
            PresetInventoryItem("Foam Water Wash", "Service", 150.0, 1.0),
            PresetInventoryItem("Spark Plug", "Pcs", 110.0, 6.0)
        )
    ),

    DAIRY_PARLOR(
        id = "DAIRY_PARLOR",
        titleRes = R.string.category_dairy_parlor,
        descriptionRes = R.string.category_dairy_parlor_desc,
        icon = Icons.Rounded.LocalDrink,
        defaultUnits = listOf("Ltr", "Ml", "Kg", "Grams", "Pcs", "Packet"),
        presetItems = listOf(
            PresetInventoryItem("Full Cream Milk 1L", "Ltr", 66.0, 20.0),
            PresetInventoryItem("Toned Milk 500ml", "Packet", 27.0, 30.0),
            PresetInventoryItem("Fresh Paneer", "Kg", 420.0, 5.0),
            PresetInventoryItem("Fresh Curd / Dahi", "Kg", 70.0, 10.0),
            PresetInventoryItem("Fresh Cow Ghee", "Kg", 650.0, 3.0),
            PresetInventoryItem("Butter 100g", "Pcs", 56.0, 15.0)
        )
    ),

    KIRANA_GROCERY(
        id = "KIRANA_GROCERY",
        titleRes = R.string.category_kirana_grocery,
        descriptionRes = R.string.category_kirana_grocery_desc,
        icon = Icons.Rounded.ShoppingCart,
        defaultUnits = listOf("Kg", "Grams", "Pcs", "Packet", "Box", "Ltr"),
        presetItems = listOf(
            PresetInventoryItem("Sugar / Cheeni", "Kg", 44.0, 25.0),
            PresetInventoryItem("Fortune Refined Oil 1L", "Ltr", 135.0, 10.0),
            PresetInventoryItem("Wheat Flour / Atta 5kg", "Packet", 210.0, 8.0),
            PresetInventoryItem("Iodized Salt 1kg", "Packet", 28.0, 15.0),
            PresetInventoryItem("Toor Dal / Arhar Dal", "Kg", 160.0, 10.0),
            PresetInventoryItem("Bathing Soap Bar", "Pcs", 35.0, 20.0)
        )
    ),

    CLOTHING_FOOTWEAR(
        id = "CLOTHING_FOOTWEAR",
        titleRes = R.string.category_clothing_footwear,
        descriptionRes = R.string.category_clothing_footwear_desc,
        icon = Icons.Rounded.Checkroom,
        defaultUnits = listOf("Pcs", "Pair", "Meter", "Set"),
        presetItems = listOf(
            PresetInventoryItem("Cotton T-Shirt (Round Neck)", "Pcs", 399.0, 10.0),
            PresetInventoryItem("Slim Fit Denim Jeans", "Pcs", 899.0, 8.0),
            PresetInventoryItem("Pant / Shirt Tailoring Charge", "Pcs", 350.0, 1.0),
            PresetInventoryItem("Cotton Shirt Fabric", "Meter", 220.0, 15.0),
            PresetInventoryItem("Men's Leather Slipper", "Pair", 450.0, 5.0)
        )
    ),

    MOBILE_ELECTRONICS(
        id = "MOBILE_ELECTRONICS",
        titleRes = R.string.category_mobile_electronics,
        descriptionRes = R.string.category_mobile_electronics_desc,
        icon = Icons.Rounded.Smartphone,
        defaultUnits = listOf("Pcs", "Service", "Set", "Box"),
        presetItems = listOf(
            PresetInventoryItem("Tempered Glass / Screen Guard", "Pcs", 100.0, 20.0),
            PresetInventoryItem("Silicone Back Cover", "Pcs", 150.0, 15.0),
            PresetInventoryItem("Fast Charging Cable (Type-C)", "Pcs", 250.0, 10.0),
            PresetInventoryItem("Fast Charger Adapter (20W)", "Pcs", 499.0, 5.0),
            PresetInventoryItem("Mobile Display Screen Replacement Labor", "Service", 300.0, 1.0)
        )
    ),

    HARDWARE_BUILDING(
        id = "HARDWARE_BUILDING",
        titleRes = R.string.category_hardware_building,
        descriptionRes = R.string.category_hardware_building_desc,
        icon = Icons.Rounded.Construction,
        defaultUnits = listOf("Pcs", "Meter", "Sq.ft", "Kg", "Bag", "Set"),
        presetItems = listOf(
            PresetInventoryItem("BRUSH RUBBER", "Pcs", 350.0, 20.0),
            PresetInventoryItem("CLEAR PVC RUBBER", "Kg", 135.0, 10.0),
            PresetInventoryItem("CUBEX STEEL BEARING", "Pcs", 18.0, 30.0),
            PresetInventoryItem("HD- ALUMINIUM", "Kg", 535.0, 50.0),
            PresetInventoryItem("TOUGHENED GLASS 12MM", "Sq.ft", 220.0, 50.0),
            PresetInventoryItem("SLIDING WINDOW FRAME", "Meter", 480.0, 20.0)
        )
    ),

    SALON_BEAUTY(
        id = "SALON_BEAUTY",
        titleRes = R.string.category_salon_beauty,
        descriptionRes = R.string.category_salon_beauty_desc,
        icon = Icons.Rounded.ContentCut,
        defaultUnits = listOf("Service", "Pcs", "Set"),
        presetItems = listOf(
            PresetInventoryItem("Men's Haircut", "Service", 100.0, 1.0),
            PresetInventoryItem("Beard Trim & Styling", "Service", 60.0, 1.0),
            PresetInventoryItem("Hair Color Application", "Service", 250.0, 1.0),
            PresetInventoryItem("Herbal Facial", "Service", 500.0, 1.0),
            PresetInventoryItem("Hair Spa Treatment", "Service", 600.0, 1.0)
        )
    ),

    FRUITS_VEGETABLES(
        id = "FRUITS_VEGETABLES",
        titleRes = R.string.category_fruits_vegetables,
        descriptionRes = R.string.category_fruits_vegetables_desc,
        icon = Icons.Rounded.Eco,
        defaultUnits = listOf("Kg", "Grams", "Dozen", "Bunch", "Pcs"),
        presetItems = listOf(
            PresetInventoryItem("Fresh Potato / Aloo", "Kg", 30.0, 50.0),
            PresetInventoryItem("Fresh Onion / Pyaz", "Kg", 35.0, 50.0),
            PresetInventoryItem("Tomato / Tamatar", "Kg", 40.0, 30.0),
            PresetInventoryItem("Fresh Bananas", "Dozen", 60.0, 10.0),
            PresetInventoryItem("Shimla Apple", "Kg", 140.0, 15.0)
        )
    ),

    PHARMACY_MEDICAL(
        id = "PHARMACY_MEDICAL",
        titleRes = R.string.category_pharmacy_medical,
        descriptionRes = R.string.category_pharmacy_medical_desc,
        icon = Icons.Rounded.LocalPharmacy,
        defaultUnits = listOf("Strip", "Box", "Pcs", "Bottle", "Packet"),
        presetItems = listOf(
            PresetInventoryItem("Paracetamol 650mg Tablets", "Strip", 32.0, 20.0),
            PresetInventoryItem("Cough Syrup 100ml", "Bottle", 110.0, 10.0),
            PresetInventoryItem("ORSL Rehydration Drink 200ml", "Pcs", 38.0, 30.0),
            PresetInventoryItem("Antiseptic Liquid 100ml", "Bottle", 65.0, 10.0),
            PresetInventoryItem("First Aid Bandage Box", "Box", 150.0, 5.0)
        )
    ),

    SERVICE_PROVIDER(
        id = "SERVICE_PROVIDER",
        titleRes = R.string.category_service_provider,
        descriptionRes = R.string.category_service_provider_desc,
        icon = Icons.Rounded.HomeRepairService,
        defaultUnits = listOf("Hours", "Service", "Day", "Flat", "Pcs"),
        presetItems = listOf(
            PresetInventoryItem("Visiting & Inspection Charge", "Service", 200.0, 1.0),
            PresetInventoryItem("Electrical Wiring Repair (per hour)", "Hours", 300.0, 1.0),
            PresetInventoryItem("Plumbing Leakage Fixing", "Service", 350.0, 1.0),
            PresetInventoryItem("AC Service & Washing", "Service", 500.0, 1.0),
            PresetInventoryItem("Carpenter Work (Daily Rate)", "Day", 800.0, 1.0)
        )
    ),

    GENERAL(
        id = "GENERAL",
        titleRes = R.string.category_general,
        descriptionRes = R.string.category_general_desc,
        icon = Icons.Rounded.Business,
        defaultUnits = listOf("Pcs", "Kg", "Grams", "Ltr", "Ml", "Meter", "Sq.ft", "Cup", "Plate", "Hours", "Pair", "Dozen", "Strip", "Box", "Set", "Packet", "Service"),
        presetItems = listOf(
            PresetInventoryItem("General Item 1", "Pcs", 100.0, 10.0),
            PresetInventoryItem("General Service", "Service", 250.0, 1.0)
        )
    );

    companion object {
        fun fromId(id: String?): BusinessTypeCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        }

        val ALL_UNITS: List<String> = listOf(
            "Pcs", "Kg", "Grams", "Ltr", "Ml", "Meter", "Sq.ft",
            "Cup", "Plate", "Hours", "Pair", "Dozen", "Strip",
            "Box", "Set", "Packet", "Service", "Day"
        )
    }
}
