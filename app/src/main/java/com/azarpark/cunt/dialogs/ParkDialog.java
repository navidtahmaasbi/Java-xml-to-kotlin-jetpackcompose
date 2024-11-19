package com.azarpark.cunt.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.cunt.R;
import com.azarpark.cunt.ai.tools.Rect;
import com.azarpark.cunt.databinding.ParkDialogBinding;
import com.azarpark.cunt.enums.PlateType;
import com.azarpark.cunt.interfaces.OnParkClicked;
import com.azarpark.cunt.location.SingleShotLocationProvider;
import com.azarpark.cunt.models.DetectionResult;
import com.azarpark.cunt.models.Place;
import com.azarpark.cunt.models.Plate;
import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.web_service.bodies.ParkBody;

import java.io.IOException;
import java.io.InputStream;

public class ParkDialog extends DialogFragment {

    public static final String TAG = "ParkDialogTag";
    ParkDialogBinding binding;
    private final OnParkClicked onParkClicked;
    private final Place place;
    private PlateType selectedTab = PlateType.simple;
    private final boolean isParkingNewPlateOnPreviousPlate;
    private SingleShotLocationProvider.GPSCoordinates location;
    private DetectionResult scanResult = new DetectionResult();

    public ParkDialog(OnParkClicked onParkClicked, Place place, boolean isParkingNewPlateOnPreviousPlate) {
        this.onParkClicked = onParkClicked;
        this.place = place;
        this.isParkingNewPlateOnPreviousPlate = isParkingNewPlateOnPreviousPlate;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = ParkDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());
        Assistant assistant = new Assistant();

        binding.placeNumber.setText(place.number + "");

        setSelectedTab(selectedTab);

        if (place.tag1 != null && !isParkingNewPlateOnPreviousPlate) {
            binding.plateSimpleTag1.setText(place.tag1);
            binding.plateSimpleTag2.setText(place.tag2);
            binding.plateSimpleTag3.setText(place.tag3);
            binding.plateSimpleTag4.setText(place.tag4);
        }

        binding.plateSimpleSelector.setOnClickListener(view -> setSelectedTab(PlateType.simple));

        binding.plateOldArasSelector.setOnClickListener(view -> setSelectedTab(PlateType.old_aras));

        binding.plateNewArasSelector.setOnClickListener(view -> setSelectedTab(PlateType.new_aras));

