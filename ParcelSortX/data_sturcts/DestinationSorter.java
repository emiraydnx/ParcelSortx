package data_sturcts;

import ArrivalBuffer;
import Parcel;

public class DestinationSorter {
    private class Node {
        String cityName;
        ArrivalBuffer parcelList;
        Node left, right;

        Node(String cityName) {
            this.cityName = cityName;
            this.parcelList = new ArrivalBuffer(Integer.MAX_VALUE); // max as def cap value::
            this.left = null;
            this.right = null;
        }
    }

    private Node root;

    public DestinationSorter() {
        root = null;
    }

    // 🟢 Parcel ekleme
    public void insertParcel(Parcel parcel) {
        root = insertParcelRecursive(root, parcel);
    }

    private Node insertParcelRecursive(Node node, Parcel parcel) {
        if (node == null) {
            Node newNode = new Node(parcel.getDestinationCity());
            newNode.parcelList.enqueue(parcel);
            return newNode;
        }

        int compare = parcel.getDestinationCity().compareToIgnoreCase(node.cityName);
        if (compare < 0) {
            node.left = insertParcelRecursive(node.left, parcel);
        } else if (compare > 0) {
            node.right = insertParcelRecursive(node.right, parcel);
        } else {
            // Aynı şehirse kuyruğa ekle
            node.parcelList.enqueue(parcel);
        }
        return node;
    }

    // 🟡 Belirli bir şehir için kuyruktaki tüm kargoları al
    public ArrivalBuffer getCityParcels(String city) {
        Node node = search(root, city);
        return (node != null) ? node.parcelList : null;
    }

    private Node search(Node node, String city) {
        if (node == null) return null;
        int compare = city.compareToIgnoreCase(node.cityName);
        if (compare < 0) return search(node.left, city);
        else if (compare > 0) return search(node.right, city);
        else return node;
    }

    // 🔴 Belirli şehirden bir parcel sil (kargo gönderildikten sonra)
    public boolean removeParcel(String city, String parcelID) {
        Node node = search(root, city);
        if (node != null && !node.parcelList.isEmpty()) {
            // Create a temporary buffer to hold parcels
            ArrivalBuffer tempBuffer = new ArrivalBuffer(Integer.MAX_VALUE);
            boolean found = false;
            
            // Move all parcels except the one to remove to temp buffer
            while (!node.parcelList.isEmpty()) {
                Parcel p = node.parcelList.dequeue();
                if (!p.getParcelID().equals(parcelID)) {
                    tempBuffer.enqueue(p);
                } else {
                    found = true;
                }
            }
            
            // Move parcels back to original buffer
            while (!tempBuffer.isEmpty()) {
                node.parcelList.enqueue(tempBuffer.dequeue());
            }
            
            return found;
        }
        return false;
    }

    // 🟢 Şehir adına göre alfabetik sıralı BST dolaşımı
    public void inOrderTraversal() {
        inOrderRecursive(root);
    }

    private void inOrderRecursive(Node node) {
        if (node != null) {
            inOrderRecursive(node.left);
            System.out.println("City: " + node.cityName + " | Parcel Count: " + node.parcelList.size());
            inOrderRecursive(node.right);
        }
    }

    // 🔍 Şehirde kaç kargo var?
    public int countCityParcels(String city) {
        Node node = search(root, city);
        return (node != null) ? node.parcelList.size() : 0;
    }

    // 🌳 BST yüksekliği
    public int getHeight() {
        return calculateHeight(root);
    }

    private int calculateHeight(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(calculateHeight(node.left), calculateHeight(node.right));
    }

    // 📊 Toplam şehir (düğüm) sayısı
    public int getCityCount() {
        return countNodes(root);
    }

    private int countNodes(Node node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    // 🚩 En çok yüke sahip şehir (en fazla parcel içeren node)
    public String getBusiestCity() {
        return findMaxCity(root, null, 0);
    }

    private String findMaxCity(Node node, String maxCity, int maxCount) {
        if (node == null) return maxCity;
        if (node.parcelList.size() > maxCount) {
            maxCity = node.cityName;
            maxCount = node.parcelList.size();
        }
        maxCity = findMaxCity(node.left, maxCity, maxCount);
        maxCity = findMaxCity(node.right, maxCity, maxCount);
        return maxCity;
    }
}

