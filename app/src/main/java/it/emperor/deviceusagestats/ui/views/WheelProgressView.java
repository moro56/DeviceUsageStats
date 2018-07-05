package it.emperor.deviceusagestats.ui.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import it.emperor.deviceusagestats.R;

public class WheelProgressView extends View {

    private final static int ANGLE_INIT_OFFSET = -90;
    private final static int DEFAULT_FILLED_PERCENT = 100;
    private final static int DEFAULT_ITEM_LINE_WIDTH = 5;
    public static final int ANIMATION_DURATION = 1200;
    public static final int INNER_BACKGROUND_CIRCLE_COLOR = Color.parseColor("#6C6C6C");

    private Paint itemArcPaint;
    private Paint itemEndPointsPaint;
    private Paint innerBackgroundCirclePaint;
    private List<WheelProgressViewItem> wheelIndicatorItems;
    private int minDistViewSize;
    private int traslationX;
    private int traslationY;
    private RectF wheelBoundsRectF;
    private Paint circleBackgroundPaint;
    private ArrayList<Float> wheelItemsAngles;
    private int filledPercent = 0;
    private int lastFilledPercent = 0;
    private int itemsLineWidth = 25;

    private int indicatorColor;

    public WheelProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public WheelProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WheelProgressView(Context context) {
        super(context);
        init(null);
    }

    public void setFilledPercent(int filledPercent) {
        this.lastFilledPercent = this.filledPercent;
        if (filledPercent < 0)
            this.filledPercent = 0;
        else if (filledPercent > 100)
            this.filledPercent = 100;
        else
            this.filledPercent = filledPercent;
    }

    public void setItemsLineWidth(int itemLineWidth) {
        this.itemsLineWidth = itemLineWidth;
        invalidate();
    }

    public void addWheelIndicatorItem(WheelProgressViewItem indicatorItem) {
        this.wheelIndicatorItems.add(indicatorItem);
        recalculateItemsAngles();
        invalidate();
    }

    public void setWheelIndicatorItem(float weight, int color) {
        if (this.wheelIndicatorItems.size() == 1) {
            this.wheelIndicatorItems.get(0).setColor(color);
            this.wheelIndicatorItems.get(0).setWeight(weight);
        } else {
            this.wheelIndicatorItems.add(new WheelProgressViewItem(weight, color));
        }
        recalculateItemsAngles();
        invalidate();
    }

    public void notifyDataSetChanged() {
        recalculateItemsAngles();
        invalidate();
    }

    public void setBackgroundColor(int color) {
        circleBackgroundPaint = new Paint();
        circleBackgroundPaint.setColor(color);
        invalidate();
    }

