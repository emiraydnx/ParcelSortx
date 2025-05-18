package data_sturcts;
import main.Parcel;

public class ArrivalBuffer {
    private class Node {
        Parcel data;
        Node next;

        public Node(Parcel data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node front;
    private Node rear;
    private int size;
    private final int capacity;

    public ArrivalBuffer(int capacity) {
        this.capacity = capacity;
        this.front = this.rear = null;
        this.size = 0;
    }

    // Ekleme (enqueue)
    public boolean enqueue(Parcel parcel) {
        if (isFull()) {
            System.err.println("Queue overflow! Parcel discarded: " + parcel.getParcelID());
            return false;
        }

        Node newNode = new Node(parcel);

        if (isEmpty()) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }

        size++;
        return true;
    }

    // Çıkarma (dequeue)
    public Parcel dequeue() {
        if (isEmpty()) {
            System.err.println("Queue underflow! No parcels to process.");
            return null;
        }

        Parcel removed = front.data;
        front = front.next;
        size--;

        if (front == null)
            rear = null; // Son elemandıysa rear da null olur

        return removed;
    }

    // Sıradaki parcel'ı göster ama çıkarma
    public Parcel peek() {
        return isEmpty() ? null : front.data;
    }

    public boolean isFull() {
        return size >= capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    // Debug amaçlı: kuyruğu yazdır
    public void printQueue() {
        Node temp = front;
        System.out.print("ArrivalBuffer [size=" + size + "]: ");
        while (temp != null) {
            System.out.print(temp.data.getParcelID() + " -> ");
            temp = temp.next;
        }
        System.out.println("null");
    }
}
