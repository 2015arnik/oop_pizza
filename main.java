abstract class Enity {
    protected String name;

    public Enity (String name) {
        this.name = name;
    }

    public  String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }
}

class Ingredient extends Enity {
    private double price;

    public Ingredient (String name, double price) {
        super(name);
        this.price = price
    }

    public double getPrice() {
        return price;
    }
    
    public void setPrice(double cost) {
        this.price = price;
    }

}



