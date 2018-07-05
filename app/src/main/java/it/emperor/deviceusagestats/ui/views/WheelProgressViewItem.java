package it.emperor.deviceusagestats.ui.views;

public class WheelProgressViewItem {

    private float weight;
    private int color;

    public WheelProgressViewItem(float weight, int color) {
        this.weight = weight;
        this.color = color;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
