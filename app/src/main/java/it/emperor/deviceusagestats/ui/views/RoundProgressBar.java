package it.emperor.deviceusagestats.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import it.emperor.deviceusagestats.R;


public class RoundProgressBar extends View {

    private static final float D_PROGRESS = 0f;
    private static final float D_RADIUS = 30f;
    private static final float D_PADDING = 1f;
    private static final int D_COLOR_BACKGROUND = Color.DKGRAY;
    private static final int D_COLOR_PROGRESS = Color.RED;

    private Paint paint;
    private RectF rectF;

    private float progress;
    private float radius;
    private float padding;

    private int colorBackground;
    private int colorProgress;

    private OnProgressChangeListener listener;

    public RoundProgressBar(Context context) {
        super(context);
        init(null);
    }

    public RoundProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoundProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RoundProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setWillNotDraw(false);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        rectF = new RectF();

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
            progress = a.getFloat(R.styleable.RoundProgressBar_rpb_progress, D_PROGRESS);
            radius = a.getDimension(R.styleable.RoundProgressBar_rpb_radius, dpToPx(getContext(), D_RADIUS));
            padding = a.getDimension(R.styleable.RoundProgressBar_rpb_padding, dpToPx(getContext(), D_PADDING));
            colorBackground = a.getColor(R.styleable.RoundProgressBar_rpb_background_color, D_COLOR_BACKGROUND);
            colorProgress = a.getColor(R.styleable.RoundProgressBar_rpb_progress_color, D_COLOR_PROGRESS);
            a.recycle();
        } else {
            progress = D_PROGRESS;
            radius = dpToPx(getContext(), D_RADIUS);
            padding = dpToPx(getContext(), D_PADDING);
            colorBackground = D_COLOR_BACKGROUND;
            colorProgress = D_COLOR_PROGRESS;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawProgress(canvas);
    }

    private void drawBackground(Canvas canvas) {
        paint.setColor(colorBackground);
        rectF.set(0, 0, getWidth(), getHeight());

        canvas.drawRoundRect(rectF, radius, radius, paint);
    }

    private void drawProgress(Canvas canvas) {
        paint.setColor(colorProgress);
        rectF.set(0 + padding, 0 + padding, (getWidth() - padding) * progress, getHeight() - padding);

        canvas.drawRoundRect(rectF, radius, radius, paint);
    }

    public void setProgress(float progress) {
        if (progress > 1f) {
            progress = 1f;
        } else if (progress < 0f) {
            progress = 0f;
        }
        if (progress != this.progress) {
            this.progress = progress;
            if (listener != null) {
                listener.progressChanged(progress);
            }
            invalidate();
        }
    }

    public void setProgress(int progress) {
        if (progress > 100) {
            progress = 100;
        } else if (progress < 0) {
            progress = 0;
        }
        float realProgress = (float) progress / 100f;
        if (realProgress != this.progress) {
            this.progress = realProgress;
            if (listener != null) {
                listener.progressChanged(realProgress);
            }
            invalidate();
        }
    }

    public void setProgressColor(int color) {
        colorProgress = color;
        invalidate();
    }

    private float dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (dp * scale);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        this.listener = listener;
    }

    public interface OnProgressChangeListener {
        void progressChanged(float progress);
    }
}
