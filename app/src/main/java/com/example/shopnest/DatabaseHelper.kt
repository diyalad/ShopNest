package com.example.shopnest

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.shopnest.Model.Address
import com.example.shopnest.Model.AdminCartItem
import com.example.shopnest.Model.AdminFavorite
import com.example.shopnest.Model.AdminOrderHistoryItem
import com.example.shopnest.Model.CartItem
import com.example.shopnest.Model.Category
import com.example.shopnest.Model.Order
import com.example.shopnest.Model.OrderHistory
import com.example.shopnest.Model.Profile
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.Model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object {
        private const val DATABASE_NAME = "ShopNest.db"
        private const val DATABASE_VERSION = 30
        private var INSTANCE: DatabaseHelper? = null
        private const val MAX_BLOB_SIZE = 1024 * 1024 // 1MB max size for images
        private const val IMAGE_COMPRESSION_QUALITY = 70

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
        // Cart Table
        const val TABLE_CART = "cart"
        const val COLUMN_CART_ID = "cart_id"
        const val CART_COLUMN_USER_ID = "user_id"
        const val COLUMN_PRODUCT_ID = "product_id"
        const val COLUMN_PRODUCT_NAME = "product_name"
        const val COLUMN_PRODUCT_PRICE = "product_price"
        const val COLUMN_PRODUCT_IMAGE_RES_ID = "product_image_res_id"
        const val COLUMN_DISCOUNT = "discount"
        const val COLUMN_QUANTITY = "quantity"

        // User Table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PHONE = "phone"
        const val COLUMN_USER_PASSWORD = "password"


        // Category columns
         const val TABLE_CATEGORIES = "categories"
         const val COLUMN_CATEGORY_ID = "id"
         const val COLUMN_CATEGORY_NAME = "name"
         const val COLUMN_CATEGORY_IMAGE_PATH = "image_path"

        // Subcategory columns
        const val TABLE_SUBCATEGORIES = "subcategories"
        const val COLUMN_SUBCATEGORY_ID = "id"
        const val COLUMN_SUBCATEGORY_NAME = "name"
        const val COLUMN_SUBCATEGORY_IMAGE_PATH = "image_path"
        const val COLUMN_CATEGORY_FOREIGN_ID = "category_id"
        const val COLUMN_SUBCATEGORY_DISCOUNTED_PRICE = "discounted_price"
        const val COLUMN_SUBCATEGORY_ORIGINAL_PRICE = "original_price"
        const val COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE = "discount_percentage"
        const val COLUMN_SUBCATEGORY_DELIVERY_INFO = "delivery_info"
        const val COLUMN_SUBCATEGORY_DETAILS = "details"
        const val COLUMN_SUBCATEGORY_RATING = "rating"


        // Category Table
//        private const val TABLE_CATEGORIES = "categories"
//        private const val COLUMN_CATEGORY_ID = "id"
//        private const val COLUMN_CATEGORY_NAME = "category_name"
//        private const val COLUMN_CATEGORY_IMAGE = "category_image"
//        private const val COLUMN_CATEGORY_IMAGE_PATH = "image_path"

        // Subcategory Table (now includes product fields)
//        internal const val TABLE_SUBCATEGORIES = "subcategories"
//        private const val COLUMN_SUBCATEGORY_ID = "id"
//        private const val COLUMN_SUBCATEGORY_NAME = "subcategory_name"
//        private const val COLUMN_SUBCATEGORY_IMAGE = "subcategory_image"
//        private const val COLUMN_CATEGORY_FOREIGN_ID = "category_id"
//        private const val COLUMN_SUBCATEGORY_DISCOUNTED_PRICE = "discounted_price"
//        private const val COLUMN_SUBCATEGORY_ORIGINAL_PRICE = "original_price"
//        private const val COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE = "discount_percentage"
//        private const val COLUMN_SUBCATEGORY_DELIVERY_INFO = "delivery_info"
//        private const val COLUMN_SUBCATEGORY_DETAILS = "details"
//        private const val COLUMN_SUBCATEGORY_RATING = "rating"

        // Profile Table
        private const val TABLE_PROFILE = "profile"
        private const val COLUMN_PROFILE_ID = "profile_id"
        private const val COLUMN_PROFILE_USER_ID = "user_id"
        private const val COLUMN_PROFILE_FULL_NAME = "full_name"
        private const val COLUMN_PROFILE_PHONE = "phone"
        private const val COLUMN_PROFILE_EMAIL = "email"
        private const val COLUMN_PROFILE_GENDER = "gender"
        private const val COLUMN_PROFILE_PINCODE = "pincode"
        private const val COLUMN_PROFILE_CITY = "city"
        private const val COLUMN_PROFILE_STATE = "state"
        private const val COLUMN_PROFILE_DOB = "dob"
        private const val COLUMN_PROFILE_MARITAL_STATUS = "marital_status"
        private const val COLUMN_PROFILE_EDUCATION = "education"
        private const val COLUMN_PROFILE_INCOME = "income"
        private const val COLUMN_PROFILE_IMAGE = "profile_image"
        private const val COLUMN_PROFILE_IMAGE_PATH= "profile_image_path"

        // Address Table
        const val TABLE_ADDRESSES = "addresses"
        const val COLUMN_ADDRESS_ID = "address_id"
        const val COLUMN_ADDRESS_FULL_NAME = "full_name"
        const val COLUMN_ADDRESS_PHONE = "phone"
        const val COLUMN_ADDRESS_ALTERNATE_PHONE = "alternate_phone"
        const val COLUMN_ADDRESS_PINCODE = "pincode"
        const val COLUMN_ADDRESS_STATE = "state"
        const val COLUMN_ADDRESS_CITY = "city"
        const val COLUMN_ADDRESS_HOUSE_NO = "house_no"
        const val COLUMN_ADDRESS_ROAD_NAME = "road_name"
        const val COLUMN_ADDRESS_USER_ID = "user_id" // For eign key to link address to a user

        // Order Table
        private const val TABLE_ORDERS = "orders"
        private const val COLUMN_ORDER_ID = "order_id"
        private const val COLUMN_ORDER_USER_ID = "user_id"
        private const val COLUMN_ORDER_SUBCATEGORY_ID = "subcategory_id"
        private const val COLUMN_ORDER_QUANTITY = "quantity"
        private const val COLUMN_ORDER_TOTAL_PRICE = "total_price"
        private const val COLUMN_ORDER_PAYMENT_ID = "payment_id"

        // Favorites Table
        private const val TABLE_FAVORITES = "favorites"
        private const val COLUMN_FAVORITE_ID = "id"
        private const val COLUMN_FAVORITE_USER_ID = "user_id"
        private const val COLUMN_FAVORITE_SUBCATEGORY_ID = "subcategory_id"

        // Order History Table
        const val TABLE_ORDER_HISTORY = "order_history"
        const val COLUMN_ORDER_HISTORY_ID = "id"
        const val COLUMN_ORDER_HISTORY_USER_ID = "user_id"
        const val COLUMN_ORDER_HISTORY_SUBCATEGORY_ID = "subcategory_id"
        const val COLUMN_ORDER_HISTORY_QUANTITY = "quantity"
        const val COLUMN_ORDER_HISTORY_TOTAL_PRICE = "total_price"
        const val COLUMN_ORDER_HISTORY_PAYMENT_ID = "payment_id"
        const val COLUMN_ORDER_HISTORY_PAYMENT_STATUS = "payment_status"
        const val COLUMN_ORDER_HISTORY_DELIVERY_START_DATE = "delivery_start_date"
        const val COLUMN_ORDER_HISTORY_DELIVERY_END_DATE = "delivery_end_date"
        const val COLUMN_ORDER_HISTORY_SIZE = "size"
        const val COLUMN_ORDER_HISTORY_ADDRESS_ID = "address_id"
        const val COLUMN_ORDER_HISTORY_ORDER_DATE = "order_date"

        // Ratings Table
        const val TABLE_RATINGS = "ratings"
        const val COLUMN_RATING_ID = "rating_id"
        const val COLUMN_RATING_USER_ID = "user_id"
        const val COLUMN_RATING_STARS = "stars"
        const val COLUMN_RATING_FEEDBACK = "feedback"
        const val COLUMN_RATING_DATE = "rating_date"

        //Admin Table
        const val TABLE_ADMIN_LOGIN = "admin_login"
        const val COLUMN_ADMIN_USERNAME = "username"
        const val COLUMN_ADMIN_PASSWORD = "password"


    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Users Table
        val createUserTable = """
        CREATE TABLE $TABLE_USERS (
            $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_NAME TEXT,
            $COLUMN_USER_EMAIL TEXT UNIQUE,
            $COLUMN_USER_PHONE TEXT UNIQUE,
            $COLUMN_USER_PASSWORD TEXT
        )
    """.trimIndent()

        db?.execSQL(createUserTable)

        // Categories Table
//        val createCategoryTable = """
//            CREATE TABLE $TABLE_CATEGORIES (
//                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
//                $COLUMN_CATEGORY_NAME TEXT UNIQUE,
//                $COLUMN_CATEGORY_IMAGE BLOB NOT NULL,
//                $COLUMN_CATEGORY_IMAGE_PATH TEXT
//            )
//        """.trimIndent()
//        db?.execSQL(createCategoryTable)

        // Subcategories Table (
//        val createSubcategoryTable = """
//            CREATE TABLE $TABLE_SUBCATEGORIES (
//                $COLUMN_SUBCATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
//                $COLUMN_SUBCATEGORY_NAME TEXT UNIQUE,
//                $COLUMN_CATEGORY_FOREIGN_ID INTEGER,
//                $COLUMN_SUBCATEGORY_IMAGE INTEGER,
//                $COLUMN_SUBCATEGORY_DISCOUNTED_PRICE REAL,
//                $COLUMN_SUBCATEGORY_ORIGINAL_PRICE REAL,
//                $COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE INTEGER,
//                $COLUMN_SUBCATEGORY_DELIVERY_INFO TEXT,
//                $COLUMN_SUBCATEGORY_DETAILS TEXT,
//                $COLUMN_SUBCATEGORY_RATING REAL,
//                FOREIGN KEY($COLUMN_CATEGORY_FOREIGN_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID)
//            )
//        """.trimIndent()
//        db?.execSQL(createSubcategoryTable)

        // Create Profile Table
        val createProfileTable = """
            CREATE TABLE $TABLE_PROFILE (
                $COLUMN_PROFILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROFILE_USER_ID INTEGER,
                $COLUMN_PROFILE_FULL_NAME TEXT,
                $COLUMN_PROFILE_PHONE TEXT,
                $COLUMN_PROFILE_EMAIL TEXT,
                $COLUMN_PROFILE_GENDER TEXT,
                $COLUMN_PROFILE_PINCODE TEXT,
                $COLUMN_PROFILE_CITY TEXT,
                $COLUMN_PROFILE_STATE TEXT,
                $COLUMN_PROFILE_DOB TEXT,
                $COLUMN_PROFILE_MARITAL_STATUS TEXT,
                $COLUMN_PROFILE_EDUCATION TEXT,
                $COLUMN_PROFILE_INCOME TEXT,
                $COLUMN_PROFILE_IMAGE_PATH TEXT
            )
        """.trimIndent()
        db?.execSQL(createProfileTable)

        // Create Address Table
        val createAddressTable = """
            CREATE TABLE $TABLE_ADDRESSES (
                $COLUMN_ADDRESS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ADDRESS_FULL_NAME TEXT,
                $COLUMN_ADDRESS_PHONE TEXT,
                $COLUMN_ADDRESS_ALTERNATE_PHONE TEXT,
                $COLUMN_ADDRESS_PINCODE TEXT,
                $COLUMN_ADDRESS_STATE TEXT,
                $COLUMN_ADDRESS_CITY TEXT,
                $COLUMN_ADDRESS_HOUSE_NO TEXT,
                $COLUMN_ADDRESS_ROAD_NAME TEXT,
                $COLUMN_ADDRESS_USER_ID INTEGER,
                FOREIGN KEY($COLUMN_ADDRESS_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        db?.execSQL(createAddressTable)

        // Create Orders Table
        val createOrdersTable = """
            CREATE TABLE $TABLE_ORDERS (
                $COLUMN_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ORDER_USER_ID INTEGER,
                $COLUMN_ORDER_SUBCATEGORY_ID INTEGER,
                $COLUMN_ORDER_QUANTITY INTEGER,
                $COLUMN_ORDER_TOTAL_PRICE REAL,
                $COLUMN_ORDER_PAYMENT_ID TEXT,
                FOREIGN KEY($COLUMN_ORDER_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
                FOREIGN KEY($COLUMN_ORDER_SUBCATEGORY_ID) REFERENCES $TABLE_SUBCATEGORIES($COLUMN_SUBCATEGORY_ID)
            )
        """.trimIndent()
        db?.execSQL(createOrdersTable)

        // Create Favorites Table
        val createFavoritesTable = """
        CREATE TABLE $TABLE_FAVORITES (
            $COLUMN_FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_FAVORITE_USER_ID INTEGER,
            $COLUMN_FAVORITE_SUBCATEGORY_ID INTEGER,
            FOREIGN KEY($COLUMN_FAVORITE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
            FOREIGN KEY($COLUMN_FAVORITE_SUBCATEGORY_ID) REFERENCES $TABLE_SUBCATEGORIES($COLUMN_SUBCATEGORY_ID)
        )
    """.trimIndent()
        db?.execSQL(createFavoritesTable)

        // Create Order History Table
        val createOrderHistoryTable = """
    CREATE TABLE $TABLE_ORDER_HISTORY (
        $COLUMN_ORDER_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_ORDER_HISTORY_USER_ID INTEGER,
        $COLUMN_ORDER_HISTORY_SUBCATEGORY_ID INTEGER,
        $COLUMN_ORDER_HISTORY_QUANTITY INTEGER,
        $COLUMN_ORDER_HISTORY_TOTAL_PRICE REAL,
        $COLUMN_ORDER_HISTORY_PAYMENT_ID TEXT,
        $COLUMN_ORDER_HISTORY_PAYMENT_STATUS TEXT,
        $COLUMN_ORDER_HISTORY_DELIVERY_START_DATE TEXT,
        $COLUMN_ORDER_HISTORY_DELIVERY_END_DATE TEXT,
        $COLUMN_ORDER_HISTORY_SIZE TEXT,
        $COLUMN_ORDER_HISTORY_ADDRESS_ID INTEGER,
        $COLUMN_ORDER_HISTORY_ORDER_DATE TEXT DEFAULT CURRENT_DATE,
        FOREIGN KEY($COLUMN_ORDER_HISTORY_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
        FOREIGN KEY($COLUMN_ORDER_HISTORY_SUBCATEGORY_ID) REFERENCES $TABLE_SUBCATEGORIES($COLUMN_SUBCATEGORY_ID),
        FOREIGN KEY($COLUMN_ORDER_HISTORY_ADDRESS_ID) REFERENCES $TABLE_ADDRESSES($COLUMN_ADDRESS_ID)
    )
""".trimIndent()
        db?.execSQL(createOrderHistoryTable)

        // Create Ratings Table
        val createRatingsTable = """
    CREATE TABLE IF NOT EXISTS $TABLE_RATINGS (
        $COLUMN_RATING_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_RATING_USER_ID INTEGER NOT NULL,
        $COLUMN_RATING_STARS INTEGER NOT NULL,
        $COLUMN_RATING_FEEDBACK TEXT NOT NULL,
        $COLUMN_RATING_DATE TEXT NOT NULL,
        FOREIGN KEY ($COLUMN_RATING_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
    );
""".trimIndent()
        db?.execSQL(createRatingsTable)


        // Create categories table (removed BLOB column)
        val createCategoryTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT UNIQUE,
                $COLUMN_CATEGORY_IMAGE_PATH TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(createCategoryTable)

        // Create subcategories table (removed BLOB column)
        val createSubcategoryTable = """
            CREATE TABLE $TABLE_SUBCATEGORIES (
                $COLUMN_SUBCATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SUBCATEGORY_NAME TEXT,
                $COLUMN_SUBCATEGORY_IMAGE_PATH TEXT NOT NULL,
                $COLUMN_CATEGORY_FOREIGN_ID INTEGER,
                $COLUMN_SUBCATEGORY_DISCOUNTED_PRICE REAL,
                $COLUMN_SUBCATEGORY_ORIGINAL_PRICE REAL,
                $COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE INTEGER,
                $COLUMN_SUBCATEGORY_DELIVERY_INFO TEXT,
                $COLUMN_SUBCATEGORY_DETAILS TEXT,
                $COLUMN_SUBCATEGORY_RATING REAL,
                FOREIGN KEY($COLUMN_CATEGORY_FOREIGN_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID)
            )
        """.trimIndent()
        db?.execSQL(createSubcategoryTable)

// In cart table creation:
        val CREATE_CART_TABLE = """
    CREATE TABLE $TABLE_CART (
        $COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_USER_ID INTEGER NOT NULL,
        $COLUMN_PRODUCT_ID INTEGER NOT NULL,
        $COLUMN_PRODUCT_NAME TEXT NOT NULL,
        $COLUMN_PRODUCT_PRICE REAL NOT NULL,
        $COLUMN_PRODUCT_IMAGE_RES_ID INTEGER NOT NULL,
        $COLUMN_DISCOUNT REAL DEFAULT 0,
        $COLUMN_QUANTITY INTEGER DEFAULT 1,
        FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS ($COLUMN_USER_ID),
        FOREIGN KEY ($COLUMN_PRODUCT_ID) REFERENCES $TABLE_SUBCATEGORIES ($COLUMN_SUBCATEGORY_ID)
    )
""".trimIndent()
        db?.execSQL(CREATE_CART_TABLE)

        val createAdminLoginTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ADMIN_LOGIN (
                $COLUMN_ADMIN_USERNAME TEXT PRIMARY KEY,
                $COLUMN_ADMIN_PASSWORD TEXT NOT NULL
            );
        """.trimIndent()
        db?.execSQL(createAdminLoginTable)

        val insertAdminCredentials = """
            INSERT INTO $TABLE_ADMIN_LOGIN ($COLUMN_ADMIN_USERNAME, $COLUMN_ADMIN_PASSWORD)
            VALUES ('admin', 'admin123');
        """.trimIndent()
        db?.execSQL(insertAdminCredentials)
    }


    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUBCATEGORIES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ADDRESSES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        db?.execSQL("DROP TABLE IF EXISTS  $TABLE_ORDER_HISTORY")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RATINGS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ADMIN_LOGIN")
        onCreate(db)
    }

    // --------------------User table methods --------------------

    fun getUserById(userId: Long): User? {
        val db = this.readableDatabase
        var user: User? = null
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE))

            user = User(id, name, email, phone)
        }

        cursor.close()
        db.close()
        return user
    }

    fun getUser(email: String, password: String): User? {
        val db = this.readableDatabase
        var user: User? = null
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)) // Changed from "id" to COLUMN_USER_ID
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))

            user = User(id, name, email, phone)
        }

        cursor.close()
        db.close()
        return user
    }

    fun updateUser(userId: Long, name: String, email: String, phone: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, name)
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PHONE, phone)
        }
        val result = db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        db.close()
        if (result == 0) {
            Log.e(TAG, "Failed to update User with ID: $userId")
        } else {
            Log.d(TAG, "User updated with ID: $userId")
        }
        return result
    }

    fun isUserExists(userId: Long): Boolean {
        Log.d(TAG, "Checking if user exists with ID: $userId")
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_ID=?",
            arrayOf(userId.toString())
        )
        val exists = cursor.count > 0
        cursor.close()
