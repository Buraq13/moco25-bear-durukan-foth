import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UserViewModel : ViewModel() {
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var bio by mutableStateOf("")
    var password by mutableStateOf("")
    var profileImageUri by mutableStateOf<Uri?>(null)

    fun register(username: String, email: String, password: String, image: Uri?) {
        this.username = username
        this.email = email
        this.bio = bio
        this.password = password
        this.profileImageUri = image
    }
}