        binding.submit.setOnClickListener(view -> {

            ParkBody body = null;

            if (selectedTab == PlateType.simple &&
                    (binding.plateSimpleTag1.getText().toString().length() != 2 ||
                            binding.plateSimpleTag2.getText().toString().length() != 1 ||
                            binding.plateSimpleTag3.getText().toString().length() != 3 ||
                            binding.plateSimpleTag4.getText().toString().length() != 2))
                Toast.makeText(getContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

            else if (selectedTab == PlateType.simple &&
                    !assistant.isValidCharForTag2(binding.plateSimpleTag2.getText().toString()))
                Toast.makeText(getContext(), "حرف وسط پلاک باید یکی از حروف \"" + Constants.VALID_CHARS + "\" باشد", Toast.LENGTH_LONG).show();

            else if (selectedTab == PlateType.old_aras &&
                    binding.plateOldAras.getText().toString().length() != 5)
                Toast.makeText(getContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

            else if (selectedTab == PlateType.new_aras &&
                    (binding.plateNewArasTag1.getText().toString().length() != 5 ||
                            binding.plateNewArasTag2.getText().toString().length() != 2))
                Toast.makeText(getContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else if (selectedTab == PlateType.simple) {
                if (location != null) {
                    body = new ParkBody(
                            binding.plateSimpleTag1.getText().toString(),
                            binding.plateSimpleTag2.getText().toString(),
                            binding.plateSimpleTag3.getText().toString(),
                            binding.plateSimpleTag4.getText().toString(),
                            "simple",
                            place.id,
                            place.street_id,
                            String.valueOf(location.latitude),
                            String.valueOf(location.longitude)
                    );
                } else {
                    body = new ParkBody(
                            binding.plateSimpleTag1.getText().toString(),
                            binding.plateSimpleTag2.getText().toString(),
                            binding.plateSimpleTag3.getText().toString(),
                            binding.plateSimpleTag4.getText().toString(),
                            "simple",
                            place.id,
                            place.street_id
                    );
                }
            } else if (selectedTab == PlateType.old_aras) {
                if (location != null) {
                    body = new ParkBody(
                            binding.plateOldAras.getText().toString(),
                            "old_aras",
                            place.id,
                            place.street_id,
                            String.valueOf(location.latitude),
                            String.valueOf(location.longitude)
                    );
                } else {
                    body = new ParkBody(
                            binding.plateOldAras.getText().toString(),
                            "old_aras",
                            place.id,
                            place.street_id
                    );
                }
            } else {
                if (location != null) {
                    body = new ParkBody(
                            binding.plateNewArasTag1.getText().toString(),
                            binding.plateNewArasTag2.getText().toString(),
                            "new_aras",
                            place.id,
                            place.street_id,
                            String.valueOf(location.latitude),
                            String.valueOf(location.longitude)
                    );
                } else {
                    body = new ParkBody(
                            binding.plateNewArasTag1.getText().toString(),
                            binding.plateNewArasTag2.getText().toString(),
                            "new_aras",
                            place.id,
                            place.street_id
                    );
                }
            }

            if(body != null){
                onParkClicked.clicked(
                        body,
                        binding.printCheckbox.isChecked(),
                        scanResult.getSourceImageUri(),
                        scanResult.getPlateImageUri()
                );
            }
        });

        binding.plateSimpleTag1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().contains(".")) {
                    binding.plateSimpleTag1.setText("");
                } else if (binding.plateSimpleTag1.getText().toString().length() == 2) {
                    binding.plateSimpleTag2.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (binding.plateSimpleTag2.getText().toString().length() == 1)     //size is your limit
                {
                    binding.plateSimpleTag3.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().contains(".")) {
                    binding.plateSimpleTag3.setText("");
                } else if (binding.plateSimpleTag3.getText().toString().length() == 3) {
                    binding.plateSimpleTag4.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().contains(".")) {
                    binding.plateSimpleTag4.setText("");
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateOldAras.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().contains(".")) {
                    binding.plateOldAras.setText("");
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateNewArasTag1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().contains(".")) {
                    binding.plateNewArasTag1.setText("");
                } else if (binding.plateNewArasTag1.getText().toString().length() == 5) {
                    binding.plateNewArasTag2.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateNewArasTag2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().contains(".")) {
                    binding.plateNewArasTag2.setText("");
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.scanPlateBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent("app.irana.cameraman.ACTION_SCAN_PLATE");
                startActivityForResult(intent, 1000);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setSelectedTab(PlateType selectedTab) {

        this.selectedTab = selectedTab;

        if (selectedTab == PlateType.simple) {

            binding.plateSimpleTag1.requestFocus();

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.selected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.white));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.black));

            binding.plateSimpleArea.setVisibility(View.VISIBLE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.GONE);

        } else if (selectedTab == PlateType.old_aras) {

            binding.plateOldAras.requestFocus();

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.selected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.white));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.black));

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);
        } else if (selectedTab == PlateType.new_aras) {

            binding.plateNewArasTag1.requestFocus();

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.selected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.white));

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @SuppressLint("SetTextI18n")
    public void setLocation(SingleShotLocationProvider.GPSCoordinates location) {
        this.location = location;
        if (location != null) {
            binding.location.setText("موقعیت شما : " + location.latitude + " - " + location.longitude);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null && data.getAction() != null && data.getAction().equals("plate-detection-result") && resultCode == Activity.RESULT_OK) {
            // Get the file Uri from the result
            Uri sourceUri = data.getData();
            Bitmap sourceBmp = loadBitmapFromUri(sourceUri, getContext());

            Bundle bundle = data.getExtras();
            String plateTag = bundle.getString("plate_tag");
            String cropData = bundle.getString("crop_data");
            Rect cropRect = null;
            try {
                cropRect = Rect.decode(cropData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Bitmap detectionBmp = Bitmap.createBitmap(sourceBmp, cropRect.x, cropRect.y, cropRect.width, cropRect.height);
            DetectionResult result = new DetectionResult(sourceBmp, detectionBmp, plateTag);
            setDetectionResult(result);
        }
    }

    public Bitmap loadBitmapFromUri(Uri uri, Context context) {
        Bitmap bitmap = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void setDetectionResult(DetectionResult result){
        //        Toast.makeText(getContext(), "result: " + result.getPlateTag(), Toast.LENGTH_SHORT).show();
        scanResult = result;
        Plate plate = Assistant.parse(result.getPlateTag());

        if (Assistant.isIranPlate(result.getPlateTag())) {
            setSelectedTab(PlateType.simple);
            binding.plateSimpleTag1.setText(plate.getTag1());
            binding.plateSimpleTag2.setText(plate.getTag2());
            binding.plateSimpleTag3.setText(plate.getTag3());
            binding.plateSimpleTag4.setText(plate.getTag4());
        } else if (Assistant.isOldAras(result.getPlateTag())) {
            setSelectedTab(PlateType.old_aras);
            binding.plateOldAras.setText(plate.getTag1());
        } else if (Assistant.isNewAras(result.getPlateTag())) {
            setSelectedTab(PlateType.new_aras);
            binding.plateNewArasTag1.setText(plate.getTag1());
            binding.plateNewArasTag2.setText(plate.getTag2());
        }
    }
}
