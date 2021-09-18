package com.azarpark.watchman.interfaces;

import com.azarpark.watchman.models.ParkModel;

public interface OnCheckoutButtonsClicked {

    public void onPaymentClicked(ParkModel parkModel);

    public void onDebtClicked(ParkModel parkModel);

    public void onShowDebtListClicked(ParkModel parkModel);

}
