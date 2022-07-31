package com.azarpark.watchman.payment.behpardakht.device;

public interface IPosPrinterEvent {

    void onPrintStarted();
    void onPrinterError(String error, boolean isPaperError);
    void onPrintEnd();

}
