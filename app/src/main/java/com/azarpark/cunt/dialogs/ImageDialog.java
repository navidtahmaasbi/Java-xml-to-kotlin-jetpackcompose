package com.azarpark.cunt.dialogs;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.azarpark.cunt.databinding.ImageDialogBinding;


public class ImageDialog extends DialogFragment {
    private ImageDialogBinding binding;
    private Bitmap bitmap;

    public ImageDialog(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = ImageDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.image.setImageBitmap(bitmap);
        binding.closeBtn.setOnClickListener(view1 -> {
            dismiss();
        });
    }
}
