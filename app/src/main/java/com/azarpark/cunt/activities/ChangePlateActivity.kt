package com.azarpark.cunt.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScopeInstance.align
import androidx.compose.foundation.layout.ColumnScopeInstance.weight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azarpark.cunt.R
import com.azarpark.cunt.adapters.DebtObjectAdapter
import com.azarpark.cunt.core.AppConfig
import com.azarpark.cunt.core.AppConfig.Companion.setPaymentType
import com.azarpark.cunt.databinding.ActivityChangePlateBinding
import com.azarpark.cunt.databinding.DebtClearedPrintTemplateBinding
import com.azarpark.cunt.databinding.DebtClearedPrintTemplateContainerBinding
import com.azarpark.cunt.databinding.FreewayDebtClearedPrintTemplateBinding
import com.azarpark.cunt.databinding.PlatePrintTemplateBinding
import com.azarpark.cunt.dialogs.LoadingBar
import com.azarpark.cunt.dialogs.MessageDialog
import com.azarpark.cunt.enums.PlateType
import com.azarpark.cunt.models.DetectionResult
import com.azarpark.cunt.models.Transaction
import com.azarpark.cunt.payment.PaymentService
import com.azarpark.cunt.payment.PaymentService.OnPaymentCallback
import com.azarpark.cunt.payment.PaymentService.OnTransactionCreated
import com.azarpark.cunt.payment.ShabaType
import com.azarpark.cunt.payment.TransactionAmount
import com.azarpark.cunt.utils.Assistant
import com.azarpark.cunt.utils.Constants
import com.azarpark.cunt.utils.Logger.Companion.e
import com.azarpark.cunt.utils.SharedPreferencesRepository
import com.azarpark.cunt.web_service.NewErrorHandler
import com.azarpark.cunt.web_service.WebService
import com.azarpark.cunt.web_service.responses.DebtHistoryResponse
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class ChangePlateActivity : AppCompatActivity() {
    var binding: ActivityChangePlateBinding? = null
    private var selectedTab = PlateType.simple
    var loadingBar: LoadingBar? = null
    var paymentService: PaymentService? = null
    var assistant: Assistant? = null
    var webService: WebService = WebService()
    var messageDialog: MessageDialog? = null
    var wagePrice: Int = 0
    var totalPrice: Int = 0


    var ptag1: String? = null
    var ptag2: String? = null
    var ptag3: String? = null
    var ptag4: String? = null
    var objectAdapter: DebtObjectAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePlateBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        assistant = Assistant()
        //        AppConfig.selectedConfig.paymentType = AppConfig.PaymentType.SAMAN;
        setPaymentType(AppConfig.PaymentType.SAMAN)

        paymentService = PaymentService.Builder()
            .activity(this)
            .webService(webService)
            .paymentCallback(object : OnPaymentCallback {
                override fun onScanDataReceived(data: Int) {
                }

                override fun onTransactionVerified(transaction: Transaction) {
                    printMiniFactor(transaction, ptag1, ptag2, ptag3, ptag4)
                }
            })
            .build()
        paymentService!!.initialize()




        binding!!.plateSimpleTag1.requestFocus()

        loadingBar = LoadingBar(this@ChangePlateActivity)

        binding!!.plateSimpleSelector.setOnClickListener { view: View? -> setSelectedTab(PlateType.simple) }

        binding!!.plateOldArasSelector.setOnClickListener { view: View? -> setSelectedTab(PlateType.old_aras) }

        binding!!.plateNewArasSelector.setOnClickListener { view: View? -> setSelectedTab(PlateType.new_aras) }

        binding!!.submit.setOnClickListener { view: View? -> loadData(false) }

        binding!!.plateSimpleTag1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (binding!!.plateSimpleTag1.text.toString().length == 2) //size is your limit
                {
                    binding!!.plateSimpleTag2.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        binding!!.plateSimpleTag2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (binding!!.plateSimpleTag2.text.toString().length == 1) //size is your limit
                {
                    binding!!.plateSimpleTag3.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        binding!!.plateSimpleTag3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (binding!!.plateSimpleTag3.text.toString().length == 3) //size is your limit
                {
                    binding!!.plateSimpleTag4.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        binding!!.plateNewArasTag1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (binding!!.plateNewArasTag1.text.toString().length == 5) //size is your limit
                {
                    binding!!.plateNewArasTag2.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        binding!!.payment.setOnClickListener { view: View -> this.payment(view) }

        binding!!.scanPlateBtn.setOnClickListener { v: View? ->
            try {
                val intent = Intent("app.irana.cameraman.ACTION_SCAN_PLATE")
                startActivityForResult(intent, 1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        wagePrice = getWagePrice()
    }

    private fun payment(view: View) {
//        String mobile = binding.mobile.getText().toString();
//        if (mobile.isEmpty() || !assistant.isMobile(mobile)) {
//            messageDialog = new MessageDialog("خطا",
//                    "ثبت شماره موبایل الزامی می باشد",
//                    "ثبت شماره",
//                    () -> messageDialog.dismiss());
//
//            messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);
//            return;
//        }


        if (selectedTab == PlateType.simple) paymentRequest(
            totalPrice,
            selectedTab,
            binding!!.plateSimpleTag1.text.toString(),
            binding!!.plateSimpleTag2.text.toString(),
            binding!!.plateSimpleTag3.text.toString(),
            binding!!.plateSimpleTag4.text.toString(),
            -1

        )
        else if (selectedTab == PlateType.old_aras) paymentRequest(
            totalPrice,
            selectedTab,
            binding!!.plateOldAras.text.toString(),
            "0",
            "0",
            "0",
            -1
        )
        else if (selectedTab == PlateType.new_aras) paymentRequest(
            totalPrice,
            selectedTab,
            binding!!.plateNewArasTag1.text.toString(),
            binding!!.plateNewArasTag2.text.toString(),
            "0",
            "0",
            -1
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && data.action != null && data.action == "plate-detection-result" && resultCode == RESULT_OK) {
            val bundle = data.extras
            val sourceBmp = bundle!!.getParcelable<Parcelable>("source_bitmap") as Bitmap?
            val detectionBmp = bundle.getParcelable<Parcelable>("detection_bitmap") as Bitmap?
            val plateTag = bundle.getString("plate_tag")

            val result = DetectionResult(sourceBmp, detectionBmp, plateTag)
            val plate = Assistant.parse(result.plateTag)

            if (Assistant.isIranPlate(result.plateTag)) {
                setSelectedTab(PlateType.simple)
                binding!!.plateSimpleTag1.setText(plate.tag1)
                binding!!.plateSimpleTag2.setText(plate.tag2)
                binding!!.plateSimpleTag3.setText(plate.tag3)
                binding!!.plateSimpleTag4.setText(plate.tag4)
            } else if (Assistant.isOldAras(result.plateTag)) {
                setSelectedTab(PlateType.old_aras)
                binding!!.plateOldAras.setText(plate.tag1)
            } else if (Assistant.isNewAras(result.plateTag)) {
                setSelectedTab(PlateType.new_aras)
                binding!!.plateNewArasTag1.setText(plate.tag1)
                binding!!.plateNewArasTag2.setText(plate.tag2)
            }
        } else {
            paymentService!!.onActivityResultHandler(requestCode, resultCode, data!!)
        }
    }

    fun myOnBackPressed(view: View?) {
        onBackPressed()

        val v = this.currentFocus
        if (v != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        paymentService!!.stop()
    }

    //------------------------------------------------------------ view
    private fun setSelectedTab(selectedTab: PlateType) {
        this.selectedTab = selectedTab
        resetData()

        if (selectedTab == PlateType.simple) {
            binding!!.plateSimpleSelector.setBackgroundResource(R.drawable.selected_tab)
            binding!!.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding!!.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab)

            binding!!.plateSimpleTitle.setTextColor(resources.getColor(R.color.white))
            binding!!.plateOldArasTitle.setTextColor(resources.getColor(R.color.black))
            binding!!.plateNewArasTitle.setTextColor(resources.getColor(R.color.black))

            binding!!.plateSimpleArea.visibility = View.VISIBLE
            binding!!.plateOldAras.visibility = View.GONE
            binding!!.plateNewArasArea.visibility = View.GONE
        } else if (selectedTab == PlateType.old_aras) {
            binding!!.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding!!.plateOldArasSelector.setBackgroundResource(R.drawable.selected_tab)
            binding!!.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab)

            binding!!.plateSimpleTitle.setTextColor(resources.getColor(R.color.black))
            binding!!.plateOldArasTitle.setTextColor(resources.getColor(R.color.white))
            binding!!.plateNewArasTitle.setTextColor(resources.getColor(R.color.black))

            binding!!.plateSimpleArea.visibility = View.GONE
            binding!!.plateOldAras.visibility = View.VISIBLE
            binding!!.plateNewArasArea.visibility = View.GONE
        } else if (selectedTab == PlateType.new_aras) {
            binding!!.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding!!.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab)
            binding!!.plateNewArasSelector.setBackgroundResource(R.drawable.selected_tab)

            binding!!.plateSimpleTitle.setTextColor(resources.getColor(R.color.black))
            binding!!.plateOldArasTitle.setTextColor(resources.getColor(R.color.black))
            binding!!.plateNewArasTitle.setTextColor(resources.getColor(R.color.white))

            binding!!.plateSimpleArea.visibility = View.GONE
            binding!!.plateOldAras.visibility = View.GONE
            binding!!.plateNewArasArea.visibility = View.VISIBLE
        }
    }

    private fun resetData() {
        binding!!.printArea.visibility = View.GONE
        binding!!.debtArea.visibility = View.GONE
        binding!!.plateSimpleTag1.setText("")
        binding!!.plateSimpleTag2.setText("")
        binding!!.plateSimpleTag3.setText("")
        binding!!.plateSimpleTag4.setText("")
        binding!!.plateOldAras.setText("")
        binding!!.plateNewArasTag1.setText("")
        binding!!.plateNewArasTag2.setText("")
        binding!!.mobile.setText("")

        totalPrice = 0
        ptag1 = ""
        ptag2 = ""
        ptag3 = ""
        ptag4 = ""
    }

    //------------------------------------------------------------ api calls
    private fun loadData(isLazyLoad: Boolean) {
        val assistant = Assistant()

        if (!isLazyLoad) {
            binding!!.debtArea.visibility = View.GONE
        }

        if (selectedTab == PlateType.simple &&
            (binding!!.plateSimpleTag1.text.toString().length != 2 || binding!!.plateSimpleTag2.text.toString().length != 1 || binding!!.plateSimpleTag3.text.toString().length != 3 || binding!!.plateSimpleTag4.text.toString().length != 2)
        ) Toast.makeText(
            applicationContext, "پلاک را درست وارد کنید", Toast.LENGTH_SHORT
        ).show()
        else if (selectedTab == PlateType.simple &&
            !assistant.isPersianAlphabet(binding!!.plateSimpleTag2.text.toString())
        ) Toast.makeText(
            applicationContext, "حرف وسط پلاک باید فارسی باشد", Toast.LENGTH_SHORT
        ).show()
        else if (selectedTab == PlateType.old_aras &&
            binding!!.plateOldAras.text.toString().length != 5
        ) Toast.makeText(applicationContext, "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show()
        else if (selectedTab == PlateType.new_aras &&
            (binding!!.plateNewArasTag1.text.toString().length != 5 ||
                    binding!!.plateNewArasTag2.text.toString().length != 2)
        ) Toast.makeText(applicationContext, "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show()
        else if (selectedTab == PlateType.simple) getCarDebtHistory02(
            selectedTab,
            binding!!.plateSimpleTag1.text.toString(),
            binding!!.plateSimpleTag2.text.toString(),
            binding!!.plateSimpleTag3.text.toString(),
            binding!!.plateSimpleTag4.text.toString(),
            binding!!.nationalCode.text.toString(),
            binding!!.mobile.text.toString()

        )
        else if (selectedTab == PlateType.old_aras) getCarDebtHistory02(
            selectedTab,
            binding!!.plateOldAras.text.toString(),
            "0", "0", "0",
            binding!!.nationalCode.text.toString(),
            binding!!.mobile.text.toString()
        )
        else getCarDebtHistory02(
            selectedTab,
            binding!!.plateNewArasTag1.text.toString(),
            binding!!.plateNewArasTag2.text.toString(),
            "0", "0",
            binding!!.nationalCode.text.toString(),
            binding!!.mobile.text.toString()
        )
    }

    private fun getCarDebtHistory02(
        plateType: PlateType,
        tag1: String,
        tag2: String,
        tag3: String,
        tag4: String,
        nationalCode: String,
        mobile: String
    ) {
        val functionRunnable = Runnable {
            getCarDebtHistory02(
                plateType,
                tag1,
                tag2,
                tag3,
                tag4,
                nationalCode,
                mobile
            )
        }
        val loadingBar = LoadingBar(this@ChangePlateActivity)
        loadingBar.show()

        Assistant.hideKeyboard(this@ChangePlateActivity, binding!!.root)

        webService.getClient(applicationContext).getCarDebtHistory(
            SharedPreferencesRepository.getTokenWithPrefix(),
            plateType.toString(),
            tag1,
            tag2,
            tag3,
            tag4,
            0,
            0,
            nationalCode,
            mobile,
            1
        ).enqueue(object : Callback<DebtHistoryResponse?> {
            override fun onResponse(
                call: Call<DebtHistoryResponse?>,
                response: Response<DebtHistoryResponse?>
            ) {
                loadingBar.dismiss()
                if (NewErrorHandler.apiResponseHasError(response, applicationContext)) return

                ptag1 = tag1
                ptag2 = tag2
                ptag3 = tag3
                ptag4 = tag4

                binding!!.debtArea.visibility = View.VISIBLE


                //                List<DebtObject> receivedDebts = response.body().getObjects();
//
//                // Set SHABA numbers based on keys
//                for (DebtObject debt : debts) {
//                    if (debt.getKey().equals("freeway_debt")) {
//                        debt.setShabaNumber("wage_freeway_shaba");
//                    } else if (debt.getKey().equals("carviolation")) {
//                        debt.setShabaNumber("wage_carviolation_shaba");
//                    } else if (debt.getKey().equals("balance")) {
//                        debt.setShabaNumber("wage_azarpark_shaba");
//                    }
//                }
                objectAdapter = DebtObjectAdapter(
                    this@ChangePlateActivity,
                    response.body()!!.getObjects()
                )

                objectAdapter!!.setOnSelectionsChangedListener {
                    var total = 0
                    for (selectedItem in objectAdapter!!.selectedItems) {
                        total += selectedItem.value
                    }
                    setTotalPrice(total)
                }

                binding!!.objectLv.adapter = objectAdapter

                val params = binding!!.objectLv.layoutParams
                params.height =
                    Assistant.dpToPx(this@ChangePlateActivity, 50f) * binding!!.objectLv.count
                binding!!.objectLv.layoutParams = params
                binding!!.objectLv.requestLayout()
                objectAdapter!!.checkAll()
                setTotalPrice(response.body()!!.calculateTotalPrice())
            }

            override fun onFailure(call: Call<DebtHistoryResponse?>, t: Throwable) {
                loadingBar.dismiss()
                NewErrorHandler.apiFailureErrorHandler(
                    call,
                    t,
                    supportFragmentManager,
                    functionRunnable
                )
            }

//            override fun onResponse(
//                call: Call<DebtHistoryResponse?>,
//                response: Response<DebtHistoryResponse?>
//            ) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onFailure(call: Call<DebtHistoryResponse?>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
        })
    }

    private fun setTotalPrice(total: Int) {
        totalPrice = total

        binding!!.totalPriceTv.text = "$totalPrice تومان"
    }

    fun paymentRequest(
        amount: Int,
        plateType: PlateType?,
        tag1: String?,
        tag2: String?,
        tag3: String?,
        tag4: String?,
        placeID: Int
    ) {
        binding!!.payment.startAnimation()
        binding!!.payment.setOnClickListener(null)
        val retryFunction =
            Runnable { paymentRequest(amount, plateType, tag1, tag2, tag3, tag4, placeID) }

        val mobile = binding!!.mobile.text.toString()


        val finalAction: OnTransactionCreated = OnTransactionCreated {
            binding!!.payment.revertAnimation()
            binding!!.payment.setOnClickListener { view: View ->
                this@ChangePlateActivity.payment(
                    view
                )
            }
        }

        // prepare transaction payload
        val amountPartList: MutableList<TransactionAmount> = ArrayList()
        val payload = StringBuilder()
        var first = true
        for (selectedItem in objectAdapter!!.selectedItems) {
            if (first) first = false
            else payload.append(",")
            payload.append(selectedItem.key).append(":").append(selectedItem.getId())

            if (selectedItem.getValue() > 0) {
                var shaba: String? = ""
                if (selectedItem.getKey() == "freeway_debt") {
                    shaba = SharedPreferencesRepository.getValue(Constants.WAGE_FREEWAY_SHABA)
                }

                if (selectedItem.getKey() == "carviolation") {
                    shaba = SharedPreferencesRepository.getValue(Constants.WAGE_CARVIOLATION_SHABA)
                }

                if (selectedItem.getKey() == "balance") {
                    shaba = SharedPreferencesRepository.getValue(Constants.WAGE_AZARPARK_SHABA)
                }

                amountPartList.add(TransactionAmount(selectedItem.value, shaba!!))
            }
        }

        if (amount == 0) {
            finalAction.onCreateTransactionFinished()
            printMiniFactor(null, ptag1, ptag2, ptag3, ptag4)
        } else {
            paymentService!!.createTransaction(
                ShabaType.NON_CHARGE, plateType!!, tag1, tag2, tag3, tag4,
                amountPartList, amount, -1, Constants.TRANSACTION_TYPE_DEBT,
                finalAction, -1, true, payload.toString()
            )
        }


        //        for (DebtObject debt : debtObjects) {
//            System.out.println("DebtObject key: " + debt.getKey() + ", ID: " + debt.getId());
//            // or use Log.i() if you're in Android and want to log the output
////            Log.i("DebtObject", "Key: " + debt.getKey() + ", ID: " + debt.getId());
//        }
    }

    //    public void submitMobile(String mobile, String tag1, String tag2, String tag3, String tag4, Runnable onDone, Runnable retryFunction) {
    //        // todo: comment for release
    ////        if(true){
    ////            onDone.run();
    ////            return;
    ////        }
    //
    //        webService.getClient(this).addMobileToPlate(SharedPreferencesRepository.getTokenWithPrefix(), assistant.getPlateType(tag1, tag2, tag3, tag4).toString(), tag1 != null ? tag1 : "0", tag2 != null ? tag2 : "0", tag3 != null ? tag3 : "0", tag4 != null ? tag4 : "0", mobile, 1).enqueue(new Callback<AddMobieToPlateResponse>() {
    //            @Override
    //            public void onResponse(Call<AddMobieToPlateResponse> call, Response<AddMobieToPlateResponse> response) {
    //                loadingBar.dismiss();
    //                if (NewErrorHandler.apiResponseHasError(response, ChangePlateActivity.this)) {
    //                    binding.payment.revertAnimation();
    //                    binding.payment.setOnClickListener(ChangePlateActivity.this::payment);
    //                    return;
    //                }
    //
    //                onDone.run();
    //            }
    //
    //            @Override
    //            public void onFailure(Call<AddMobieToPlateResponse> call, Throwable t) {
    //                loadingBar.dismiss();
    //                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), retryFunction);
    //            }
    //        });
    //    }
    private fun printMiniFactor(
        transaction: Transaction?,
        tag1: String?,
        tag2: String?,
        tag3: String?,
        tag4: String?
    ) {
        binding!!.printArea.removeAllViews()
        binding!!.printArea.visibility = View.VISIBLE
        val containerBinding = DebtClearedPrintTemplateContainerBinding.inflate(
            LayoutInflater.from(
                applicationContext
            ), binding!!.printArea, true
        )

        for (selectedItem in objectAdapter!!.selectedItems) {
            if (selectedItem.getKey() == "freeway_debt") {
                val printTemplateBinding = FreewayDebtClearedPrintTemplateBinding.inflate(
                    LayoutInflater.from(
                        applicationContext
                    ), containerBinding.body, true
                )

                printTemplateBinding.priceTv.text = String.format(
                    "%s تومان", NumberFormat.getNumberInstance(
                        Locale.US
                    ).format(selectedItem.value.toLong())
                )
                printTemplateBinding.timeTv.text = assistant!!.time
                printTemplateBinding.traceNumberTv.text =
                    if (transaction != null) transaction.trace_number else ""

                val platePrintTemplateBinding = PlatePrintTemplateBinding.inflate(
                    LayoutInflater.from(
                        applicationContext
                    ), printTemplateBinding.plateContainer, true
                )
                setPrintData(platePrintTemplateBinding, tag1, tag2, tag3, tag4)
            } else if (selectedItem.getKey() == "balance") {
                val printTemplateBinding = DebtClearedPrintTemplateBinding.inflate(
                    LayoutInflater.from(
                        applicationContext
                    ), containerBinding.body, true
                )

                printTemplateBinding.priceTv.text = String.format(
                    "%s تومان", NumberFormat.getNumberInstance(
                        Locale.US
                    ).format(selectedItem.value.toLong())
                )
                printTemplateBinding.timeTv.text = assistant!!.time
                printTemplateBinding.traceNumberTv.text =
                    if (transaction != null) transaction.trace_number else ""

                val platePrintTemplateBinding = PlatePrintTemplateBinding.inflate(
                    LayoutInflater.from(
                        applicationContext
                    ), printTemplateBinding.plateContainer, true
                )
                setPrintData(platePrintTemplateBinding, tag1, tag2, tag3, tag4)
            }
        }

        paymentService!!.print(binding!!.printArea, 1500) { this.resetData() }
    }

    private fun setPrintData(
        printTemplateBinding: PlatePrintTemplateBinding,
        tag1: String?,
        tag2: String?,
        tag3: String?,
        tag4: String?
    ) {
        if (assistant!!.getPlateType(tag1, tag2, tag3, tag4) == PlateType.simple) {
            printTemplateBinding.plateSimpleArea.visibility = View.VISIBLE
            printTemplateBinding.plateOldArasArea.visibility = View.GONE
            printTemplateBinding.plateNewArasArea.visibility = View.GONE

            printTemplateBinding.plateSimpleTag1.text = tag1
            printTemplateBinding.plateSimpleTag2.text = tag2
            printTemplateBinding.plateSimpleTag3.text = tag3
            printTemplateBinding.plateSimpleTag4.text = tag4
        } else if (assistant!!.getPlateType(tag1, tag2, tag3, tag4) == PlateType.old_aras) {
            printTemplateBinding.plateSimpleArea.visibility = View.GONE
            printTemplateBinding.plateOldArasArea.visibility = View.VISIBLE
            printTemplateBinding.plateNewArasArea.visibility = View.GONE

            printTemplateBinding.plateOldArasTag1En.text = tag1
            printTemplateBinding.plateOldArasTag1Fa.text = tag1
        } else {
            printTemplateBinding.plateSimpleArea.visibility = View.GONE
            printTemplateBinding.plateOldArasArea.visibility = View.GONE
            printTemplateBinding.plateNewArasArea.visibility = View.VISIBLE

            printTemplateBinding.plateNewArasTag1En.text = tag1
            printTemplateBinding.plateNewArasTag1Fa.text = tag1
            printTemplateBinding.plateNewArasTag2En.text = tag2
            printTemplateBinding.plateNewArasTag2Fa.text = tag2
        }
    }

    private fun getWagePrice(): Int {
        var price = 0

        try {
            price = SharedPreferencesRepository.getWagePrice().toInt()
        } catch (e: Exception) {
            e("Error: No valid wage price received")
        }

        return price
    }
}
@Composable
fun ChangePlateScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        // Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(Color.Blue),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Handle Barcode Icon Click */ },
                modifier = Modifier.size(45.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_qr_code),
                    contentDescription = "Barcode",
                    tint = Color.White,
                    modifier = Modifier.visibility(visible = false)
                )
            }

            Text(
                text = "استعلام بدهی",
                color = Color.White,
                fontFamily = Font(R.font.iran_sans_bold),
                fontSize = 13.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )

            IconButton(
                onClick = { /* Handle Back Action */ },
                modifier = Modifier.size(45.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.rotate(-90f)
                )
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Vehicle Plate Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "پلاک خودرو",
                        color = Color.Gray,
                        fontFamily = Font(R.font.iran_sans),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.End)
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlateOption(title = "ارس جدید", isSelected = false)
                        PlateOption(title = "ارس", isSelected = false)
                        PlateOption(title = "ملی", isSelected = true)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Editable Plate Fields
                    EditablePlateFields()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = { /* Submit Action */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "استعلام بدهی")
            }
        }
    }
}

@Composable
fun PlateOption(title: String, isSelected: Boolean) {
    val backgroundColor = if (isSelected) Color.Blue else Color.LightGray
    val textColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .weight(1f)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(5.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontFamily = Font(R.font.iran_sans),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun EditablePlateFields() {
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlateField(hint = "56", maxLength = 2)
        PlateField(hint = "س", maxLength = 1)
        PlateField(hint = "526", maxLength = 3)
        PlateField(hint = "15", maxLength = 2)
    }
}

@Composable
fun PlateField(hint: String, maxLength: Int) {
    TextField(
        value = "",
        onValueChange = { /* Handle Change */ },
        placeholder = { Text(text = hint) },
        singleLine = true,
        maxLines = 1,
        modifier = Modifier
            .width(60.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
    )
}
