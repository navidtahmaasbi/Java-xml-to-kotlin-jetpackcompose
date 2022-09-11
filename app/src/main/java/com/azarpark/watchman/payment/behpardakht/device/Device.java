package com.azarpark.watchman.payment.behpardakht.device;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.pax.dal.IDAL;
import com.pax.dal.IDeviceInfo;
import com.pax.dal.IMag;
import com.pax.dal.IPrinter;
import com.pax.dal.ISys;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class Device {
    private static Device ourInstance;
    private static IPrinter prn;
    private Context context;
    private static NeptuneLiteUser ppUser;
    private volatile static IDAL dal;
    private static ISys iSys;
    private static IMag mag;
    public static Device getInstance(Context context) {
        return ourInstance = new Device(context);
    }

    private Device(Context context) {
        this.context = context;


        try {
            ppUser = NeptuneLiteUser.getInstance();
            dal = ppUser.getDal(context);
            prn=dal.getPrinter();
            iSys = dal.getSys();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getModel(){
        try {
            Map<ETermInfoKey, String> terminalInfo = dal.getSys().getTermInfo();
            return terminalInfo.get(ETermInfoKey.MODEL).trim();
        } catch (Exception exceptions) {
            exceptions.printStackTrace();
        }
        return null;
    }

    public boolean hasPrinter(){
        Map<String, String> support = new HashMap<String, String>();
        String KEYBOARD = null;
        String PRINTER = null;
        try {
            Map<Integer, IDeviceInfo.ESupported> supportedMap = dal.getDeviceInfo().getModuleSupported();

            PRINTER = supportedMap.get(IDeviceInfo.MODULE_PRINTER).toString();
        } catch (Exception ex) {
        }
        String model=null;
        try {
            model =iSys.getTermInfo().get(ETermInfoKey.MODEL);
        }catch (Exception ex){}

        if (model.equals("A920")||model.equals("a920")||model.equals("A930")||model.equals("a930")||model.equals("A80")||model.equals("a80")||model.equals("A910")||model.equals("a910")) {
            return true;
        }else
        {
            if (PRINTER != null && PRINTER.equals("YES")) {
                return true;
            } else {
                return false;
            }

        }



    }

    public EnPrinterStatus printerStatus() {
        try {
            prn.init();
            int status = prn.getStatus();

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

    public EnPrinterStatus getPrinterStatus()
    {
        try {
            if(hasPrinter())
            {
                return printerStatus();
            }else{
                return EnPrinterStatus.NotFound;
            }
        }catch (Exception ex){return EnPrinterStatus.NotFound;}

    }

    public boolean isPrinterReady() {
        EnPrinterStatus status = getPrinterStatus();
        return (status.equals(EnPrinterStatus.Ready) || status.equals(EnPrinterStatus.Busy));
    }

    public void directPrint(Bitmap receipt, IPosPrinterEvent printerEvent, Executor threadPoolExecutor)
    {
        if(receipt!=null && hasPrinter())
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


    public Bitmap[] splitBitmap(Bitmap picture)
    {

        int pos = (picture.getHeight() / 8);

        Bitmap[] imgs = new Bitmap[8];
        imgs[0] = Bitmap.createBitmap(picture, 0, 0, picture.getWidth() ,  pos);
        imgs[1] = Bitmap.createBitmap(picture, 0, pos*1, picture.getWidth(),  pos);
        imgs[2] = Bitmap.createBitmap(picture, 0, pos*2, picture.getWidth(),  pos);
        imgs[3] = Bitmap.createBitmap(picture, 0, pos*3, picture.getWidth(),  pos);
        imgs[4] = Bitmap.createBitmap(picture, 0, pos*4, picture.getWidth(),  pos);
        imgs[5] = Bitmap.createBitmap(picture, 0, pos*5, picture.getWidth(),  pos);
        imgs[6] = Bitmap.createBitmap(picture, 0, pos*6, picture.getWidth(),  pos);
        imgs[7] = Bitmap.createBitmap(picture, 0, pos*7, picture.getWidth(),  pos);

        return imgs;


    }



    public synchronized void print(Bitmap bitmap, @Nullable IPosPrinterEvent printerEvent) {

        Integer[] paperError = new Integer[]{240, 2, 4, 8};

      /*  Bitmap[] bitmaps = splitBitmap(bitmap);


        for (int i = 0; i <bitmaps.length ; i++) {*/


        try {
            prn.init();

            if (prn.getStatus() != 0) {
                if (printerEvent != null)
                    printerEvent.onPrinterError("Printer not ready", false);
                return;
            }

            String model=null;
            try {

                model=getModel();

            }catch (Exception e){}


            if (model!=null)
            {


                if (model.equals("A920")||model.equals("a920")) {
                    prn.printBitmap(bitmap);

                }else
                {

                    prn.setGray(4);

                    //  prn.printBitmapWithMonoThreshold(bitmap,255);
                    prn.printBitmap(bitmap);
                }


            }else
            {

                prn.printBitmap(bitmap);

            }


            //   prn.setGray(2);
            //prn.printBitmap(bitmaps[i]);
            //   prn.printBitmap(bitmap);
            // prn.printBitmapWithMonoThreshold(bitmap,255);



            if (printerEvent != null)
                printerEvent.onPrintStarted();
            prn.start();
            int printerStatus = prn.getStatus();
            while (printerStatus == 1) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printerStatus = prn.getStatus();
            }

            if (printerEvent != null)
                if (printerStatus == 0) {
                    printerEvent.onPrintEnd();
                } else {
                    printerEvent.onPrinterError("error : " + printerStatus,
                            Arrays.asList(paperError).contains(printerStatus));
                }
        } catch (Exception e) {
            try {
                e.printStackTrace();
                if (printerEvent != null)
                    printerEvent.onPrinterError(e.getMessage(), false);
            } catch (Exception ex) {
            }

        }
        // }
    }
}
