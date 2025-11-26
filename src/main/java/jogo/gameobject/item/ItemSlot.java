package jogo.gameobject.item;

public class ItemSlot {
    private final Item item;
    private int quantity;

    public ItemSlot(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
