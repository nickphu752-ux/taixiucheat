package com.ncorti.kotlin.template.app

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
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
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.*
import java.io.File

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: LinearLayout

    // NHỚ ĐIỀN API KEY CỦA BẠN VÀO ĐÂY
    private val apiKey = "AQ.Ab8RN6JJ5_dkNooJrL3QOqd0sKRY_RT-LsauSR0ls3S8Zes-Zw" 
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private var guessJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#CC000000")) 
            setPadding(32, 32, 32, 32)
        }

        val resultText = TextView(this).apply {
            text = "AI: Chờ soi cầu..."
            setTextColor(Color.WHITE)
            textSize = 15f
        }

        // Nút bấm thủ công: Bạn tự bấm khi mở bảng cầu trên game
        val analyzeButton = Button(this).apply {
            text = "CHỤP & SOI CẦU"
            setOnClickListener {
                if (guessJob?.isActive == true) return@setOnClickListener
                
                resultText.text = "Đang chụp & phân tích..."
                
                guessJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val imagePath = "/sdcard/screen_temp.png"
                        val process = Runtime.getRuntime().exec("su -c screencap -p $imagePath")
                        process.waitFor()

                        val imgFile = File(imagePath)
                        if (imgFile.exists()) {
                            val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            
                            if (bitmap != null) {
                                val inputContent = content {
                                    image(bitmap)
                                    // Yêu cầu AI tìm bảng cầu, đọc chuỗi và suy luận quy luật
                                    text("Hãy phân tích ảnh màn hình game Tài Xỉu này. " +
                                         "1. Tìm bảng lịch sử kết quả (các chấm/chữ T, X hoặc Tài, Xỉu gần nhất). " +
                                         "2. Đọc chuỗi kết quả đó. " +
                                         "3. Suy luận theo các quy luật cơ bản (cầu bệt, cầu 1-1, cầu 2-2, cầu 1-2-3...). " +
                                         "4. Đưa ra dự đoán cho ván tiếp theo. " +
                                         "Trả lời thật ngắn gọn theo định dạng: [Chuỗi hiện tại] -> [Tên quy luật] -> Chốt: TÀI (hoặc XỈU).")
                                }
                                
                                val response = generativeModel.generateContent(inputContent)
                                val ketQua = response.text?.trim() ?: "AI không phân tích được."
                                
                                withContext(Dispatchers.Main) {
                                    resultText.text = ketQua
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            resultText.text = "Lỗi: ${e.message}"
                        }
                    }
                }
            }
        }

        val closeButton = Button(this).apply {
            text = "Tắt Menu"
            setOnClickListener { stopSelf() }
        }

        floatingView.addView(resultText)
        floatingView.addView(analyzeButton)
        floatingView.addView(closeButton)

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
        guessJob?.cancel()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