    private void init(AttributeSet attrs) {
        int itemsLineWidth = DEFAULT_ITEM_LINE_WIDTH;
        setItemsLineWidth(itemsLineWidth);
//        setFilledPercent(DEFAULT_FILLED_PERCENT);

        int bgColor = Color.TRANSPARENT;
        int progressColor = INNER_BACKGROUND_CIRCLE_COLOR;
        int progressBackgroundColor = INNER_BACKGROUND_CIRCLE_COLOR;

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WheelProgressView);
            bgColor = a.getColor(R.styleable.WheelProgressView_wpv_background_color, bgColor);
            progressColor = a.getColor(R.styleable.WheelProgressView_wpv_progress_color, progressColor);
            progressBackgroundColor = a.getColor(R.styleable.WheelProgressView_wpv_progress_background_color, progressColor);
            a.recycle();
        }

        setBackgroundColor(bgColor);

        this.wheelIndicatorItems = new ArrayList<>();
        this.wheelItemsAngles = new ArrayList<>();

        itemArcPaint = new Paint();
        itemArcPaint.setStyle(Paint.Style.STROKE);
        itemArcPaint.setStrokeWidth(itemsLineWidth * 2);
        itemArcPaint.setAntiAlias(true);

        innerBackgroundCirclePaint = new Paint();
        innerBackgroundCirclePaint.setColor(progressBackgroundColor);
        innerBackgroundCirclePaint.setStyle(Paint.Style.STROKE);
        innerBackgroundCirclePaint.setStrokeWidth(itemsLineWidth * 2);
        innerBackgroundCirclePaint.setAntiAlias(true);

        indicatorColor = progressColor;

        itemEndPointsPaint = new Paint();
        itemEndPointsPaint.setAntiAlias(true);
    }

    private void recalculateItemsAngles() {
        wheelItemsAngles.clear();
        float total = 0;
        float angleAccumulated = 0;

        for (WheelProgressViewItem item : wheelIndicatorItems) {
            total += item.getWeight();
        }
        for (int i = 0; i < wheelIndicatorItems.size(); ++i) {
            float normalizedValue = wheelIndicatorItems.get(i).getWeight() / total;
            float angle = 360 * normalizedValue * filledPercent / 100;
            wheelItemsAngles.add(angle + angleAccumulated);
            angleAccumulated += angle;
        }
    }

    public void startItemsAnimation() {
        ObjectAnimator animation = ObjectAnimator.ofInt(WheelProgressView.this, "filledPercent", lastFilledPercent, filledPercent);
        animation.setDuration(ANIMATION_DURATION);
        animation.setInterpolator(PathInterpolatorCompat.create(0.4F, 0.0F, 0.2F, 1.0F));
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                recalculateItemsAngles();
                invalidate();
            }
        });
        animation.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int viewHeight = getMeasuredHeight();
        int viewWidth = getMeasuredWidth();
        this.minDistViewSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        int maxDistViewSize = Math.max(getMeasuredWidth(), getMeasuredHeight());

        if (viewWidth <= viewHeight) {
            this.traslationX = 0;
            this.traslationY = (maxDistViewSize - minDistViewSize) / 2;
        } else {
            this.traslationX = (maxDistViewSize - minDistViewSize) / 2;
            this.traslationY = 0;
        }
        //noinspection SuspiciousNameCombination
        wheelBoundsRectF = new RectF(itemsLineWidth, itemsLineWidth, minDistViewSize - itemsLineWidth, minDistViewSize - itemsLineWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(traslationX, traslationY);
        if (circleBackgroundPaint != null)
            canvas.drawCircle(wheelBoundsRectF.centerX(), wheelBoundsRectF.centerY(), wheelBoundsRectF.width() / 2 - itemsLineWidth, circleBackgroundPaint);
        canvas.drawArc(wheelBoundsRectF, ANGLE_INIT_OFFSET, 360, false, innerBackgroundCirclePaint);
        drawIndicatorItems(canvas);
    }

    private void drawIndicatorItems(Canvas canvas) {
        if (wheelIndicatorItems.size() > 0) {
            for (int i = wheelIndicatorItems.size() - 1; i >= 0; i--) {
                draw(wheelIndicatorItems.get(i), wheelBoundsRectF, wheelItemsAngles.get(i), canvas);
            }
        }
    }

    private void draw(WheelProgressViewItem indicatorItem, RectF surfaceRectF, float angle, Canvas canvas) {
        itemArcPaint.setColor(indicatorItem.getColor());
        itemEndPointsPaint.setColor(indicatorItem.getColor());
        canvas.drawArc(surfaceRectF, ANGLE_INIT_OFFSET, angle, false, itemArcPaint);
        canvas.drawCircle(minDistViewSize / 2, itemsLineWidth, itemsLineWidth, itemEndPointsPaint);
        int topPosition = minDistViewSize / 2 - itemsLineWidth;
        canvas.drawCircle(
                (float) (Math.cos(Math.toRadians(angle + ANGLE_INIT_OFFSET)) * topPosition + topPosition + itemsLineWidth),
                (float) (Math.sin(Math.toRadians((angle + ANGLE_INIT_OFFSET))) * topPosition + topPosition + itemsLineWidth), itemsLineWidth, itemEndPointsPaint);
    }

    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
    }
}