//        db.close()
        Log.d(TAG, "User exists: $exists")
        return exists
    }

// -------------------- CATEGORY METHODS --------------------

//    fun insertCategory(name: String, imagePath: String): Long {
//        val db = writableDatabase
//        val contentValues = ContentValues().apply {
//            put(COLUMN_CATEGORY_NAME, name)
//            put(COLUMN_CATEGORY_IMAGE, imagePath)
//        }
//
//        return try {
//            db.insert(TABLE_CATEGORIES, null, contentValues)
//        } finally {
//            db.close()
//        }
//    }
//
//    fun getCategoryImagePath(categoryId: Int): String? {
//        val db = readableDatabase
//        val cursor = db.rawQuery(
//            "SELECT $COLUMN_CATEGORY_IMAGE FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_ID = ?",
//            arrayOf(categoryId.toString())
//        )
//
//        return try {
//            if (cursor.moveToFirst() && !cursor.isNull(0)) {
//                cursor.getString(0)
//            } else {
//                null
//            }
//        } finally {
//            cursor.close()
//        }
//    }
//
//    private fun compressImage(original: ByteArray): ByteArray {
//        return try {
//            val options = BitmapFactory.Options().apply {
//                inSampleSize = 2
//            }
//            val bitmap = BitmapFactory.decodeByteArray(original, 0, original.size, options)
//            val outputStream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, outputStream)
//            outputStream.toByteArray()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error compressing image", e)
//            original
//        }
//    }
//
//    fun getAllCategories(): ArrayList<Category> {
//        val categoryList = ArrayList<Category>()
//        val db = readableDatabase
//        val cursor = db.rawQuery(
//            "SELECT $COLUMN_CATEGORY_ID, $COLUMN_CATEGORY_NAME FROM $TABLE_CATEGORIES",
//            null
//        )
//
//        cursor.use {
//            while (it.moveToNext()) {
//                try {
//                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
//                    val name = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
//                    categoryList.add(Category(id, name, null)) // Don't load images here
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error reading category data", e)
//                }
//            }
//        }
//        return categoryList
//    }
//
//    fun checkCategoryExists(name: String): Boolean {
//        val db = readableDatabase
//        val cursor = db.rawQuery(
//            "SELECT * FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_NAME = ?",
//            arrayOf(name)
//        )
//        return try {
//            val exists = cursor.count > 0
//            Log.d(TAG, "Category exists check: $name -> $exists")
//            exists
//        } finally {
//            cursor.close()
//            db.close()
//        }
//    }
//
//    fun getAllCategoriesWithoutImages(): ArrayList<Category> {
//        val categoryList = ArrayList<Category>()
//        val db = readableDatabase
//        val cursor = db.rawQuery(
//            "SELECT $COLUMN_CATEGORY_ID, $COLUMN_CATEGORY_NAME FROM $TABLE_CATEGORIES",
//            null
//        )
//
//        cursor.use {
//            while (it.moveToNext()) {
//                try {
//                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
//                    val name = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
//                    categoryList.add(Category(id, name, null))
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error reading category data", e)
//                }
//            }
//        }
//        return categoryList
//    }


    //===================SUB CATEGORY METHODS========================

