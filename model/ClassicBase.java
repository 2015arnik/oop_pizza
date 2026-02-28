package model;

public class ClassicBase extends Base {
    public ClassicBase() {
        super("Классическая", Base.getClassicBasePrice());
    }

    @Override
    public void setPrice(double price) {
        Base.setClassicBasePrice(price);
        super.setPrice(Base.getClassicBasePrice());
    }
}