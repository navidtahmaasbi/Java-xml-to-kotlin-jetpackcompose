package com.azarpark.watchman.models;

import com.azarpark.watchman.web_service.responses.Discount;

public class ChargeOrDiscount {

    public boolean isCharge;
    public int chargeAmount;
    public Discount discount;

    public ChargeOrDiscount(int chargeAmount){
        this.isCharge = true;
        this.chargeAmount = chargeAmount;
        discount = null;
    }

    public ChargeOrDiscount(Discount discount){
        this.isCharge = false;
        this.chargeAmount = chargeAmount;
        this.discount = discount;
    }

}
