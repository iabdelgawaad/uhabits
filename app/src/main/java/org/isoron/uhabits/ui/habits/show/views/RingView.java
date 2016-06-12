/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.ui.habits.show.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.isoron.uhabits.R;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.InterfaceUtils;

public class RingView extends View
{
    public static final PorterDuffXfermode XFERMODE_CLEAR =
            new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private int color;
    private float precision;
    private float percentage;
    private int diameter;
    private float thickness;

    private RectF rect;
    private TextPaint pRing;

    private Integer backgroundColor;
    private Integer inactiveColor;

    private float em;
    private String text;
    private float textSize;
    private boolean enableFontAwesome;

    @Nullable
    private Bitmap drawingCache;
    private Canvas cacheCanvas;

    private boolean isTransparencyEnabled;

    public RingView(Context context)
    {
        super(context);

        percentage = 0.0f;
        precision = 0.01f;
        color = ColorUtils.getAndroidTestColor(0);
        thickness = InterfaceUtils.dpToPixels(getContext(), 2);
        text = "";
        textSize = context.getResources().getDimension(R.dimen.smallTextSize);

        init();
    }

    public RingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        percentage = InterfaceUtils.getFloatAttribute(context, attrs, "percentage", 0);
        precision = InterfaceUtils.getFloatAttribute(context, attrs, "precision", 0.01f);

        color = InterfaceUtils.getColorAttribute(context, attrs, "color", 0);
        backgroundColor = InterfaceUtils.getColorAttribute(context, attrs, "backgroundColor", null);
        inactiveColor = InterfaceUtils.getColorAttribute(context, attrs, "inactiveColor", null);

        thickness = InterfaceUtils.getFloatAttribute(context, attrs, "thickness", 0);
        thickness = InterfaceUtils.dpToPixels(context, thickness);

        float defaultTextSize = context.getResources().getDimension(R.dimen.smallTextSize);
        textSize = InterfaceUtils.getFloatAttribute(context, attrs, "textSize", defaultTextSize);
        textSize = InterfaceUtils.spToPixels(context, textSize);

        text = InterfaceUtils.getAttribute(context, attrs, "text", "");

        enableFontAwesome = InterfaceUtils.getBooleanAttribute(context, attrs, "enableFontAwesome", false);

        init();
    }

    public void setColor(int color)
    {
        this.color = color;
        postInvalidate();
    }

    public void setTextSize(float textSize)
    {
        this.textSize = textSize;
    }

    @Override
    public void setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
        postInvalidate();
    }

    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
        postInvalidate();
    }

    public void setPrecision(float precision)
    {
        this.precision = precision;
        postInvalidate();
    }

    public void setThickness(float thickness)
    {
        this.thickness = thickness;
        postInvalidate();
    }

    public void setText(String text)
    {
        this.text = text;
        postInvalidate();
    }

    private void init()
    {
        pRing = new TextPaint();
        pRing.setAntiAlias(true);
        pRing.setColor(color);
        pRing.setTextAlign(Paint.Align.CENTER);

        if(backgroundColor == null)
            backgroundColor = InterfaceUtils.getStyledColor(getContext(), R.attr.cardBackgroundColor);

        if(inactiveColor == null)
            inactiveColor = InterfaceUtils.getStyledColor(getContext(), R.attr.highContrastTextColor);

        inactiveColor = ColorUtils.setAlpha(inactiveColor, 0.1f);

        rect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        diameter = Math.min(height, width);

        pRing.setTextSize(textSize);
        em = pRing.measureText("M");

        setMeasuredDimension(diameter, diameter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        if(isTransparencyEnabled) reallocateCache();
    }

    private void reallocateCache()
    {
        if (drawingCache != null) drawingCache.recycle();
        drawingCache = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(drawingCache);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Canvas activeCanvas;

        if(isTransparencyEnabled)
        {
            if(drawingCache == null) reallocateCache();
            activeCanvas = cacheCanvas;
            drawingCache.eraseColor(Color.TRANSPARENT);
        }
        else
        {
            activeCanvas = canvas;
        }

        pRing.setColor(color);
        rect.set(0, 0, diameter, diameter);

        float angle = 360 * Math.round(percentage / precision) * precision;

        activeCanvas.drawArc(rect, -90, angle, true, pRing);

        pRing.setColor(inactiveColor);
        activeCanvas.drawArc(rect, angle - 90, 360 - angle, true, pRing);

        if(thickness > 0)
        {
            if(isTransparencyEnabled)
                pRing.setXfermode(XFERMODE_CLEAR);
            else
                pRing.setColor(backgroundColor);

            rect.inset(thickness, thickness);
            activeCanvas.drawArc(rect, 0, 360, true, pRing);
            pRing.setXfermode(null);

            pRing.setColor(color);
            pRing.setTextSize(textSize);
            if(enableFontAwesome) pRing.setTypeface(InterfaceUtils.getFontAwesome(getContext()));
            activeCanvas.drawText(text, rect.centerX(), rect.centerY() + 0.4f * em, pRing);
        }

        if(activeCanvas != canvas)
            canvas.drawBitmap(drawingCache, 0, 0, null);
    }

    public void setIsTransparencyEnabled(boolean isTransparencyEnabled)
    {
        this.isTransparencyEnabled = isTransparencyEnabled;
    }
}