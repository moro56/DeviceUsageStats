package it.emperor.deviceusagestats.utils;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import java.util.ArrayList;
import java.util.List;

public class EasySpan {

    private SpannableStringBuilder mMainSpan;
    private List<SpanUnit> mUnits;

    private EasySpan(Builder builder) {
        mMainSpan = new SpannableStringBuilder(builder.totalText);
        mUnits = builder.units;
    }

    public Spanned getSpannedText() {
        for (SpanUnit unit : mUnits) {
            if (!unit.normalText) {
                mMainSpan.setSpan(unit.type, unit.start, unit.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return mMainSpan;
    }

    public static class Builder {

        private String totalText;
        private List<SpanUnit> units;

        public Builder() {
            this.totalText = "";
            this.units = new ArrayList<>();
        }

        public Builder appendSpansWithPositions(String text, int start, int end, Object... types) {
            for (Object type : types) {
                units.add(new SpanUnit(type, totalText.length() + start, totalText.length() + end));
            }
            totalText += text;
            return this;
        }

        public Builder appendSpans(String text, Object... types) {
            for (Object type : types) {
                units.add(new SpanUnit(type, totalText.length(), totalText.length() + text.length()));
            }
            totalText += text;
            return this;
        }

        public Builder appendText(String text) {
            if (text != null) {
                units.add(new SpanUnit());
                totalText += text;
            }
            return this;
        }

        public boolean isEmpty() {
            return totalText.isEmpty();
        }

        public Builder clear() {
            this.totalText = "";
            this.units.clear();
            return this;
        }

        public EasySpan build() {
            return new EasySpan(this);
        }
    }

    private static class SpanUnit {

        private Object type;
        private int start;
        private int end;
        private boolean normalText;

        SpanUnit(Object type, int start, int end) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.normalText = false;
        }

        SpanUnit() {
            this.normalText = true;
        }
    }
}
