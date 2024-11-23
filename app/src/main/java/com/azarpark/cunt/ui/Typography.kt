package com.azarpark.cunt.ui


import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.azarpark.cunt.R

val sahelFontFamily = FontFamily(
    Font(R.font.sahel_fa_num_semi_bold, FontWeight.Normal),
    Font(R.font.sahel_fa_num_semi_bold, FontWeight.Bold)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = sahelFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    h6 = TextStyle(
        fontFamily = sahelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    h5 = TextStyle(
        fontFamily = sahelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp
    )
)
