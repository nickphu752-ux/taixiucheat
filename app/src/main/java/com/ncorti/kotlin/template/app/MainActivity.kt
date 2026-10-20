import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tạo giao diện bằng code thuần để không cần tạo file XML
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val button = Button(this).apply {
            text = "Cấp quyền & Bật Menu Nổi"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                    // Xin quyền hiển thị trên ứng dụng khác
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    // Nếu đã có quyền thì bật Service Menu Nổi
                    startService(Intent(this@MainActivity, FloatingService::class.java))
                }
            }
        }

        layout.addView(button)
        setContentView(layout)
    }
}
