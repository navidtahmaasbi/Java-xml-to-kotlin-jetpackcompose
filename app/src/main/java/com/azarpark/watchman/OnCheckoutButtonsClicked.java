package com.azarpark.watchman;

public interface OnCheckoutButtonsClicked {

    public void onPaymentClicked(ParkModel parkModel);

    public void onDebtClicked(ParkModel parkModel);

    public void onShowDebtListClicked(ParkModel parkModel);

}
