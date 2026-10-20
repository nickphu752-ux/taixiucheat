package com.ncorti.kotlin.template.app

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: LinearLayout

    // ĐIỀN API KEY CỦA BẠN VÀO ĐÂY
    private val apiKey = "DÁN_API_KEY_CỦA_BẠN_VÀO_ĐÂY" 
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Cấu hình giao diện menu nổi
        floatingView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#CC000000")) // Nền đen trong suốt
            setPadding(32, 32, 32, 32)
        }

        val resultText = TextView(this).apply {
            text = "AI: Sẵn sàng"
            setTextColor(Color.WHITE)
            textSize = 16f
        }

        val guessButton = Button(this).apply {
            text = "Dự đoán"
            setOnClickListener {
                resultText.text = "Đang xử lý..."
                
                // Giả lập dự đoán ngẫu nhiên (hoặc bạn có thể gọi API Gemini ở đây)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Demo: Random kết quả do chưa xử lý chụp màn hình phức tạp
                        val randomResult = if ((0..1).random() == 0) "Tài" else "Xỉu"
                        
                        CoroutineScope(Dispatchers.Main).launch {
                            resultText.text = "Kết quả: $randomResult"
                        }
                    } catch (e: Exception) {
                        CoroutineScope(Dispatchers.Main).launch {
                            resultText.text = "Lỗi kết nối"
                        }
                    }
                }
            }
        }

        floatingView.addView(resultText)
        floatingView.addView(guessButton)

        // Cấu hình WindowManager
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
