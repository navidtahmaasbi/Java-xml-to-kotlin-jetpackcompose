package com.azarpark.cunt.payment.behpardakht.device;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.pos.device.printer.Printer;
import java.util.concurrent.Executor;
import androidx.annotation.Nullable;


/**
 * Created by z.Gholizade
 */
public class Device {
    private static Device ourInstance;
    private Context context;


    private static Printer printer;

    public static Device getInstance(Context context) {
        return ourInstance = new Device(context);
    }

    private Device(Context context) {
        this.context = context;


        try {
            printer = Printer.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void directPrint(Bitmap receipt, IPosPrinterEvent printerEvent, Executor threadPoolExecutor)
    {
        if(receipt!=null )

            new PrintTask(receipt,printerEvent).executeOnExecutor(threadPoolExecutor);
    }

    public synchronized void  directPrint(Bitmap receipt , IPosPrinterEvent printerEvent)
    {
        directPrint(receipt,printerEvent, AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private  class PrintTask extends AsyncTask
    {

        private final Bitmap receipt;
        private final IPosPrinterEvent printerEvent;

        public PrintTask(Bitmap receipt, IPosPrinterEvent printerEvent) {
            this.receipt = receipt;
            this.printerEvent = printerEvent;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            print(receipt, printerEvent);


            return null;
        }
    }




    int ret;
    public synchronized void print(Bitmap bmp, @Nullable final IPosPrinterEvent printerEvent) {

        if (bmp != null) {

            printerEvent.onPrintStarted();
            com.pos.device.printer.PrintTask task = new com.pos.device.printer.PrintTask();
            printer.reset();
            task.setGray(300);

            ret = printer.getStatus();

            task.setPrintBitmap(bmp);
            task.addFeedPaper(0);

            printer.startPrint(task, new com.pos.device.printer.PrinterCallback() {
                @Override
                public void onResult(int i, com.pos.device.printer.PrintTask printTask) {
                    ret = i;
                    if (i!=0)
                        printerEvent.onPrinterError(i);

                }
            });

            int printerStatus = printer.getStatus();
            while (printerStatus == -1 || printerStatus == -6) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printerStatus = printer.getStatus();
            }


        }
        printerEvent.onPrintEnd();
    }


    public EnPrinterStatus printerStatus() {
        try {

            int status = printer.getStatus();

            switch (status) {
                case 0:
                    return EnPrinterStatus.Ready;
                case 1:
                    return EnPrinterStatus.Busy;
                case 2:
                    return EnPrinterStatus.PaperError;
                default:
                    return EnPrinterStatus.Error;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return EnPrinterStatus.Error;
    }


}
