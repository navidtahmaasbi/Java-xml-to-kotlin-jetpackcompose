package com.azarpark.watchman.models

import com.azarpark.watchman.enums.PlateType

class Plate(val plateType: PlateType,val tag1:String,val tag2: String = "-1",val tag3: String = "-1",val tag4: String = "-3")