package com.example.inventory.model;

public class Item {

    private Long id;
    private String name;
    private String sku;
    private int quantity;
    private double price;

    public Item() {}

    public Item(Long id, String name, String sku, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public String getSku()               { return sku; }
    public void setSku(String sku)       { this.sku = sku; }

    public int getQuantity()             { return quantity; }
    public void setQuantity(int qty)     { this.quantity = qty; }

    public double getPrice()             { return price; }
    public void setPrice(double price)   { this.price = price; }
}
