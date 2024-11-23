package com.azarpark.watchman.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.azarpark.cunt.enums.PlateType
import com.azarpark.cunt.ui.AppTheme
import kotlinx.coroutines.launch
import java.util.Locale


class ChangePlateActivity : ComponentActivity() {

    private var selectedTab by mutableStateOf(PlateType.simple)
    private var plateTags = mutableStateMapOf(
        "tag1" to "",
        "tag2" to "",
        "tag3" to "",
        "tag4" to ""
    )
    private var totalPrice by mutableStateOf(0)
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
                    onPaymentRequested = :: paymentRequest,
                    onLoadData = :: loadData
                )
            }
        }
    }

    private fun paymentRequest() {
        Toast.makeText(this, "Processing payment for $totalPrice", Toast.LENGTH_SHORT).show()
        // Implement your payment logic here
    }

    private fun loadData() {
        Toast.makeText(this, "Loading data for plate: ${plateTags.values.joinToString()}", Toast.LENGTH_SHORT).show()
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton("Simple", selectedTab == PlateType.simple) { onTabSelected(PlateType.simple) }
            TabButton("Old Aras", selectedTab == PlateType.old_aras) { onTabSelected(PlateType.old_aras) }
            TabButton("New Aras", selectedTab == PlateType.new_aras) { onTabSelected(PlateType.new_aras) }
        }

        // Plate Input Area
        when (selectedTab) {
            PlateType.simple -> PlateInputSimple(plateTags, onTagChanged)
            PlateType.old_aras -> PlateInputOldAras(plateTags, onTagChanged)
            PlateType.new_aras -> PlateInputNewAras(plateTags, onTagChanged)
        }

        // Mobile Number Input
        TextField(
            value = plateTags["mobile"] ?: "",
            onValueChange = { onMobileChanged(it) },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Total Price
        Text(
            text = "Total Price: $totalPrice تومان",
            style = MaterialTheme.typography.h6,
            color = Color.Black
        )

        // Action Buttons
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { coroutineScope.launch { onLoadData() } }) {
                Text("Load Data")
            }
            Button(onClick = { coroutineScope.launch { onPaymentRequested() } }) {
                Text("Pay")
            }
        }
    }
}
@Composable
fun TabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color.Blue else Color.Gray
        )
    ) {
        Text(label, color = Color.White)
    }
}

@Composable
fun PlateInputSimple(plateTags: Map<String, String>, onTagChanged: (String, String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("tag1", "tag2", "tag3", "tag4").forEach { tag ->
            TextField(
                value = plateTags[tag] ?: "",
                onValueChange = { onTagChanged(tag, it) },
                label = { Text(tag.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PlateInputOldAras(plateTags: Map<String, String>, onTagChanged: (String, String) -> Unit) {
    TextField(
        value = plateTags["tag1"] ?: "",
        onValueChange = { onTagChanged("tag1", it) },
        label = { Text("Old Aras Tag") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PlateInputNewAras(plateTags: Map<String, String>, onTagChanged: (String, String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = plateTags["tag1"] ?: "",
            onValueChange = { onTagChanged("tag1", it) },
            label = { Text("Tag1") },
            modifier = Modifier.weight(1f)
        )
        TextField(
            value = plateTags["tag2"] ?: "",
            onValueChange = { onTagChanged("tag2", it) },
            label = { Text("Tag2") },
            modifier = Modifier.weight(1f)
        )
    }
}
