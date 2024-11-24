package com.azarpark.cunt.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.azarpark.cunt.R
import com.azarpark.cunt.dialogs.ConfirmDialog
import com.azarpark.cunt.dialogs.LoadingBar
import com.azarpark.cunt.dialogs.MessageDialog
import com.azarpark.cunt.utils.Assistant
import com.azarpark.cunt.utils.Constants
import com.azarpark.cunt.utils.SharedPreferencesRepository
import com.azarpark.cunt.web_service.NewErrorHandler
import com.azarpark.cunt.web_service.WebService
import com.azarpark.cunt.web_service.bodies.LoginBody
import com.azarpark.cunt.web_service.responses.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity :
//    AppCompatActivity()
//    androidx.activity.ComponentActivity()
FragmentActivity()
{
    //    var binding: ActivityLoginBinding? = null
    var assistant: Assistant? = null
    var webService: WebService = WebService()
    var confirmDialog: ConfirmDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding!!.root)

        assistant = Assistant()
        setContent {
            LoginActivityContent{username, password ->
                onLoginClicked(username, password)
            }
        }
    }


    fun onLoginClicked(username: String, password: String) {
        // Handle login validation and logic
        if (!assistant!!.isMobile(username)) {
            Toast.makeText(applicationContext, "شماره تلفن را درست وارد کنید", Toast.LENGTH_SHORT)
                .show()
        } else if (!assistant!!.isPassword(password)) {
            Toast.makeText(applicationContext, "رمز عبور را درست وارد کنید", Toast.LENGTH_SHORT)
                .show()
        } else {
            showConfirmation(username,password)
        }
    }

    private fun showConfirmation(username: String, password: String) {
        confirmDialog = ConfirmDialog(
            "پذیرش قوانین",
            Constants.rules,
            "تایید",
            "عدم تایید",
            object : ConfirmDialog.ConfirmButtonClicks {
                override fun onConfirmClicked() {
                    login(LoginBody(username, password), username)
                }

                override fun onCancelClicked() {
                    confirmDialog!!.dismiss()
                }
            }
        )
        confirmDialog!!.show(supportFragmentManager, MessageDialog.TAG)
    }

    private fun login(loginBody: LoginBody, mobile: String) {
        val functionRunnable = Runnable { login(loginBody, mobile) }
        val loadingBar = LoadingBar(this@LoginActivity)
        loadingBar.show()

        webService.getClient(applicationContext).login(loginBody)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    loadingBar.dismiss()
                    if (NewErrorHandler.apiResponseHasError(response, applicationContext)) return

                    response.body()?.let {
                        SharedPreferencesRepository.setToken(
                            response.body()!!.access_token
                        )

                        SharedPreferencesRepository.setValue(
                            Constants.REFRESH_TOKEN,
                            response.body()!!.refresh_token
                        )
                    }
                    SharedPreferencesRepository.setValue(Constants.USERNAME, mobile)

                    Assistant.loginEvent(loginBody.username)

                    this@LoginActivity.finish()
                    startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    loadingBar.dismiss()
                    NewErrorHandler.apiFailureErrorHandler(
                        call,
                        t,
                        confirmDialog!!.show(supportFragmentManager, MessageDialog.TAG),
                        functionRunnable
                    )
                }

//                override fun onResponse(
//                    call: Call<LoginResponse?>,
//                    response: Response<LoginResponse?>
//                ) {
//
//                }
//
//                override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
//
//                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}

@Composable
fun LoginScreen(
    username: String = "",
    onUsernameChange: (String) -> Unit = {},
    password: String = "",
    onPasswordChange: (String) -> Unit = {},
    onLoginClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ){ CenteredLogo()}

        // Login Form
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.Center)
                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                .padding(15.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Title
                Text(
                    text = "شماره موبایل خود را وارد کنید",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.iran_sans))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Username Input
                TextField(
                    value = username,
                    onValueChange = onUsernameChange,
//                    modifier = Modifier.fillMaxWidth().background(color = Color.LightGray,shape = RoundedCornerShape(10.dp)),
                    placeholder = {
                        Text(
                            text = "09*********",
                            fontFamily = FontFamily(Font(R.font.iran_sans)),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()






                        )
                    },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mobile),
                            contentDescription = null,
                            tint = Color(0xFF6699CC)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.LightGray,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password Input
                TextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth().background(color = Color.LightGray,shape = RoundedCornerShape(10.dp)),
                    placeholder = {
                        Text(
                            text = "رمز عبور",
                            fontFamily = FontFamily(Font(R.font.iran_sans)),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_password),
                            contentDescription = null,
                            tint = Color(0xFF6699CC)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Login Button
                Text(
                    text = "ورود",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF6699CC),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .clickable { onLoginClicked() }
                        .padding(10.dp),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.iran_sans_bold))
                )
            }
        }
    }
}

@Composable
fun LoginActivityContent(onLogin: (String,String)-> Unit){
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    LoginScreen(
        username = username,
        onUsernameChange = { username = it },
        password = password,
        onPasswordChange = { password = it },
        onLoginClicked = {
            onLogin(username, password)
        }
    )
}
@Composable
fun CenteredLogo(){
    Image(
        painter = painterResource(id = R.drawable.ic_login_logo),
        contentDescription = null,
        modifier = Modifier
            .size(150.dp)
            .padding(top = 30.dp),
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen(){
    LoginActivityContent {username, password ->}
}



