package com.azarpark.cunt.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azarpark.cunt.R
import com.azarpark.cunt.enums.PlateType
import com.azarpark.cunt.ui.AppTheme
import kotlinx.coroutines.launch


class ChangePlateActivity : ComponentActivity() {

    private var selectedTab by mutableStateOf(PlateType.simple)
    private var plateTags = mutableStateMapOf(
        "tag1" to "",
        "tag2" to "",
        "tag3" to "",
        "tag4" to ""
    )
    private var totalPrice by mutableIntStateOf(0)
    private var mobileNumber by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ChangePlateScreen(
                    selectedTab = selectedTab,
                    plateTags = plateTags,
                    totalPrice = totalPrice,
                    onTabSelected = { selectedTab = it },
                    onTagChanged = { key, value -> plateTags[key] = value },
                    onMobileChanged = { mobileNumber = it },
                    onPaymentRequested = ::paymentRequest,
                    onLoadData = ::loadData
                )
            }
        }
    }

    private fun paymentRequest() {
        Toast.makeText(this, "Processing payment for $totalPrice", Toast.LENGTH_SHORT).show()
        // Implement your payment logic here
    }

    private fun loadData() {
        Toast.makeText(
            this,
            "Loading data for plate: ${plateTags.values.joinToString()}",
            Toast.LENGTH_SHORT
        ).show()
        // Implement your data loading logic here
    }
}

@Composable
fun ChangePlateScreen(
    selectedTab: PlateType,
    plateTags: Map<String, String>,
    totalPrice: Int,
    onTabSelected: (PlateType) -> Unit,
    onTagChanged: (String, String) -> Unit,
    onMobileChanged: (String) -> Unit,
    onPaymentRequested: () -> Unit,
    onLoadData: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "استعلام بدهی",
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.Black
        )
        Text(
            text = "پلاک خودرو",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.align(Alignment.End)
        )

        // Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton(label = "ملی", isSelected = selectedTab == PlateType.simple) {
                onTabSelected(PlateType.simple)
            }
            TabButton(label = "ارس", isSelected = selectedTab == PlateType.old_aras) {
                onTabSelected(PlateType.old_aras)
            }
            TabButton(label = "ارس جدید", isSelected = selectedTab == PlateType.new_aras) {
                onTabSelected(PlateType.new_aras)
            }
        }
        when (selectedTab) {
            PlateType.simple -> PlateInputSimple(plateTags, onTagChanged)
            PlateType.old_aras -> PlateInputOldAras(plateTags, onTagChanged)
            PlateType.new_aras -> PlateInputNewAras(plateTags, onTagChanged)
        }





        Button(
            onClick = { coroutineScope.launch { onLoadData() } },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF007BFF)),
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
        ) {
            Text("اسکن پلاک", color = Color.White)
        }

        // National Code Input
        TextField(
            value = plateTags["tag5"] ?: "",
            onValueChange = { onMobileChanged(it) },
            placeholder = {
                Text(
                    text = "کد ملی وارد شود",
                    fontFamily = FontFamily(Font(R.font.iran_sans)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()


                )
            },
            colors = TextFieldDefaults.textFieldColors(
//                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Blue,
                unfocusedIndicatorColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        TextField(
            value = plateTags["mobile"] ?: "",
            onValueChange = { onMobileChanged(it) },
            placeholder = {
                Text(
                    text = "09*********",
                    fontFamily = FontFamily(Font(R.font.iran_sans)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()


                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.textFieldColors(
//                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Gray,
                unfocusedIndicatorColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Button(
            onClick = { coroutineScope.launch { onPaymentRequested() } },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF007BFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("استعلام بدهی", color = Color.White)
        }

        Text(
            text = "مبلغ کل: $totalPrice تومان",
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

//        // Action Buttons
//        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
//            Button(onClick = { coroutineScope.launch { onLoadData() } }) {
//                Text("Load Data")
//            }
//            Button(onClick = { coroutineScope.launch { onPaymentRequested() } }) {
//                Text("Pay")
//            }
//        }
    }
}

@Composable
fun PlateInputNewAras(plateTags: Map<String, String>, onTagChanged: (String, String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // New Aras Tag 1
        TextField(
            value = plateTags["tag1"] ?: "",
            onValueChange = { onTagChanged("tag1", it) },
            placeholder = { Text("12345") },
            modifier = Modifier
                .width(150.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )

        // New Aras Tag 2
        TextField(
            value = plateTags["tag2"] ?: "",
            onValueChange = { onTagChanged("tag2", it) },
            placeholder = { Text("55") },
            modifier = Modifier
                .width(100.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
    }
}


@Composable
fun PlateInputOldAras(plateTags: Map<String, String>, onTagChanged: (String, String) -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Old Aras Tag
        TextField(
            value = plateTags["tag1"] ?: "",
            onValueChange = { onTagChanged("tag1", it) },
            placeholder = { Text("12345") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
    }
}


@Composable
fun PlateInputSimple(plateTags: Map<String, String>, onTagChanged: (String, String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Field containing tags 1, 2, 3 with gray background
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = plateTags["tag1"] ?: "",
                        onValueChange = { onTagChanged("tag1", it) },
                        placeholder = { Text("56") },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                    )
                    TextField(
                        value = plateTags["tag2"] ?: "",
                        onValueChange = { onTagChanged("tag2", it) },
                        placeholder = { Text("س") },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                    )
                    TextField(
                        value = plateTags["tag3"] ?: "",
                        onValueChange = { onTagChanged("tag3", it) },
                        placeholder = { Text("526") },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                    )
                }
            }

            // Field containing tag 4 with gray background
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                TextField(
                    value = plateTags["tag4"] ?: "",
                    onValueChange = { onTagChanged("tag4", it) },
                    placeholder = { Text("15") },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        textColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                )
            }
        }
    }
}



@Composable
fun TabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color(0xFF007BFF) else Color.Gray
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White)
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}


@Preview
@Composable
fun Previewchangeplatescreen() {
    ChangePlateScreen(
        selectedTab = PlateType.simple,
        plateTags = mapOf("tag1" to "", "tag2" to "", "tag3" to "", "tag4" to ""),
        totalPrice = 0,
        onTabSelected = {},
        onTagChanged = { _, _ -> },
        onMobileChanged = {},
        onPaymentRequested = {},
        onLoadData = {}
    )
}









