package com.azarpark.cunt.payment.behpardakht.device;


/**
 * Created by m.tavakoli
 */
public interface IPosPrinterEvent {

    void onPrintStarted();
    void onPrinterError(int error);
    void onPrintEnd();

}
