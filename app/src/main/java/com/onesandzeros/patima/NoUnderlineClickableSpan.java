package com.onesandzeros.patima;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class NoUnderlineClickableSpan extends ClickableSpan {
    private final View.OnClickListener mListener;

    public NoUnderlineClickableSpan(View.OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View widget) {
        mListener.onClick(widget);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}

