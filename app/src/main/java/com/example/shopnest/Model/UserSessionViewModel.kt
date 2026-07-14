import androidx.lifecycle.ViewModel

class UserSessionViewModel : ViewModel() {
    private var _userId: Long = -1L
    val userId: Long get() = _userId

    fun setUserId(id: Long) {
        _userId = id
    }
}