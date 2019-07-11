package moe.tlaster.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import moe.tlaster.more.widget.NavigationBar

class MainActivity : AppCompatActivity() {
    private val navigationBar by lazy {
        NavigationBar(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
//
//suspend fun <T> GeckoResult<T>.await(): T? = suspendCoroutine { scope ->
//    then({
//        GeckoResult.fromValue(scope.resume(it))
//    }, {
//        GeckoResult.fromValue(scope.resumeWithException(it))
//    })
//}