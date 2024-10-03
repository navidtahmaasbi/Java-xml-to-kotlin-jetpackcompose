package com.azarpark.watchman.presentation.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.azarpark.watchman.R
import com.azarpark.watchman.databinding.PlateGetterViewBinding
import com.azarpark.watchman.enums.PlateType
import androidx.core.content.ContextCompat
import android.view.View

class PlateGetter(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayoutCompat(context, attrs, defStyleAttr) {

    var binding : PlateGetterViewBinding =
        PlateGetterViewBinding.inflate(LayoutInflater.from(context), this,true)
    private var selectedTab  = PlateType.simple
        get() = field

    init {
        setSelectedTab(selectedTab)
        binding.plateSimpleSelector.setOnClickListener { setSelectedTab(PlateType.simple) }
        binding.plateOldArasSelector.setOnClickListener { setSelectedTab(PlateType.old_aras) }
        binding.plateNewArasSelector.setOnClickListener { setSelectedTab(PlateType.new_aras) }

    }

    fun setSelectedTab(selectedTab: PlateType) {
        this.selectedTab = selectedTab
        if (selectedTab == PlateType.simple) {
            binding.plateSimpleTag1.requestFocus()
            binding.plateSimpleSelector.setBackgroundResource(R.drawable.selected_tab)
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding.plateSimpleTitle.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.plateOldArasTitle.setTextColor(ContextCompat.getColor(context,R.color.black))
            binding.plateNewArasTitle.setTextColor(ContextCompat.getColor(context,R.color.black))
            binding.plateSimpleArea.setVisibility(VISIBLE)
            binding.plateOldAras.setVisibility(GONE)
            binding.plateNewArasArea.setVisibility(GONE)
        } else if (selectedTab == PlateType.old_aras) {
            binding.plateOldAras.requestFocus()
            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.selected_tab)
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding.plateSimpleTitle.setTextColor(ContextCompat.getColor(context,R.color.black))
            binding.plateOldArasTitle.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.plateNewArasTitle.setTextColor(ContextCompat.getColor(context,R.color.black))
            binding.plateSimpleArea.setVisibility(GONE)
            binding.plateOldAras.setVisibility(VISIBLE)
            binding.plateNewArasArea.setVisibility(GONE)
        } else if (selectedTab == PlateType.new_aras) {
            binding.plateNewArasTag1.requestFocus()
            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.selected_tab)
            binding.plateSimpleTitle.setTextColor(ContextCompat.getColor(context,R.color.black))
            binding.plateOldArasTitle.setTextColor(ContextCompat.getColor(context,R.color.black))
            binding.plateNewArasTitle.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.plateSimpleArea.setVisibility(GONE)
            binding.plateOldAras.setVisibility(GONE)
            binding.plateNewArasArea.setVisibility(VISIBLE)
        }
    }

    fun getTag1() : String {
        return when (selectedTab) {
            PlateType.simple -> binding.plateSimpleTag1.text.toString()
            PlateType.old_aras -> binding.plateOldAras.text.toString()
            PlateType.new_aras -> binding.plateNewArasTag1.text.toString()
        }
    }

    fun getTag2() : String? {
        return when (selectedTab) {
            PlateType.simple -> binding.plateSimpleTag2.text.toString()
            PlateType.old_aras -> null
            PlateType.new_aras -> binding.plateNewArasTag2.text.toString()
        }
    }

    fun getTag3() : String? {
        return when (selectedTab) {
            PlateType.simple -> binding.plateSimpleTag3.text.toString()
            else -> null
        }
    }

    fun getTag4() : String? {
        return when (selectedTab) {
            PlateType.simple -> binding.plateSimpleTag4.text.toString()
            else -> null
        }
    }
}