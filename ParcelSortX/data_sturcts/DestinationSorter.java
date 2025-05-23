package data_sturcts;

import java.util.*;
import main.Parcel;

public class DestinationSorter {

    private class Node {
        String cityName;
        LinkedList<Parcel> parcelQueue;
        Node left, right;
        int dispatchedCount;

        public Node(String cityName) {
            this.cityName = cityName;
            this.parcelQueue = new LinkedList<>();
            this.dispatchedCount = 0;
        }
    }

    private Node root;

    public DestinationSorter() {
        this.root = null;
    }

    public void insertParcel(Parcel parcel) {
        root = insertRecursive(root, parcel);
    }

    private Node insertRecursive(Node current, Parcel parcel) {
        String city = parcel.getDestinationCity();

        if (current == null) {
            Node node = new Node(city);
            node.parcelQueue.add(parcel);
            return node;
        }

        int cmp = city.compareTo(current.cityName);
        if (cmp == 0) {
            current.parcelQueue.add(parcel);
        } else if (cmp < 0) {
            current.left = insertRecursive(current.left, parcel);
        } else {
            current.right = insertRecursive(current.right, parcel);
        }

        return current;
    }

    public Parcel getNextParcelForCity(String city) {
        Node node = findCityNode(root, city);
        if (node != null && !node.parcelQueue.isEmpty()) {
            return node.parcelQueue.peek();
        }
        return null;
    }

    public void removeParcel(String city, String parcelID) {
        Node node = findCityNode(root, city);
        if (node != null && !node.parcelQueue.isEmpty()) {
            Parcel first = node.parcelQueue.peek();
            if (first.getParcelID().equals(parcelID)) {
                node.parcelQueue.poll();
                node.dispatchedCount++;
            }
        }
    }

    public int countCityParcels(String city) {
        Node node = findCityNode(root, city);
        return node != null ? node.parcelQueue.size() : 0;
    }

    public int totalDeliveredTo(String city) {
        Node node = findCityNode(root, city);
        return node != null ? node.dispatchedCount : 0;
    }

    private Node findCityNode(Node current, String city) {
        if (current == null) return null;
        int cmp = city.compareTo(current.cityName);
        if (cmp == 0) return current;
        else if (cmp < 0) return findCityNode(current.left, city);
        else return findCityNode(current.right, city);
    }

    public int getHeight() {
        return heightRecursive(root);
    }

    private int heightRecursive(Node current) {
        if (current == null) return 0;
        return 1 + Math.max(heightRecursive(current.left), heightRecursive(current.right));
    }

    public String getCityWithMaxParcels() {
        return getCityWithMaxRecursive(root, null, 0);
    }

    private String getCityWithMaxRecursive(Node node, String maxCity, int maxCount) {
        if (node == null) return maxCity;

        int count = node.parcelQueue.size();
        if (count > maxCount) {
            maxCity = node.cityName;
            maxCount = count;
        }

        String leftMax = getCityWithMaxRecursive(node.left, maxCity, maxCount);
        String rightMax = getCityWithMaxRecursive(node.right, maxCity, maxCount);

        int leftCount = countCityParcels(leftMax != null ? leftMax : "");
        int rightCount = countCityParcels(rightMax != null ? rightMax : "");

        if (leftCount >= rightCount && leftCount > maxCount) return leftMax;
        if (rightCount > leftCount && rightCount > maxCount) return rightMax;
        return maxCity;
    }
}