//    fun insertSubcategoryIfNotExists(
//        name: String,
//        imageBytes: ByteArray,
//        categoryId: Int,
//        discountedPrice: Double,
//        originalPrice: Double,
//        discountPercentage: Int,
//        deliveryInfo: String,
//        details: String,
//        rating: Float
//    ) {
//        val db = this.writableDatabase
//        val contentValues = ContentValues().apply {
//            put("name", name)
//            put("image", imageBytes)
//            put("category_id", categoryId)
//            put("discounted_price", discountedPrice)
//            put("original_price", originalPrice)
//            put("discount_percentage", discountPercentage)
//            put("delivery_info", deliveryInfo)
//            put("details", details)
//            put("rating", rating)
//        }
//        db.insertWithOnConflict("subcategories", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
//        db.close()
//    }
//
//    fun getSubcategoriesByCategoryId(categoryId: Int): List<Subcategory> {
//        val subcategoryList = mutableListOf<Subcategory>()
//        val db = readableDatabase
//        val query = """
//        SELECT * FROM $TABLE_SUBCATEGORIES
//        WHERE $COLUMN_CATEGORY_FOREIGN_ID = ?
//    """.trimIndent()
//
//        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))
//
//        if (cursor.moveToFirst()) {
//            do {
//                val subcategory = Subcategory(
//                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID)),
//                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME)),
//                    image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE)),
//                    categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID)),
//                    discountedPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE)),
//                    originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE)),
//                    discountPercentage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE)),
//                    deliveryInfo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO)),
//                    details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS)),
//                    rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING)),
//                    isFavorite = false // Will be updated later
//                )
//                subcategoryList.add(subcategory)
//            } while (cursor.moveToNext())
//        }
//
//        cursor.close()
//        return subcategoryList
//    }
//
//    fun getAllSubcategories(): List<Subcategory> {
//        val subcategoryList = mutableListOf<Subcategory>()
//        val db = readableDatabase
//        val query = """
//        SELECT * FROM $TABLE_SUBCATEGORIES
//    """.trimIndent()
//        val cursor = db.rawQuery(query, null)
//
//        if (cursor.moveToFirst()) {
//            do {
//                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID))
//                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME))
//                val image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE)) // Corrected to getBlob
//                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID))
//
//                // Fetch product-related fields (if available)
//                val discountedPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE))
//                val originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE))
//                val discountPercentage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE))
//                val deliveryInfo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO))
//                val details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS))
//                val rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING))
//
//                // Create Subcategory object with all fields
//                subcategoryList.add(
//                    Subcategory(
//                        id,
//                        name,
//                        image,
//                        categoryId,
//                        discountedPrice,
//                        originalPrice,
//                        discountPercentage,
//                        deliveryInfo,
//                        details,
//                        rating
//                    )
//                )
//
//                Log.d(TAG, "Subcategory retrieved: ID=$id, Name=$name, Image=${image?.size ?: 0} bytes, CategoryID=$categoryId")
//            } while (cursor.moveToNext())
//        }
//
//        cursor.close()
//        db.close()
//        return subcategoryList
//    }
//
//
//    fun checkSubcategoryExists(name: String): Boolean {
//        val db = readableDatabase
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_SUBCATEGORIES WHERE $COLUMN_SUBCATEGORY_NAME = ?", arrayOf(name))
//        val exists = cursor.count > 0
//        Log.d(TAG, "Subcategory exists check: $name -> $exists")
//        cursor.close()
//        db.close()
//        return exists
//    }
//    fun getSubcategoriesByCategory(categoryId: Int): List<Subcategory> {
//        val subcategoryList = mutableListOf<Subcategory>()
//        val db = readableDatabase
//        val query = """
//        SELECT * FROM $TABLE_SUBCATEGORIES
//        WHERE $COLUMN_CATEGORY_FOREIGN_ID = ?
//    """.trimIndent()
//
//        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))
//
//        if (cursor.moveToFirst()) {
//            do {
//                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID))
//                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME))
//                val image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE))
//                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID))
//                val discountedPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE))
//                val originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE))
//                val discountPercentage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE))
//                val deliveryInfo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO))
//                val details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS))
//                val rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING))
//
//                subcategoryList.add(
//                    Subcategory(
//                        id,
//                        name,
//                        image,
//                        categoryId,
//                        discountedPrice,
//                        originalPrice,
//                        discountPercentage,
//                        deliveryInfo,
//                        details,
//                        rating,
//                        isFavorite = false // Default to false, can be updated later
//                    )
//                )
//            } while (cursor.moveToNext())
//        }
//
//        cursor.close()
//        return subcategoryList
//    }
    //=====================PROFILE METHODS===================================

    fun addProfile(
        userId: Long,
        fullName: String,
        phone: String,
        email: String,
        gender: String,
        pincode: String,
        city: String,
        state: String,
        dob: String,
        maritalStatus: String,
        education: String,
        income: String,
        profileImagePath: String?
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PROFILE_USER_ID, userId)
            put(COLUMN_PROFILE_FULL_NAME, fullName)
            put(COLUMN_PROFILE_PHONE, phone)
            put(COLUMN_PROFILE_EMAIL, email)
            put(COLUMN_PROFILE_GENDER, gender)
            put(COLUMN_PROFILE_PINCODE, pincode)
            put(COLUMN_PROFILE_CITY, city)
            put(COLUMN_PROFILE_STATE, state)
            put(COLUMN_PROFILE_DOB, dob)
            put(COLUMN_PROFILE_MARITAL_STATUS, maritalStatus)
            put(COLUMN_PROFILE_EDUCATION, education)
            put(COLUMN_PROFILE_INCOME, income)
            profileImagePath?.let {
                put(COLUMN_PROFILE_IMAGE_PATH, it) // <-- Store the file path
            }
        }

        val result = db.insert(TABLE_PROFILE, null, values)
        db.close()

        if (result == -1L) {
            Log.e(TAG, "Failed to insert Profile for user ID: $userId")
        } else {
            Log.d(TAG, "Profile inserted for user ID: $userId with Profile ID: $result")
        }

        return result
    }

    fun updateProfile(
        profileId: Long,
        fullName: String,
        phone: String,
        email: String,
        gender: String,
        pincode: String,
        city: String,
        state: String,
        dob: String,
        maritalStatus: String,
        education: String,
        income: String,
        profileImagePath: String?
    ): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PROFILE_FULL_NAME, fullName)
            put(COLUMN_PROFILE_PHONE, phone)
            put(COLUMN_PROFILE_EMAIL, email)
            put(COLUMN_PROFILE_GENDER, gender)
            put(COLUMN_PROFILE_PINCODE, pincode)
            put(COLUMN_PROFILE_CITY, city)
            put(COLUMN_PROFILE_STATE, state)
            put(COLUMN_PROFILE_DOB, dob)
            put(COLUMN_PROFILE_MARITAL_STATUS, maritalStatus)
            put(COLUMN_PROFILE_EDUCATION, education)
            put(COLUMN_PROFILE_INCOME, income)
            profileImagePath?.let {
                put(COLUMN_PROFILE_IMAGE_PATH, it) // <-- Store the file path
            }
        }

        val result = db.update(
            TABLE_PROFILE,
            values,
            "$COLUMN_PROFILE_ID = ?",
            arrayOf(profileId.toString())
        )

        db.close()

        if (result == 0) {
            Log.e(TAG, "Failed to update Profile with ID: $profileId")
        } else {
            Log.d(TAG, "Profile updated with ID: $profileId")
        }

        return result
    }

    fun getProfileByUserId(userId: Long): Profile? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_PROFILE WHERE $COLUMN_PROFILE_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var profile: Profile? = null
        if (cursor.moveToFirst()) {
            val profileId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_ID))
            val fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_FULL_NAME))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PHONE))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_EMAIL))
            val gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_GENDER))
            val pincode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PINCODE))
            val city = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_CITY))
            val state = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_STATE))
            val dob = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_DOB))
            val maritalStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_MARITAL_STATUS))
            val education = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_EDUCATION))
            val income = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_INCOME))
            val profileImagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE_PATH))

            profile = Profile(
                profileId, userId, fullName, phone, email, gender, pincode, city, state, dob,
                maritalStatus, education, income, profileImagePath
            )
        }

        cursor.close()
        db.close()
        return profile
    }

    fun getProfileIdByUserId(userId: Long): Long {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILE,
            arrayOf(COLUMN_PROFILE_ID),
            "$COLUMN_PROFILE_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        var profileId: Long = -1
        if (cursor.moveToFirst()) {
            profileId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_ID))
        }

        cursor.close()
        db.close()
        return profileId
    }

    fun deleteProfile(userId: Long): Int {
        val db = writableDatabase
        val result = db.delete(
            TABLE_PROFILE,
            "$COLUMN_PROFILE_USER_ID = ?",
            arrayOf(userId.toString())
        )
        db.close()
        return result
    }

    fun insertUser(name: String, email: String, phone: String, password: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_NAME, name)
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PHONE, phone)
            put(COLUMN_USER_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()
        return result
    }

    fun checkEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun checkPhoneExists(phone: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_PHONE = ?"
        val cursor = db.rawQuery(query, arrayOf(phone))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun checkUserCredentials(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    fun updatePassword(emailOrPhone: String, newPassword: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_PASSWORD, newPassword)
        }
        val whereClause = "$COLUMN_USER_EMAIL = ? OR $COLUMN_USER_PHONE = ?"
        val whereArgs = arrayOf(emailOrPhone, emailOrPhone)
        val rowsAffected = db.update(TABLE_USERS, contentValues, whereClause, whereArgs)
        db.close()
        return rowsAffected > 0
    }

    fun verifyUser(emailOrPhone: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE ($COLUMN_USER_EMAIL = ? OR $COLUMN_USER_PHONE = ?) AND $COLUMN_USER_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(emailOrPhone, emailOrPhone, password))
        val validUser = cursor.count > 0
        cursor.close()
        db.close()
        return validUser
    }

    //-----------------------------------------------ADDRESSS---------------------------------------
    fun insertAddress(
        fullName: String,
        phone: String,
        alternatePhone: String,
        pincode: String,
        state: String,
        city: String,
        houseNo: String,
        roadName: String,
        userId: Long
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ADDRESS_FULL_NAME, fullName)
            put(COLUMN_ADDRESS_PHONE, phone)
            put(COLUMN_ADDRESS_ALTERNATE_PHONE, alternatePhone)
            put(COLUMN_ADDRESS_PINCODE, pincode)
            put(COLUMN_ADDRESS_STATE, state)
            put(COLUMN_ADDRESS_CITY, city)
            put(COLUMN_ADDRESS_HOUSE_NO, houseNo)
            put(COLUMN_ADDRESS_ROAD_NAME, roadName)
            put(COLUMN_ADDRESS_USER_ID, userId)
        }
        val result = db.insert(TABLE_ADDRESSES, null, values)
        db.close()
        return result
    }

    fun getAddressesByUserId(userId: Long): List<Address> {
        val addressList = mutableListOf<Address>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_ADDRESSES WHERE $COLUMN_ADDRESS_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_ID))
                val fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_FULL_NAME))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_PHONE))
                val alternatePhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_ALTERNATE_PHONE))
                val pincode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_PINCODE))
                val state = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_STATE))
                val city = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_CITY))
                val houseNo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_HOUSE_NO))
                val roadName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_ROAD_NAME))

                addressList.add(
                    Address(
                        id,
                        fullName,
                        phone,
                        alternatePhone,
                        pincode,
                        state,
                        city,
                        houseNo,
                        roadName,
                        userId
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return addressList
    }

    fun deleteAddress(addressId: Long): Int {
        val db = writableDatabase
        val result = db.delete(
            TABLE_ADDRESSES,
            "$COLUMN_ADDRESS_ID = ?",
            arrayOf(addressId.toString())
        )
        db.close()
        return result
    }

    fun updateAddress(address: Address): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ADDRESS_FULL_NAME, address.fullName)
            put(COLUMN_ADDRESS_PHONE, address.phone)
            put(COLUMN_ADDRESS_ALTERNATE_PHONE, address.alternatePhone)
            put(COLUMN_ADDRESS_PINCODE, address.pincode)
            put(COLUMN_ADDRESS_STATE, address.state)
            put(COLUMN_ADDRESS_CITY, address.city)
            put(COLUMN_ADDRESS_HOUSE_NO, address.houseNo)
            put(COLUMN_ADDRESS_ROAD_NAME, address.roadName)
        }
        val result = db.update(
            TABLE_ADDRESSES,
            values,
            "$COLUMN_ADDRESS_ID = ?",
            arrayOf(address.addressId.toString())
        )
        db.close()
        return result
    }

    fun getAddressById(addressId: Long): Address? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_ADDRESSES WHERE $COLUMN_ADDRESS_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(addressId.toString())) // Fixed: Added missing parenthesis

        var address: Address? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_ID))
            val fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_FULL_NAME))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_PHONE))
            val alternatePhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_ALTERNATE_PHONE))
            val pincode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_PINCODE))
            val state = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_STATE))
            val city = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_CITY))
            val houseNo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_HOUSE_NO))
            val roadName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_ROAD_NAME))
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_USER_ID))

            address = Address(
                id,
                fullName,
                phone,
                alternatePhone,
                pincode,
                state,
                city,
                houseNo,
                roadName,
                userId
            )
        }

        cursor.close()
        db.close()
        return address
    }

    //-----------------------------------------------ORDERS---------------------------------------

    fun storeOrderDetails(userId: Long, subcategoryId: Int, quantity: Int, totalPrice: Double, paymentId: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ORDER_USER_ID, userId)
            put(COLUMN_ORDER_SUBCATEGORY_ID, subcategoryId)
            put(COLUMN_ORDER_QUANTITY, quantity)
            put(COLUMN_ORDER_TOTAL_PRICE, totalPrice)
            put(COLUMN_ORDER_PAYMENT_ID, paymentId)
        }
        val result = db.insert(TABLE_ORDERS, null, values)
        db.close()
        return result
    }

    fun storePaymentDetails(userId: Long, paymentId: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ORDER_USER_ID, userId)
            put(COLUMN_ORDER_PAYMENT_ID, paymentId)
        }
        val result = db.insert(TABLE_ORDERS, null, values)
        db.close()
        return result
    }

    fun getOrderDetailsByUserId(userId: Long): List<Order> {
        val orderList = mutableListOf<Order>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_ORDERS WHERE $COLUMN_ORDER_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID))
                val subcategoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_SUBCATEGORY_ID))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_QUANTITY))
                val totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TOTAL_PRICE))
                val paymentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_PAYMENT_ID))

                orderList.add(Order(orderId, userId, subcategoryId, quantity, totalPrice, paymentId))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return orderList
    }
    //----------------------------------------FAVOURITE ----------------------------------
    fun addFavorite(userId: Long, subcategoryId: Int): Long {
        val db = writableDatabase
        return try {
            if (isUserExists(userId) && checkSubcategoryExistsById(subcategoryId)) {
                val values = ContentValues().apply {
                    put(COLUMN_FAVORITE_USER_ID, userId)
                    put(COLUMN_FAVORITE_SUBCATEGORY_ID, subcategoryId)
                }
                db.insert(TABLE_FAVORITES, null, values)
            } else {
                Log.e(TAG, "User or Subcategory does not exist. Cannot add favorite.")
                -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite: ${e.message}")
            -1
        } finally {
        }
    }

    fun removeFavorite(userId: Long, subcategoryId: Int): Int {
        val db = writableDatabase
        return try {
            db.delete(
                TABLE_FAVORITES,
                "$COLUMN_FAVORITE_USER_ID = ? AND $COLUMN_FAVORITE_SUBCATEGORY_ID = ?",
                arrayOf(userId.toString(), subcategoryId.toString())
            ).also { count ->
                Log.d(TAG, "Removed $count favorite(s) for user $userId, product $subcategoryId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite: ${e.message}")
            0
        }
    }

    fun isFavorite(userId: Long, subcategoryId: Int): Boolean {
        val db = readableDatabase
        return db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_FAVORITE_ID),
            "$COLUMN_FAVORITE_USER_ID = ? AND $COLUMN_FAVORITE_SUBCATEGORY_ID = ?",
            arrayOf(userId.toString(), subcategoryId.toString()),
            null, null, null
        ).use { cursor ->
            cursor.count > 0
        }
    }

    private fun checkSubcategoryExistsById(subcategoryId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SUBCATEGORIES,
            arrayOf(COLUMN_SUBCATEGORY_ID),
            "$COLUMN_SUBCATEGORY_ID = ?",
            arrayOf(subcategoryId.toString()),
            null, null, null
        )
        return try {
            cursor.count > 0
        } finally {
            cursor.close()
        }
    }

    fun getFavoritesByUserId(userId: Long): List<Subcategory> {
        val favoriteList = mutableListOf<Subcategory>()
        val db = readableDatabase

        val query = """
    SELECT s.* FROM $TABLE_SUBCATEGORIES s
    JOIN $TABLE_FAVORITES f ON s.$COLUMN_SUBCATEGORY_ID = f.$COLUMN_FAVORITE_SUBCATEGORY_ID
    WHERE f.$COLUMN_FAVORITE_USER_ID = ?
    """.trimIndent()

        Log.d(TAG, "Executing favorites query for user $userId")

        db.rawQuery(query, arrayOf(userId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                try {
                    val subcategory = Subcategory(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME)),
                        imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE_PATH)),
                        categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID)),
                        discountedPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE)),
                        originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE)),
                        discountPercentage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE)),
                        deliveryInfo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO)),
                        details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS)),
                        rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING)),
                        isFavorite = true
                    )
                    favoriteList.add(subcategory)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing subcategory: ${e.message}")
                }
            }
        }
        return favoriteList
    }

    fun adminGetAllFavoritesWithDetails(): List<AdminFavorite> {
        val favorites = mutableListOf<AdminFavorite>()
        val db = this.readableDatabase

        val query = """
    SELECT 
        f.$COLUMN_FAVORITE_ID,
        f.$COLUMN_FAVORITE_USER_ID,
        s.$COLUMN_SUBCATEGORY_ID,
        s.$COLUMN_SUBCATEGORY_NAME,
        s.$COLUMN_SUBCATEGORY_IMAGE_PATH,
        s.$COLUMN_CATEGORY_FOREIGN_ID,
        s.$COLUMN_SUBCATEGORY_DISCOUNTED_PRICE,
        s.$COLUMN_SUBCATEGORY_ORIGINAL_PRICE,
        s.$COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE,
        s.$COLUMN_SUBCATEGORY_DELIVERY_INFO,
        s.$COLUMN_SUBCATEGORY_DETAILS,
        s.$COLUMN_SUBCATEGORY_RATING
    FROM $TABLE_FAVORITES f
    JOIN $TABLE_SUBCATEGORIES s ON f.$COLUMN_FAVORITE_SUBCATEGORY_ID = s.$COLUMN_SUBCATEGORY_ID
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        try {
            while (cursor.moveToNext()) {
                val subcategory = Subcategory(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME)),
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE_PATH)),
                    categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID)),
                    discountedPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE)),
                    originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE)),
                    discountPercentage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE)),
                    deliveryInfo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO)),
                    details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS)),
                    rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING)),
                    isFavorite = true
                )

                favorites.add(
                    AdminFavorite(
                        favoriteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE_USER_ID)),
                        subcategory = subcategory
                    )
                )
            }
        } finally {
            cursor.close()
        }

        return favorites
    }


//==============================ORDERHISTORYYYY===================================================

    fun storeOrderHistory(
        userId: Long,
        subcategoryId: Int,
        quantity: Int,
        totalPrice: Double,
        paymentId: String,
        paymentStatus: String,
        deliveryStartDate: String,
        deliveryEndDate: String,
        size: String,
        addressId: Long
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_ORDER_HISTORY_USER_ID, userId)
        values.put(COLUMN_ORDER_HISTORY_SUBCATEGORY_ID, subcategoryId)
        values.put(COLUMN_ORDER_HISTORY_QUANTITY, quantity)
        values.put(COLUMN_ORDER_HISTORY_TOTAL_PRICE, totalPrice)
        values.put(COLUMN_ORDER_HISTORY_PAYMENT_ID, paymentId)
        values.put(COLUMN_ORDER_HISTORY_PAYMENT_STATUS, paymentStatus)
        values.put(COLUMN_ORDER_HISTORY_DELIVERY_START_DATE, deliveryStartDate)
        values.put(COLUMN_ORDER_HISTORY_DELIVERY_END_DATE, deliveryEndDate)
        values.put(COLUMN_ORDER_HISTORY_SIZE, size)
        values.put(COLUMN_ORDER_HISTORY_ADDRESS_ID, addressId)

        // Add current date as order date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        values.put(COLUMN_ORDER_HISTORY_ORDER_DATE, currentDate)

        return db.insert(TABLE_ORDER_HISTORY, null, values)
    }

    // Update your getOrderHistoryByUserId method to retrieve orderDate
    fun getOrderHistoryByUserId(userId: Long): List<OrderHistory> {
        val orderHistoryList = mutableListOf<OrderHistory>()
        val db = this.readableDatabase

        // Add the COLUMN_ORDER_HISTORY_ORDER_DATE to your query
        val query = """
        SELECT oh.*, a.*, s.*
        FROM $TABLE_ORDER_HISTORY oh
        JOIN $TABLE_ADDRESSES a ON oh.$COLUMN_ORDER_HISTORY_ADDRESS_ID = a.$COLUMN_ADDRESS_ID
        JOIN $TABLE_SUBCATEGORIES s ON oh.$COLUMN_ORDER_HISTORY_SUBCATEGORY_ID = s.$COLUMN_SUBCATEGORY_ID
        WHERE oh.$COLUMN_ORDER_HISTORY_USER_ID = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                // Get the orderDate or use current date as fallback
                val orderDate = try {
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ORDER_DATE))
                } catch (e: Exception) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }

                val orderHistory = OrderHistory(
                    orderHistoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ID)),
                    userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_USER_ID)),
                    subcategoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_SUBCATEGORY_ID)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_QUANTITY)),
                    totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_TOTAL_PRICE)),
                    paymentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_PAYMENT_ID)),
                    paymentStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_PAYMENT_STATUS)),
                    deliveryStartDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_DELIVERY_START_DATE)),
                    deliveryEndDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_DELIVERY_END_DATE)),
                    size = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_SIZE)),
                    addressId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ADDRESS_ID)),
                    productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME)),
                    productImagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE_PATH)),
                    productRating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING)),
                    deliveryAddress = """
                    ${cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_FULL_NAME))}
                    ${cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_HOUSE_NO))}, ${cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_ROAD_NAME))}
                    ${cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_CITY))}, ${cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_STATE))} - ${cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_PINCODE))}
                    Phone: ${cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS_PHONE))}
                """.trimIndent(),
                    orderDate = orderDate
                )
                orderHistoryList.add(orderHistory)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orderHistoryList
    }

    fun getAllAdminOrderHistoryItems(): List<AdminOrderHistoryItem> {
        val orderItems = mutableListOf<AdminOrderHistoryItem>()
        val db = readableDatabase

        val query = """
        SELECT oh.*, 
               u.$COLUMN_USER_NAME as userName, 
               u.$COLUMN_USER_EMAIL as userEmail,
               s.$COLUMN_SUBCATEGORY_NAME as productName,
               s.$COLUMN_SUBCATEGORY_IMAGE_PATH as productImageResId,
               a.$COLUMN_ADDRESS_ID as address
        FROM $TABLE_ORDER_HISTORY oh
        JOIN $TABLE_USERS u ON oh.$COLUMN_ORDER_HISTORY_USER_ID = u.$COLUMN_USER_ID
        JOIN $TABLE_SUBCATEGORIES s ON oh.$COLUMN_ORDER_HISTORY_SUBCATEGORY_ID = s.$COLUMN_SUBCATEGORY_ID
        LEFT JOIN $TABLE_ADDRESSES a ON oh.$COLUMN_ORDER_HISTORY_ADDRESS_ID = a.$COLUMN_ADDRESS_ID
        ORDER BY oh.$COLUMN_ORDER_HISTORY_ID DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val orderItem = AdminOrderHistoryItem(
                    orderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_USER_ID)),
                    userName = cursor.getString(cursor.getColumnIndexOrThrow("userName")),
                    userEmail = cursor.getString(cursor.getColumnIndexOrThrow("userEmail")),
                    productId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_SUBCATEGORY_ID)),
                    productName = cursor.getString(cursor.getColumnIndexOrThrow("productName")),
                    productImageResId = cursor.getInt(cursor.getColumnIndexOrThrow("productImageResId")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_QUANTITY)),
                    totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_TOTAL_PRICE)),
                    paymentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_PAYMENT_ID)),
                    paymentStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_PAYMENT_STATUS)),
                    deliveryStartDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_DELIVERY_START_DATE)),
                    deliveryEndDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_DELIVERY_END_DATE)),
                    size = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_SIZE)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    orderDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ORDER_DATE))
                )
                orderItems.add(orderItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orderItems
    }
    // In DatabaseHelper.kt
    @SuppressLint("Range")
    fun getAllOrderHistoryWithDetails(): List<OrderHistory> {
        val orderHistoryList = mutableListOf<OrderHistory>()
        val db = readableDatabase

        val query = """
        SELECT 
            oh.*,
            s.$COLUMN_SUBCATEGORY_NAME as product_name,
            s.$COLUMN_SUBCATEGORY_IMAGE_PATH as product_image,
            s.$COLUMN_SUBCATEGORY_RATING as product_rating,
            a.$COLUMN_ADDRESS_FULL_NAME as full_name,
            a.$COLUMN_ADDRESS_HOUSE_NO as house_no,
            a.$COLUMN_ADDRESS_ROAD_NAME as road_name,
            a.$COLUMN_ADDRESS_CITY as city,
            a.$COLUMN_ADDRESS_STATE as state,
            a.$COLUMN_ADDRESS_PINCODE as pincode,
            a.$COLUMN_ADDRESS_PHONE as phone
        FROM $TABLE_ORDER_HISTORY oh
        JOIN $TABLE_SUBCATEGORIES s ON oh.$COLUMN_ORDER_HISTORY_SUBCATEGORY_ID = s.$COLUMN_SUBCATEGORY_ID
        LEFT JOIN $TABLE_ADDRESSES a ON oh.$COLUMN_ORDER_HISTORY_ADDRESS_ID = a.$COLUMN_ADDRESS_ID
        ORDER BY oh.$COLUMN_ORDER_HISTORY_ORDER_DATE DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                // Format delivery address
                val deliveryAddress = """
                ${cursor.getString(cursor.getColumnIndexOrThrow("full_name"))}
                ${cursor.getString(cursor.getColumnIndexOrThrow("house_no"))}, 
                ${cursor.getString(cursor.getColumnIndexOrThrow("road_name"))}
                ${cursor.getString(cursor.getColumnIndexOrThrow("city"))}, 
                ${cursor.getString(cursor.getColumnIndexOrThrow("state"))} - 
                ${cursor.getString(cursor.getColumnIndexOrThrow("pincode"))}
                Phone: ${cursor.getString(cursor.getColumnIndexOrThrow("phone"))}
            """.trimIndent()

                orderHistoryList.add(
                    OrderHistory(
                        orderHistoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ID)),
                        userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_USER_ID)),
                        subcategoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_SUBCATEGORY_ID)),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_QUANTITY)),
                        totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_TOTAL_PRICE)),
                        paymentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_PAYMENT_ID)),
                        paymentStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_PAYMENT_STATUS)),
                        deliveryStartDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_DELIVERY_START_DATE)),
                        deliveryEndDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_DELIVERY_END_DATE)),
                        size = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_SIZE)),
                        addressId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ADDRESS_ID)),
                        productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                        productImagePath = cursor.getString(cursor.getColumnIndexOrThrow("product_image")),
                        productRating = cursor.getFloat(cursor.getColumnIndexOrThrow("product_rating")),
                        deliveryAddress = deliveryAddress,
                        orderDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_HISTORY_ORDER_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orderHistoryList
    }
    //================================RATING TABLE=======================================================
    fun insertRating(userId: Long, stars: Int, feedback: String): Boolean {
        val db = writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.format(Date())

        val values = ContentValues().apply {
            put(COLUMN_RATING_USER_ID, userId)
            put(COLUMN_RATING_STARS, stars)
            put(COLUMN_RATING_FEEDBACK, feedback)
            put(COLUMN_RATING_DATE, date)
        }

        val result = db.insert(TABLE_RATINGS, null, values)
        db.close()
        return result != -1L
    }

    fun hasUserRated(userId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_RATINGS WHERE $COLUMN_RATING_USER_ID = ?",
            arrayOf(userId.toString())
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count > 0
    }

    @SuppressLint("Range")
    fun getUserRating(userId: Long): Pair<Int, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_RATING_STARS, $COLUMN_RATING_FEEDBACK FROM $TABLE_RATINGS WHERE $COLUMN_RATING_USER_ID = ?",
            arrayOf(userId.toString())
        )

        var rating: Pair<Int, String>? = null
        if (cursor.moveToFirst()) {
            val stars = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATING_STARS))
            val feedback = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RATING_FEEDBACK))
            rating = Pair(stars, feedback)
        }
        cursor.close()
        db.close()
        return rating
    }
   //===================================================================  Add to cart ============================================================
   fun addToCart(userId: Long, product: Subcategory): Long {
       val db = writableDatabase
       val values = ContentValues().apply {
           put(CART_COLUMN_USER_ID, userId)
           put(COLUMN_PRODUCT_ID, product.id)
           put(COLUMN_PRODUCT_NAME, product.name)
           put(COLUMN_PRODUCT_PRICE, product.discountedPrice)
           put(COLUMN_PRODUCT_IMAGE_RES_ID, product.imagePath) // Store file path
           put(COLUMN_DISCOUNT, product.discountPercentage)
           put(COLUMN_QUANTITY, 1) // Default quantity
       }
       return db.insert(TABLE_CART, null, values)
   }

    // Get cart items for user
    fun getCartItems(userId: Long): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_CART 
        WHERE $CART_COLUMN_USER_ID = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    cartItems.add(
                        CartItem(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_ID)),
                            userId = cursor.getLong(cursor.getColumnIndexOrThrow(CART_COLUMN_USER_ID)),
                            productId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                            price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)),
                            imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE_RES_ID)),
                            discount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISCOUNT)),
                            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                        )
                    )
                } while (cursor.moveToNext())
            }
                    cursor.close()
            return cartItems
    }
    // Remove item from cart
    fun removeFromCart(cartItemId: Int): Boolean {
        val db = this.writableDatabase
        return db.delete(TABLE_CART, "$COLUMN_CART_ID = ?", arrayOf(cartItemId.toString())) > 0
    }

    // Clear cart for user
    fun clearCart(userId: Long): Boolean {
        val db = this.writableDatabase
        return db.delete(TABLE_CART, "$COLUMN_USER_ID = ?", arrayOf(userId.toString())) > 0
    }

    // Add these to your DatabaseHelper class

    fun getAllAdminCartItems(): List<AdminCartItem> {
        val cartItems = mutableListOf<AdminCartItem>()
        val db = readableDatabase

        // Enhanced query to join with users table if needed
        val query = """
        SELECT c.*, u.$COLUMN_USER_NAME as userName, u.$COLUMN_USER_EMAIL as userEmail
        FROM $TABLE_CART c
        LEFT JOIN $TABLE_USERS u ON c.$COLUMN_USER_ID = u.$COLUMN_USER_ID
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val cartItem = AdminCartItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    productId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                    productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                    productPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)),
                    productImageResId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE_RES_ID)),
                    discount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISCOUNT)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                    userName = cursor.getString(cursor.getColumnIndexOrThrow("userName")),
                    userEmail = cursor.getString(cursor.getColumnIndexOrThrow("userEmail"))
                    // Add other admin-specific fields as needed
                )
                cartItems.add(cartItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cartItems
    }

  //  ===============================Category================================
//
//
//    // Insert a category with an image path
//    fun insertCategory(name: String, imagePath: String): Long {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put(COLUMN_CATEGORY_NAME, name)
//            put(COLUMN_CATEGORY_IMAGE_PATH, imagePath)
//        }
//        return db.insert(TABLE_CATEGORIES, null, values)
//    }
//
//    // Fetch all categories with image paths
//    fun getAllCategories(): ArrayList<Category> {
//        val categories = ArrayList<Category>()
//        val db = readableDatabase
//        val selectQuery = "SELECT $COLUMN_CATEGORY_ID, $COLUMN_CATEGORY_NAME, $COLUMN_CATEGORY_IMAGE_PATH FROM $TABLE_CATEGORIES"
//        val cursor: Cursor = db.rawQuery(selectQuery, null)
//
//        if (cursor.moveToFirst()) {
//            do {
//                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
//                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
//                val imageBytes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_IMAGE_PATH))
//                categories.add(Category(id, name, imageBytes))
//            } while (cursor.moveToNext())
//        }
//        cursor.close()
//        return categories
//    }
//
//    // Check if a category exists
//    fun checkCategoryExists(name: String): Boolean {
//        val db = readableDatabase
//        val selectQuery = "SELECT 1 FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_NAME = ?"
//        val cursor: Cursor = db.rawQuery(selectQuery, arrayOf(name))
//        val exists = cursor.count > 0
//        cursor.close()
//        return exists
//    }

    fun insertCategory(name: String, imagePath: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, name)
            put(COLUMN_CATEGORY_IMAGE_PATH, if (imagePath.isNotEmpty()) imagePath else "")
        }
        return db.insert(TABLE_CATEGORIES, null, values).also { id ->
            if (id == -1L) Log.e("DatabaseHelper", "Failed to insert category: $name")
        }
    }

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_CATEGORIES"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_IMAGE_PATH))

                Log.d("DatabaseHelper", "Loaded category: $name, path: $imagePath")

                categories.add(Category(id, name, imagePath))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }

    fun updateCategory(id: Int, name: String, imagePath: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, name)
            put(COLUMN_CATEGORY_IMAGE_PATH, imagePath)
        }
        return db.update(TABLE_CATEGORIES, values, "$COLUMN_CATEGORY_ID=?", arrayOf(id.toString()))
    }

    fun checkCategoryExists(name: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_NAME = ?",
            arrayOf(name)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // =================== SUB CATEGORY METHODS ========================

    fun insertSubcategory(
        name: String,
        imagePath: String,
        categoryId: Int,
        discountedPrice: Double,
        originalPrice: Double,
        discountPercentage: Int,
        deliveryInfo: String,
        details: String,
        rating: Float
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SUBCATEGORY_NAME, name)
            put(COLUMN_SUBCATEGORY_IMAGE_PATH, imagePath)
            put(COLUMN_CATEGORY_FOREIGN_ID, categoryId)
            put(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE, discountedPrice)
            put(COLUMN_SUBCATEGORY_ORIGINAL_PRICE, originalPrice)
            put(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE, discountPercentage)
            put(COLUMN_SUBCATEGORY_DELIVERY_INFO, deliveryInfo)
            put(COLUMN_SUBCATEGORY_DETAILS, details)
            put(COLUMN_SUBCATEGORY_RATING, rating)
        }
        return db.insert(TABLE_SUBCATEGORIES, null, values)
    }

    fun getSubcategoriesByCategory(categoryId: Int): List<Subcategory> {
        val subcategories = mutableListOf<Subcategory>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_SUBCATEGORIES 
            WHERE $COLUMN_CATEGORY_FOREIGN_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))

        if (cursor.moveToFirst()) {
            do {
                subcategories.add(
                    Subcategory(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE_PATH)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return subcategories
    }

    fun getAllSubcategories(): List<Subcategory> {
        val subcategories = mutableListOf<Subcategory>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_SUBCATEGORIES"

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                subcategories.add(
                    Subcategory(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE_PATH)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return subcategories
    }
    fun checkSubcategoryExists(name: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_SUBCATEGORIES WHERE $COLUMN_SUBCATEGORY_NAME = ?",
            arrayOf(name)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getSubcategoriesByCategoryId(categoryId: Int): List<Subcategory> {
        val subcategories = mutableListOf<Subcategory>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_SUBCATEGORIES 
        WHERE $COLUMN_CATEGORY_FOREIGN_ID = ?
        ORDER BY $COLUMN_SUBCATEGORY_NAME ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))

        with(cursor) {
            while (moveToNext()) {
                subcategories.add(
                    Subcategory(
                        getInt(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ID)),
                        getString(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_NAME)),
                        getString(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_IMAGE_PATH)),
                        getInt(getColumnIndexOrThrow(COLUMN_CATEGORY_FOREIGN_ID)),
                        getDouble(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNTED_PRICE)),
                        getDouble(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_ORIGINAL_PRICE)),
                        getInt(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE)),
                        getString(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DELIVERY_INFO)),
                        getString(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_DETAILS)),
                        getFloat(getColumnIndexOrThrow(COLUMN_SUBCATEGORY_RATING))
                    ))
            }
            close()
        }

        return subcategories
    }

}
